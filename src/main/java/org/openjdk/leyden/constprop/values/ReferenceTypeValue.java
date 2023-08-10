package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.BinaryOperation;
import org.openjdk.leyden.constprop.operations.Operation;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract sealed class ReferenceTypeValue<T> extends ConstantizationValue<T>
        permits ImmutableReferenceTypeValue, MutableReferenceTypeValue {
    private static final int DEFAULT_UNION_SET_LIMIT = 5;

    private final String descriptor;

    protected ReferenceTypeValue(String descriptor) {
        this(descriptor, (T) null);
    }

    protected ReferenceTypeValue(String descriptor, T value) {
        super(value);

        this.descriptor = Objects.requireNonNull(descriptor);
    }

    protected ReferenceTypeValue(String descriptor, Operation<T> operation) {
        super(operation);

        this.descriptor = Objects.requireNonNull(descriptor);
    }

    protected ReferenceTypeValue(String descriptor, Operation<T> operation, ConstantizationValue<?>... sources) {
        super(operation, sources);

        this.descriptor = Objects.requireNonNull(descriptor);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the maximum number of elements allowed in a union set.
     * <p>
     * Most reference data types should use union set to represent multiple possible values. In events of types
     * representing continuous values (e.g., <code>java.time.Instant</code>), set this limit to <code>-1</code> to
     * disable union set and handle merging in the <code>iterator<code/> function in the subclass.
     *
     * @return the maximum number of elements allowed in a union set
     * @see #iterator()
     */
    protected int unionSetLimit() {
        return DEFAULT_UNION_SET_LIMIT;
    }

    @Override
    public Iterator<T> iterator() {
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(super.iterator(), 0), false)
                .distinct()
                .iterator();
    }

    private Collection<T> distinctValues() {
        return StreamSupport
                .stream(this.spliterator(), false)
                .distinct()
                .toList();
    }

    @Override
    public Collection<T> values() {
        Collection<T> values = distinctValues();

        if (values.size() > unionSetLimit()) {
            return Collections.emptyList();
        } else {
            return values;
        }
    }

    public String toString() {
        Collection<T> values = distinctValues();

        if (values.size() > unionSetLimit()) {
            return "(degraded)";
        }

        return values.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("(empty)");
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
                        .limit(unionSetLimit() + 1)
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

        if (obj instanceof ReferenceTypeValue<?> other) {
            Collection<?> thisValues = distinctValues();
            Collection<?> thatValues = other.distinctValues();

            if (thisValues.size() != thatValues.size()) {
                return false;
            }

            return Stream.concat(thisValues.stream(), thatValues.stream())
                    .distinct()
                    .count() == thisValues.size();
        }

        return false;
    }
}
