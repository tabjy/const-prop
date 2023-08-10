package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;

public class ObjectValue extends ImmutableReferenceTypeValue<ObjectValue.UnusedDesc> {
    public static final String TYPE_DESCRIPTOR = "Ljava/lang/Object;";

    public ObjectValue() {
        super(TYPE_DESCRIPTOR);
    }

    public ObjectValue(Operation<UnusedDesc> operation) {
        super(TYPE_DESCRIPTOR, operation);
    }

    public ObjectValue(Operation<UnusedDesc> operation, ConstantizationValue<?>... sources) {
        super(TYPE_DESCRIPTOR, operation, sources);
    }

    @Override
    public ConstantizationValue<UnusedDesc> ident() {
        return new ObjectValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<UnusedDesc> merge(ConstantizationValue<UnusedDesc> other) {
        return null;
    }

    public static final class UnusedDesc {
    }
}
