package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Pair;

public final class DoubleValue extends ValueTypeValue<Double> {
    public static final String TYPE_DESCRIPTOR = "D";

    public DoubleValue() {
        this(0d);
    }

    public DoubleValue(Double value) {
        super(value);
    }

    public DoubleValue(Operation<Double> operation) {
        super(operation);
    }

    public DoubleValue(Operation<Double> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);
    }

    @Override
    public Pair<Double, Double> typeMinMax() {
        return Pair.of(Double.MIN_VALUE, Double.MAX_VALUE);
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
    public ConstantizationValue<Double> ident() {
        return new DoubleValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<Double> merge(ConstantizationValue<Double> other) {
        return new DoubleValue(mergeOperator(), this, other);
    }
}
