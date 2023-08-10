package org.openjdk.leyden.constprop.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ClassLoaderClassPool extends AbstractClassPool {
    private final ClassLoader classLoader;

    public ClassLoaderClassPool() {
        this(ClassLoaderClassPool.class.getClassLoader());
    }

    public ClassLoaderClassPool(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public byte[] classForName(String name) throws ClassNotFoundException {
        try (InputStream is = classLoader.getResourceAsStream(name + ".class")) {
            return Objects.requireNonNull(is).readAllBytes();
        } catch (IOException | IllegalStateException e) {
            throw new ClassNotFoundException("class " + name + " cannot be found", e);
        }
    }
}
