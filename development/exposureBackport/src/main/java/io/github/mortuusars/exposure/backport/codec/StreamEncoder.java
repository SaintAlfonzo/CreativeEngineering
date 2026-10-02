package io.github.mortuusars.exposure.backport.codec;

/** Backport of 1.20.5+ {@code net.minecraft.network.codec.StreamEncoder}. */
@FunctionalInterface
public interface StreamEncoder<O, T> {
    void encode(O buffer, T value);
}
