package org.openjdk.leyden.constprop.operations;

import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Iterator;

public abstract class UnaryOperation<T, R> extends NArayOperation<R> {

    public static <U> UnaryOperation<U, U> identity() {
        return new UnaryOperation<>() {
            @Override
            public Unknowable<U> applyPartial(Unknowable<U> operand) {
                return operand;
            }

            @Override
            public String toString() {
                return "ident op";
            }
        };
    }

    public UnaryOperation() {
        super(1);
    }

    @Override
    public Iterator<R> applyAll(ConstantizationValue<?>... operands) {
        @SuppressWarnings("unchecked")
        ConstantizationValue<T> operand = (ConstantizationValue<T>) operands[0];

        return this.applyAll(operand);
    }

    public Iterator<R> applyAll(ConstantizationValue<T> operand) {
        return super.applyAll(operand);
    }

    @Override
    public Unknowable<R> applyPartial(Unknowable<?>... operands) {
        @SuppressWarnings("unchecked")
        Unknowable<T> operand = (Unknowable<T>) operands[0];

        return this.applyPartial(operand);
    }

    public Unknowable<R> applyPartial(Unknowable<T> operand) {
        return super.applyPartial(operand);
    }

    @Override
    public Unknowable<R> apply(Object... operands) {
        @SuppressWarnings("unchecked")
        T operand = (T) operands[0];

        return this.apply(operand);
    }

    public Unknowable<R> apply(T operand) {
        return super.apply(operand);
    }
}
