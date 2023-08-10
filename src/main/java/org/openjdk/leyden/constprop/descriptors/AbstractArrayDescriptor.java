package org.openjdk.leyden.constprop.descriptors;

import org.openjdk.leyden.constprop.values.ConstantizationValue;

public abstract class AbstractArrayDescriptor {
    public abstract void setElement(int index, ConstantizationValue<?> value);

    public abstract ConstantizationValue<?> getElement(int index);

    public abstract int length();
}
