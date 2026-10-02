package io.github.mortuusars.exposure.backport.codec;

/** Backport of 1.20.5+ {@code net.minecraft.network.codec.StreamMemberEncoder}: value-first encoder. */
@FunctionalInterface
public interface StreamMemberEncoder<O, T> {
    void encode(T value, O buffer);
}
