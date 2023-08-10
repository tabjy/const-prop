package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;

public non-sealed abstract class ImmutableReferenceTypeValue<T> extends ReferenceTypeValue<T> {

    public ImmutableReferenceTypeValue(String descriptor) {
        super(descriptor);
    }

    public ImmutableReferenceTypeValue(String descriptor, T value) {
        super(descriptor, value);
    }

    public ImmutableReferenceTypeValue(String descriptor, Operation<T> operation) {
        super(descriptor, operation);
    }

    public ImmutableReferenceTypeValue(String descriptor, Operation<T> operation, ConstantizationValue<?>... sources) {
        super(descriptor, operation, sources);
    }
}
