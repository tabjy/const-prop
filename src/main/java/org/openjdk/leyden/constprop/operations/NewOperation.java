package org.openjdk.leyden.constprop.operations;


import org.openjdk.leyden.constprop.util.Unknowable;
import org.openjdk.leyden.constprop.values.ConstantizationValue;

import java.util.Collections;
import java.util.Iterator;

public abstract class NewOperation<R> extends NArayOperation<R> {

    public static <U> NewOperation<U> unknown() {
        return new NewOperation<U>() {
            @Override
            public Unknowable<U> apply() {
                return Unknowable.unknown();
            }

            @Override
            public String toString() {
                return "unknown op";
            }
        };
    }

    public static <U> NewOperation<U> from(U value) {
        return new NewOperation<U>() {
            @Override
            public Unknowable<U> apply() {
                return Unknowable.ofNullable(value);
            }

            @Override
            public String toString() {
                return String.format("from op (%s)", value);
            }
        };
    }

    public NewOperation() {
        super(0);
    }

    @Override
    public Iterator<R> applyAll(ConstantizationValue<?>... operands) {
        return this.applyAll();
    }

    public Iterator<R> applyAll() {
        Unknowable<R> result = this.apply();
        if (result.isKnown()) {
            return Collections.singleton(result.get()).iterator();
        }

        return Collections.emptyIterator();
    }

    @Override
    public final Unknowable<R> applyPartial(Unknowable<?>... operands) {
        throw new UnsupportedOperationException("applyPartial() not supported for NewOperation");
    }

    @Override
    public Unknowable<R> apply(Object... operands) {
        return this.apply();
    }


    public Unknowable<R> apply() {
        return super.apply();
    }
}
