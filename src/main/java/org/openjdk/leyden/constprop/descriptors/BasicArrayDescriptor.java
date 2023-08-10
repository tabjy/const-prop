package org.openjdk.leyden.constprop.descriptors;

import org.openjdk.leyden.constprop.util.SparseArray;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

public class BasicArrayDescriptor extends AbstractArrayDescriptor {
    private final SparseArray<ConstantizationValue<?>> array = new SparseArray<>();

    @Override
    public void setElement(int index, ConstantizationValue<?> value) {
        array.set(index, value);
    }

    @Override
    public ConstantizationValue<?> getElement(int index) {
        return array.get(index);
    }

    public int length() {
        return array.length();
    }
}
