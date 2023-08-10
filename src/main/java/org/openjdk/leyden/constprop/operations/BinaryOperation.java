package org.openjdk.leyden.constprop.operations;

import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Iterator;

public abstract class BinaryOperation<T, U, R> extends NArayOperation<R> {

    public BinaryOperation() {
        super(2);
    }

    @Override
    public Iterator<R> applyAll(ConstantizationValue<?>... operands) {
        @SuppressWarnings("unchecked")
        ConstantizationValue<T> operand1 = (ConstantizationValue<T>) operands[0];

        @SuppressWarnings("unchecked")
        ConstantizationValue<U> operand2 = (ConstantizationValue<U>) operands[1];

        return this.applyAll(operand1, operand2);
    }

    public Iterator<R> applyAll(ConstantizationValue<T> operand1, ConstantizationValue<U> operand2) {
        return super.applyAll(operand1, operand2);
    }

    @Override
    public Unknowable<R> applyPartial(Unknowable<?>... operands) {
        @SuppressWarnings("unchecked")
        Unknowable<T> operand1 = (Unknowable<T>) operands[0];

        @SuppressWarnings("unchecked")
        Unknowable<U> operand2 = (Unknowable<U>) operands[1];

        return this.applyPartial(operand1, operand2);
    }

    public Unknowable<R> applyPartial(Unknowable<T> operand1, Unknowable<U> operand2) {
        return super.applyPartial(operand1, operand2);
    }

    @Override
    public Unknowable<R> apply(Object... operands) {
        @SuppressWarnings("unchecked")
        T operand1 = (T) operands[0];

        @SuppressWarnings("unchecked")
        U operand2 = (U) operands[1];

        return this.apply(operand1, operand2);
    }

    public Unknowable<R> apply(T operand1, U operand2) {
        return super.apply(operand1, operand2);
    }
}
