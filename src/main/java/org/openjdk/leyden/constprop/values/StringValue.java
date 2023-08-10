package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;

public final class StringValue extends ImmutableReferenceTypeValue<String> {
    public static final String TYPE_DESCRIPTOR = "Ljava/lang/String;";

    public StringValue() {
        super(TYPE_DESCRIPTOR);
    }

    public StringValue(String value) {
        super(TYPE_DESCRIPTOR, value);
    }

    public StringValue(Operation<String> operation) {
        super(TYPE_DESCRIPTOR, operation);
    }

    public StringValue(Operation<String> operation, ConstantizationValue<?>... sources) {
        super(TYPE_DESCRIPTOR, operation, sources);
    }

    @Override
    public ConstantizationValue<String> ident() {
        return new StringValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<String> merge(ConstantizationValue<String> other) {
        return new StringValue(mergeOperator(), this, other);
    }
}
