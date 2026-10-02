package io.github.mortuusars.exposure.backport.codec;

/** Backport of 1.20.5+ {@code net.minecraft.network.codec.StreamDecoder}. */
@FunctionalInterface
public interface StreamDecoder<I, T> {
    T decode(I buffer);
}
