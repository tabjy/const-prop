package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;
import org.openjdk.leyden.constprop.util.Pair;

public final class FloatValue extends ValueTypeValue<Float> {
    public static final String TYPE_DESCRIPTOR = "F";

    public FloatValue() {
        this(0f);
    }

    public FloatValue(Float value) {
        super(value);
    }

    public FloatValue(Operation<Float> operation) {
        super(operation);
    }

    public FloatValue(Operation<Float> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);
    }

    @Override
    public Pair<Float, Float> typeMinMax() {
        return Pair.of(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public String getDescriptor() {
        return TYPE_DESCRIPTOR;
    }

    @Override
    public ConstantizationValue<Float> ident() {
        return new FloatValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<Float> merge(ConstantizationValue<Float> other) {
        return new FloatValue(mergeOperator(), this, other);
    }
}
