package org.openjdk.leyden.constprop.analysis;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

public class ConstantizationAnalyzer extends Analyzer<ConstantizationValue<?>> {
    private final ConstantizationInterpreter interpreter;

    public ConstantizationAnalyzer() {
        this(new ConstantizationInterpreter());
    }

    public ConstantizationAnalyzer(ConstantizationInterpreter interpreter) {
        super(interpreter);

        this.interpreter = interpreter;
    }

    @Override
    public Frame<ConstantizationValue<?>>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        interpreter.setMethodNode(method);
        return super.analyze(owner, method);
    }
}
