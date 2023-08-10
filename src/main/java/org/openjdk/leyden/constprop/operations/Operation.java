package org.openjdk.leyden.constprop.operations;

import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Iterator;

public abstract sealed class Operation<R> permits NArayOperation {
    public abstract Iterator<R> accept(ConstantizationValue<?>[] sources);
}
