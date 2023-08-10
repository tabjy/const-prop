package org.openjdk.leyden.constprop.operations;

import org.openjdk.leyden.constprop.util.CartesianProduct;
import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract non-sealed class NArayOperation<R> extends Operation<R> {

    private final int n;

    public NArayOperation(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Operation expects a non-negative number of operands");
        }

        this.n = n;
    }

    public Iterator<R> accept(ConstantizationValue<?>[] sources) {
        Objects.requireNonNull(sources);
        if (sources.length != n) {
            throw new IllegalArgumentException(String.format("Operation expects exactly %d operands", n));
        }

        ConstantizationValue<?>[] operands = Arrays.copyOf(sources, n);
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(applyAll(operands), 0), false)
                .distinct()
                .iterator();
    }

    /**
     * apply the operation with inputs being all possible values of a given source at once. This function is called
     * exactly once.
     *
     * @param operands an array of collections of all possible values of each operand
     * @return all possible results
     */
    public Iterator<R> applyAll(ConstantizationValue<?>... operands) {
        List<?>[] lists = (List<?>[]) Arrays.stream(operands)
                .map(operand -> {
                    List<? extends Unknowable<?>> list = StreamSupport.stream(operand.spliterator(), false)
                            .distinct()
                            .map(Unknowable::ofNullable)
                            .toList();
                    if (list.isEmpty()) {
                        list = List.of(Unknowable.unknown());
                    }

                    return list;
                })
                .toArray(List[]::new);

        @SuppressWarnings("unchecked")
        List<List<Unknowable<?>>> combinations = CartesianProduct.of((List<Unknowable<?>>[]) lists);

        return combinations.stream().map(combination -> applyPartial(combination.toArray(Unknowable[]::new)))
                .filter(Unknowable::isKnown)
                .map(Unknowable::get)
                .distinct()
                .iterator();
    }

    /**
     * apply the operation with inputs being all possible values of a given source at one. The difference between this
     * function and {@link NArayOperation#apply(Object...)} is that an operand could potentially be unknown (from
     * constant propagation). Users must check and wrap the monadic type if necessary. This function is called as many
     * times as the cardinality of the operand cartesian set.
     *
     * @param operands an array possible values of each operand
     * @return one result of the operation
     * @see Unknowable
     */
    public Unknowable<R> applyPartial(Unknowable<?>... operands) {
        if (Stream.of(operands).allMatch(Unknowable::isKnown)) {
            return apply(Stream.of(operands).map(Unknowable::get).toArray());
        }

        return Unknowable.unknown();
    }

    /**
     * apply the operation with inputs being each possible values from a given source. Cartesian products are used to
     * break possible operand values into combinations. This function is called as many times as the cardinality of the
     * operand cartesian set.
     *
     * @param operands each possible non-null value from each operand
     * @return one result of the operation, with null pointers indicating the result is really a null instead of unknown
     */
    public Unknowable<R> apply(Object... operands) {
        throw new UnsupportedOperationException(
                String.format("Override either %s#applyAll, %s#applyPartial or %s#apply",
                        this.getClass().getSimpleName(), this.getClass().getSimpleName(), this.getClass().getSimpleName()
                ));
    }
}
