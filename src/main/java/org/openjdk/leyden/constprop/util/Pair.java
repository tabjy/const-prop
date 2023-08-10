package org.openjdk.leyden.constprop.util;

import java.util.Objects;

public final class Pair<T, R> {
    private T left;
    private R right;

    public Pair() {
    }

    private Pair(T left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <T, R> Pair<T, R> of(T left, R right) {
        return new Pair<>(left, right);
    }

    public static <T, R> Pair<T, R> of() {
        return new Pair<>(null, null);
    }

    public T left() {
        return left;
    }

    public void left(T left) {
        this.left = left;
    }

    public R right() {
        return right;
    }

    public void right(R right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Pair<?, ?> other) {
            return Objects.equals(left, other.left) && Objects.equals(right, other.right);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
