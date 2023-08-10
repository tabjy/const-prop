package org.openjdk.leyden.constprop.analysis;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.openjdk.leyden.constprop.targets.ConstantizationTargetFactory;
import org.openjdk.leyden.constprop.targets.DefaultConstantizationTargetRegistry;
import org.openjdk.leyden.constprop.values.ConstantizationValue;
import org.openjdk.leyden.constprop.values.ValueTypeValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ConstantizationAnalyzerTest {

    public static class AdHocTestTarget {
        public static void test() {
//            AdHocTestTarget obj3 = new AdHocTestTarget();
//            Class<?> c = AdHocTestTarget.class;
//            Object obj2;
//            Object obj = new Object();
//            int[] arr = new int[1];
            int i = 0;

//            if (i > 0.5) {
//                i = 43;
//            }

            while (i < 10) {
                i++;
            }

            int ii = i;
        }
    }

    private static byte[] readClassBytes(Class<?> clazz) {
        try (InputStream is =
                     clazz.getClassLoader().getResourceAsStream(Type.getInternalName(clazz) + ".class")) {
            return Objects.requireNonNull(is).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodNode getMethodNode(Class<?> clazz, String name, String descriptor) {
        ClassReader cr = new ClassReader(readClassBytes(clazz));
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.EXPAND_FRAMES);

        return cn.methods.stream().filter(mn -> mn.name.equals(name) && mn.desc.equals(descriptor)).findFirst()
                .orElse(null);
    }

    @Test
    void testAdhocConstantizationInterpreter() throws AnalyzerException {
        MethodNode mn = getMethodNode(AdHocTestTarget.class, "test", "()V");

        ConstantizationAnalyzer analyzer = new ConstantizationAnalyzer();
        Frame<ConstantizationValue<?>>[] frames = analyzer.analyze(Type.getInternalName(AdHocTestTarget.class), mn);

        // debug
        System.out.println(frames.length);
    }

    @Test
    void testAdhocBasicInterpreter() throws AnalyzerException {
        MethodNode mn = getMethodNode(AdHocTestTarget.class, "test", "()V");

        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        analyzer.analyze(Type.getInternalName(AdHocTestTarget.class), mn);
    }

    @Test
    void testTargetExtraction() throws AnalyzerException {
        MethodNode mn = getMethodNode(AdHocTestTarget.class, "test", "()V");

        ConstantizationAnalyzer analyzer = new ConstantizationAnalyzer();
        Frame<ConstantizationValue<?>>[] frames = analyzer.analyze(Type.getInternalName(AdHocTestTarget.class), mn);

        DefaultConstantizationTargetRegistry registry = new DefaultConstantizationTargetRegistry();
        registry.addMethodScopeTarget(Type.getInternalName(AdHocTestTarget.class), "test", "()V",
                ConstantizationTargetFactory.createLocalVariableTarget("ii", 42, mn));

        registry.getMethodScopeTargets(Type.getInternalName(AdHocTestTarget.class), "test", "()V")
                .forEach(target ->
                        System.out.println(((ValueTypeValue<?>) target.extractValueFromFrames(frames)).extrema()));
    }
}
