package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.descriptors.BasicMutableObjectDescriptor;
import org.openjdk.leyden.constprop.operations.Operation;

import java.lang.constant.ClassDesc;

public class DefaultConstantizationValueFactory extends AbstractConstantizationValueFactory {
    @Override
    public <V, T extends ConstantizationValue<V>> T createValue(String descriptor) {
        if (descriptor.startsWith("[")) {
            // TODO: implement array value
            throw new UnsupportedOperationException("Array value is not supported yet");
        }

        switch (descriptor) {
            case IntegerValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T intValue = (T) new IntegerValue();
                return intValue;
            }
            case FloatValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T floatValue = (T) new FloatValue();
                return floatValue;
            }
            case LongValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T longValue = (T) new LongValue();
                return longValue;
            }
            case DoubleValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T doubleValue = (T) new DoubleValue();
                return doubleValue;
            }
            case StringValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T stringValue = (T) new StringValue();
                return stringValue;
            }
            case ClassValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T classValue = (T) new ClassValue();
                return classValue;
            }
            case ObjectValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T objectValue = (T) new ObjectValue();
                return objectValue;
            }

            // TODO: immutable classes should be registered
        }

        if (descriptor.startsWith("L")) {
            @SuppressWarnings("unchecked")
            // TODO
            T referenceValue = (T) new MutableReferenceTypeValue<BasicMutableObjectDescriptor>(descriptor);
            return referenceValue;
        }

        throw new IllegalArgumentException("Unsupported descriptor: " + descriptor);
    }

    @Override
    public <V, T extends ConstantizationValue<V>> T createValue(String descriptor,
                                                                Operation<V> operation,
                                                                ConstantizationValue<?>... sources) {
        if (descriptor.startsWith("[")) {
            // TODO: implement array value
            throw new UnsupportedOperationException("Array value is not supported yet");
        }

        switch (descriptor) {
            case IntegerValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T intValue = (T) new IntegerValue((Operation<Integer>) operation, sources);
                return intValue;
            }
            case FloatValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T floatValue = (T) new FloatValue((Operation<Float>) operation, sources);
                return floatValue;
            }
            case LongValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T longValue = (T) new LongValue((Operation<Long>) operation, sources);
                return longValue;
            }
            case DoubleValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T doubleValue = (T) new DoubleValue((Operation<Double>) operation, sources);
                return doubleValue;
            }
            case StringValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T stringValue = (T) new StringValue((Operation<String>) operation, sources);
                return stringValue;
            }
            case ClassValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T classValue = (T) new ClassValue((Operation<ClassDesc>) operation, sources);
                return classValue;
            }
            case ObjectValue.TYPE_DESCRIPTOR -> {
                @SuppressWarnings("unchecked")
                T objectValue = (T) new ObjectValue((Operation<ObjectValue.UnusedDesc>) operation, sources);
                return objectValue;
            }
        }

        if (descriptor.startsWith("L")) {
            @SuppressWarnings("unchecked")
            // TODO
            T referenceValue = (T) new MutableReferenceTypeValue<>(descriptor, (Operation<BasicMutableObjectDescriptor>) operation, sources);
            return referenceValue;
        }

        throw new IllegalArgumentException("Unsupported descriptor: " + descriptor);
    }
}
