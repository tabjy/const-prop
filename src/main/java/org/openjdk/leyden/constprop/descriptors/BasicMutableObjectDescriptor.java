package org.openjdk.leyden.constprop.descriptors;

import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.HashMap;
import java.util.Map;

public class BasicMutableObjectDescriptor extends AbstractMutableObjectDescriptor {
    private final Map<String, ConstantizationValue<?>> fields = new HashMap<>();

    @Override
    public void putField(String name, ConstantizationValue<?> value) {
        fields.put(name, value);
    }

    @Override
    public ConstantizationValue<?> getField(String name) {
        return fields.get(name);
    }
}
