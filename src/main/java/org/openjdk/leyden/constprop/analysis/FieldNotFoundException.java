package org.openjdk.leyden.constprop.analysis;

public class FieldNotFoundException extends Exception {
    public FieldNotFoundException() {
        super((Throwable) null);
    }

    public FieldNotFoundException(String s) {
        super(s, null);
    }

    public FieldNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public Throwable getException() {
        return super.getCause();
    }
}
