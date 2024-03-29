package org.openjdk.leyden.constprop.targets;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class ConstantizationTargetFactory {
    public static ConstantizationTarget createLocalVariableTarget(int instruction,
                                                                  int slot,
                                                                  String name,
                                                                  int line) {
        return new LocalVariableTarget(instruction, slot, name, line);
    }

    public static ConstantizationTarget createLocalVariableTarget(int instruction, int slot) {
        return createLocalVariableTarget(instruction, slot, null, -1);
    }

    public static ConstantizationTarget createLocalVariableTarget(String name, int line, MethodNode methodNode) {
        LineNumberNode lineNumberNode = (LineNumberNode) Arrays.stream(methodNode.instructions.toArray())
                .filter(inst -> (inst instanceof LineNumberNode lnn) && lnn.line == line)
                .findAny().orElseThrow(() ->
                        new IllegalArgumentException(
                                String.format("Line %d not found in method %s", line, methodNode.name)));

        int instruction = methodNode.instructions.indexOf(lineNumberNode);

        LocalVariableNode variableNode = methodNode.localVariables.stream()
                .filter(var -> var.name.equals(name))
                .filter(var -> methodNode.instructions.indexOf(var.start) < instruction
                        && methodNode.instructions.indexOf(var.end) > instruction)
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException(String.format("Variable \"%s\" not in scope on line %d of %s",
                                name,
                                line,
                                methodNode.name)));

        return createLocalVariableTarget(instruction, variableNode.index, name, line);
    }

    public static ConstantizationTarget createStackValueTarget(int instruction, int slot) {
        return new StackValueTarget(instruction, slot);
    }

    public static List<List<ConstantizationTarget>> createStackValueTarget(String owner,
                                                                           String method,
                                                                           String descriptor,
                                                                           MethodNode methodNode) {
        Type methodType = Type.getMethodType(descriptor);
        Type[] argumentTypes = methodType.getArgumentTypes();

        return IntStream.range(0, methodNode.instructions.size())
                .filter(i -> methodNode.instructions.get(i) instanceof MethodInsnNode min
                        && min.owner.equals(owner)
                        && min.name.equals(method)
                        && min.desc.equals(descriptor)
                )
                .mapToObj(i -> {
                    MethodInsnNode min = (MethodInsnNode) methodNode.instructions.get(i);
                    int j = (min.getOpcode() == Opcodes.INVOKESTATIC || min.getOpcode() == Opcodes.INVOKEDYNAMIC)
                            ? argumentTypes.length
                            : argumentTypes.length + 1;

                    return IntStream.range(0, j)
                            .mapToObj(jj -> createStackValueTarget(i, j - jj - 1))
                            .toList();
                }).toList();
    }

    public static ConstantizationTarget createStaticFieldTarget(String owner, String name) {
        return new StaticFieldTarget(owner, name);
    }

    public static ConstantizationTarget createStaticFieldTarget(String name) {
        return createStaticFieldTarget(null, name);
    }
}
