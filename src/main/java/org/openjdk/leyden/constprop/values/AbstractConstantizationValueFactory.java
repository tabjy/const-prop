package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.NewOperation;
import org.openjdk.leyden.constprop.operations.Operation;

public abstract class AbstractConstantizationValueFactory {

    public abstract <V, T extends ConstantizationValue<V>> T createValue(String descriptor);

    public <V, T extends ConstantizationValue<V>> T createValue(String descriptor, V value) {
        return createValue(descriptor, NewOperation.from(value));
    }

    public <V, T extends ConstantizationValue<V>> T createValue(String descriptor, Operation<V> operation) {
        return createValue(descriptor, operation, new ConstantizationValue[0]);
    }

    public abstract <V, T extends ConstantizationValue<V>> T createValue(String descriptor,
                                                                         Operation<V> operation,
                                                                         ConstantizationValue<?>... sources);
}

