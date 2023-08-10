package org.openjdk.leyden.constprop.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A three-state monadic wrapper representing a possibly unknown and nullable value. Similar to
 * {@link java.util.Optional}.
 *
 * @param <T> the type of the value
 */
public class Unknowable<T> {

    private static final Unknowable<?> UNKNOWN = new Unknowable<>(null);
    private static final Unknowable<?> NULL = new Unknowable<>(null);

    private final T value;

    public static <T> Unknowable<T> unknown() {
        @SuppressWarnings("unchecked")
        Unknowable<T> t = (Unknowable<T>) UNKNOWN;
        return t;
    }

    /**
     * the null value constant, <code>nil</code> since <code>null</code> is a keyword.
     *
     * @return the null value constant
     * @param <T> the type of the value
     */
    public static <T> Unknowable<T> nil() {
        @SuppressWarnings("unchecked")
        Unknowable<T> t = (Unknowable<T>) NULL;
        return t;
    }

    public static <T> Unknowable<T> of(T value) {
        return new Unknowable<>(Objects.requireNonNull(value));
    }

    public static <T> Unknowable<T> ofNullable(T value) {
        return value == null ? nil() : of(value);
    }

    private Unknowable(T value) {
        this.value = value;
    }

    public T get() {
        if (this == UNKNOWN) {
            throw new NoSuchElementException("Unknown value");
        }

        if (this == NULL) {
            return null;
        }

        return value;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isNonNull() {
        return this != NULL;
    }
}
