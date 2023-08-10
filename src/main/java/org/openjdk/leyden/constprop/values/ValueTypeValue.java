package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.BinaryOperation;
import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract sealed class ValueTypeValue<T extends Comparable<T>> extends ConstantizationValue<T>
        permits DoubleValue, FloatValue, IntegerValue, LongValue {
    private static final int DEFAULT_EXPAND_LIMIT = 5;

    protected ValueTypeValue(T value) {
        super(value);

        Objects.requireNonNull(value);
    }

    protected ValueTypeValue(Operation<T> operation) {
        super(operation);
    }

    protected ValueTypeValue(Operation<T> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);
    }

    @Override
    public Iterator<T> iterator() {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(super.iterator(), 0), false)
                .distinct()
                .iterator();
    }

    public Pair<T, T> extrema() {
        List<T> values = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(super.iterator(), 0), false)
                .distinct()
                .toList();

        if (values.size() > expandLimit()) {
            return typeMinMax();
        }

        final Pair<T, T> extrema = Pair.of(null, null);
        values.stream().max(T::compareTo).ifPresent(extrema::right);
        values.stream().min(T::compareTo).ifPresent(extrema::left);

        return extrema;
    }

    public abstract Pair<T, T> typeMinMax();

    /**
     * @return the maximum number of times a max/min range is allowed to be expanded
     */
    protected int expandLimit() {
        return DEFAULT_EXPAND_LIMIT;
    }

    // TODO: clean up duplicated code
    protected BinaryOperation<T, T, T> mergeOperator() {
        return new BinaryOperation<>() {
            @Override
            public Iterator<T> applyAll(ConstantizationValue<T> operand1, ConstantizationValue<T> operand2) {
                return Stream.concat(
                                StreamSupport.stream(operand1.spliterator(), false),
                                StreamSupport.stream(operand2.spliterator(), false))
                        .distinct()
                        .limit(expandLimit() + 1)
                        .iterator();
            }

            @Override
            public String toString() {
                return "merge op";
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ValueTypeValue<?> other) {
            return extrema().equals(other.extrema());
        }

        return false;
    }
}
