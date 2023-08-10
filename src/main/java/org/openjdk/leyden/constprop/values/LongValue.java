package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Pair;

public final class LongValue extends ValueTypeValue<Long> {
    public static final String TYPE_DESCRIPTOR = "J";

    public LongValue() {
        this(0L);
    }

    public LongValue(Long value) {
        super(value);
    }

    public LongValue(Operation<Long> operation) {
        super(operation);
    }

    public LongValue(Operation<Long> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);
    }

    @Override
    public Pair<Long, Long> typeMinMax() {
        return Pair.of(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public String getDescriptor() {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public ConstantizationValue<Long> ident() {
        return new LongValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<Long> merge(ConstantizationValue<Long> other) {
        return new LongValue(mergeOperator(), this, other);
    }
}
