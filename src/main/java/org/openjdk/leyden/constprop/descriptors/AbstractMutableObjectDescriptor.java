package org.openjdk.leyden.constprop.descriptors;

import org.openjdk.leyden.constprop.values.ConstantizationValue;

public abstract class AbstractMutableObjectDescriptor {
    public abstract void putField(String name, ConstantizationValue<?> value);

    public abstract ConstantizationValue<?> getField(String name);
}
