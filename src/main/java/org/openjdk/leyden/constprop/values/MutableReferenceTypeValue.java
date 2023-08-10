package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.descriptors.AbstractMutableObjectDescriptor;
import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;

public non-sealed class MutableReferenceTypeValue<T extends AbstractMutableObjectDescriptor> extends ReferenceTypeValue<T> {
    private static int ID_COUNTER = 0;

    private final long id;

    public MutableReferenceTypeValue(String descriptor) {
        super(descriptor);

        id = ID_COUNTER++;
    }

    public MutableReferenceTypeValue(String descriptor, T value) {
        super(descriptor, value);

        id = ID_COUNTER++;
    }

    public MutableReferenceTypeValue(String descriptor, Operation<T> operation) {
        super(descriptor, operation);

        id = ID_COUNTER++;
    }

    public MutableReferenceTypeValue(String descriptor, Operation<T> operation, ConstantizationValue<?>... sources) {
        super(descriptor, operation, sources);

        id = ID_COUNTER++;
    }

    private MutableReferenceTypeValue(long id, String descriptor, Operation<T> operation, ConstantizationValue<?>... sources) {
        super(descriptor, operation, sources);

        this.id = id;
    }

    public final long getId() {
        return id;
    }

    @Override
    public ConstantizationValue<T> ident() {
        return new MutableReferenceTypeValue<>(this.id, this.getDescriptor(), UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<T> merge(ConstantizationValue<T> other) {
        return null;
    }
}
