package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Pair;

public final class IntegerValue extends ValueTypeValue<Integer> {
    public static final String TYPE_DESCRIPTOR = "I";

    public IntegerValue() {
        this(0);
    }

    public IntegerValue(Integer value) {
        super(value);
    }

    public IntegerValue(Operation<Integer> operation) {
        super(operation);
    }

    public IntegerValue(Operation<Integer> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);
    }

    @Override
    public Pair<Integer, Integer> typeMinMax() {
        return Pair.of(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public String getDescriptor() {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public ConstantizationValue<Integer> ident() {
        return new IntegerValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<Integer> merge(ConstantizationValue<Integer> other) {
        return new IntegerValue(mergeOperator(), this, other);
    }
}
