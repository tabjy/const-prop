package org.openjdk.leyden.constprop.targets;

import org.objectweb.asm.tree.analysis.Frame;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

public abstract class ConstantizationTarget {
    public enum Scope {CLASS, METHOD}

    /* package-private */
    abstract Scope getScope();

    public abstract ConstantizationValue<?> extractValueFromFrames(Frame<ConstantizationValue<?>>[] frames);

    // TODO: for field variables
//    abstract ConstantizationValue<?> extractValueFromClassPool()
}
