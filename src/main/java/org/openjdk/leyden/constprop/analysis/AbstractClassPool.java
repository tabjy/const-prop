package org.openjdk.leyden.constprop.analysis;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.Optional;

public abstract class AbstractClassPool {
    public abstract byte[] classForName(String name) throws ClassNotFoundException;

    public MethodNode methodForName(String clazz, String method, String descriptor) throws ClassNotFoundException, MethodNotFoundException {
        ClassReader cr = new ClassReader(classForName(clazz));
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);

        return cn.methods.stream().filter(mn -> mn.name.equals(method) && mn.desc.equals(descriptor)).findFirst()
                .orElseThrow(() ->
                        new MethodNotFoundException(clazz + "::" + method + descriptor + " cannot be found"));
    }

    public FieldNode fieldForName(String clazz, String field) throws ClassNotFoundException, FieldNotFoundException {
        ClassReader cr = new ClassReader(classForName(clazz));
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);

        return cn.fields.stream().filter(fn -> fn.name.equals(field)).findFirst()
                .orElseThrow(() -> new FieldNotFoundException(clazz + "." + field + " cannot be found"));
    }

    public Optional<Object> staticFinalFieldValueForName(String clazz, String field) throws ClassNotFoundException, FieldNotFoundException {
        FieldNode fn = fieldForName(clazz, field);

        if (Modifier.isStatic(fn.access) && Modifier.isFinal(fn.access)) {
            return Optional.of(fn.value);
        } else {
            return Optional.empty();
        }
    }

    // TODO: scan class hierarchy for instanceof and checkcast instructions
}
