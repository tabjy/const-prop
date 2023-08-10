package org.openjdk.leyden.constprop.values;

import org.openjdk.leyden.constprop.operations.Operation;
import org.openjdk.leyden.constprop.operations.UnaryOperation;

import java.lang.constant.ClassDesc;

public final class ClassValue extends ImmutableReferenceTypeValue<ClassDesc> {
    public static final String TYPE_DESCRIPTOR = "Ljava/lang/Class;";

    public ClassValue() {
        super(TYPE_DESCRIPTOR);
    }

    public ClassValue(ClassDesc value) {
        super(TYPE_DESCRIPTOR, value);
    }

    public ClassValue(Operation<ClassDesc> operation) {
        super(TYPE_DESCRIPTOR, operation);
    }

    public ClassValue(Operation<ClassDesc> operation, ConstantizationValue<?>... sources) {
        super(TYPE_DESCRIPTOR, operation, sources);
    }

    @Override
    public ConstantizationValue<ClassDesc> ident() {
        return new ClassValue(UnaryOperation.identity(), this);
    }

    @Override
    public ConstantizationValue<ClassDesc> merge(ConstantizationValue<ClassDesc> other) {
        return new ClassValue(mergeOperator(), this, other);
    }
}
