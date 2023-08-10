package org.openjdk.leyden.constprop.values;

import org.objectweb.asm.tree.analysis.Value;
import org.openjdk.leyden.constprop.operations.NewOperation;
import org.openjdk.leyden.constprop.operations.Operation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public abstract sealed class ConstantizationValue<T> implements Value, Iterable<T>
        permits ConstantizationValue.UninitializedValue, ReferenceTypeValue, ValueTypeValue {

    protected final Operation<T> operation;
    protected final ConstantizationValue<?>[] sources;

    protected ConstantizationValue(T value) {
        this(NewOperation.from(value));
    }

    protected ConstantizationValue(Operation<T> operation) {
        this(operation, new ConstantizationValue[0]);
    }

    protected ConstantizationValue(Operation<T> operation, ConstantizationValue<?>... sources) {
        Objects.requireNonNull(operation);
        Objects.requireNonNull(sources);

        this.operation = operation;
        this.sources = sources;
    }

    public abstract String getDescriptor();

    @Override
    public Iterator<T> iterator() {
        return operation.accept(sources);
    }

    public Collection<T> values() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), 0), false)
                .toList();
    }

    public abstract ConstantizationValue<T> ident();

    public abstract ConstantizationValue<T> merge(ConstantizationValue<T> other);

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public String toString() {
        List<T> list = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), 0),
                false).toList();
        return list.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("(empty)");
    }

    public static final class UninitializedValue extends ConstantizationValue<Object> {
        public static final UninitializedValue INSTANCE = new UninitializedValue();

        private UninitializedValue() {
            super((Object) null);
        }

        @Override
        public int getSize() {
            return 1;
        }

        @Override
        public String toString() {
            return "UNINITIALIZED_VALUE";
        }

        @Override
        public String getDescriptor() {
            return ".";
        }

        @Override
        public ConstantizationValue<Object> ident() {
            return INSTANCE;
        }

        @Override
        public ConstantizationValue<Object> merge(ConstantizationValue<Object> other) {
            return INSTANCE;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == INSTANCE;
        }
    }

//    public static final class CopiedValue<T> extends ConstantizationValue<T> {
//
//        private final ConstantizationValue<T> original;
//
//        public CopiedValue(ConstantizationValue<T> value) {
//            super(UnaryOperation.identity(), value);
//
//            Objects.requireNonNull(value);
//            original = value;
//        }
//
//        @Override
//        public int getSize() {
//            return original.getSize();
//        }
//
//        @Override
//        public ConstantizationValue<T> merge(ConstantizationValue<T> other) {
//            return null;
//        }
//    }

//    public static final class MergedValues extends ConstantizationValue<Object> {
//
//        private final ConstantizationValue<?>[] values;
//
//        public MergedValues(ConstantizationValue<?>... values) {
//            super(NArayOperation.merge(values.length), values);
//
//            Objects.requireNonNull(values);
//            Arrays.stream(values).forEach(Objects::requireNonNull);
//
//            if (Arrays.stream(values).map(Value::getSize).distinct().count() > 1) {
//                throw new IllegalArgumentException("All values must have the same size");
//            }
//
//            this.values = values;
//        }
//
//        @Override
//        public int getSize() {
//            return values[0].getSize();
//        }
//    }
}
