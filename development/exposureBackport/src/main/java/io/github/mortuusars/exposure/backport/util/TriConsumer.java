package io.github.mortuusars.exposure.backport.util;

/** commons-lang3 3.13+ {@code org.apache.commons.lang3.function.TriConsumer} (1.19.2 ships 3.12). */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}
