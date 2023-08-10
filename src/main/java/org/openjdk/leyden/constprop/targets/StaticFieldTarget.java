package org.openjdk.leyden.constprop.targets;

import org.objectweb.asm.tree.analysis.Frame;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

public class StaticFieldTarget extends ConstantizationTarget {
    public final String owner;
    public final String name;

    StaticFieldTarget(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @Override
    Scope getScope() {
        return Scope.CLASS;
    }

    @Override
    public String toString() {
        return String.format("StaticFieldTarget[owner=%s, name=%s]", owner, name);
    }

    @Override
    public ConstantizationValue<?> extractValueFromFrames(Frame<ConstantizationValue<?>>[] frames) {
        throw new UnsupportedOperationException("static fields cannot be extracted from frames");
    }
}
