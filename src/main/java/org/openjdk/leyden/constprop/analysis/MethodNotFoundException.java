package org.openjdk.leyden.constprop.analysis;

public class MethodNotFoundException extends Exception {
    public MethodNotFoundException() {
        super((Throwable) null);
    }

    public MethodNotFoundException(String s) {
        super(s, null);
    }

    public MethodNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public Throwable getException() {
        return super.getCause();
    }
}
