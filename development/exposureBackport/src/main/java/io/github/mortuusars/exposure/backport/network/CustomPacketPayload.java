package io.github.mortuusars.exposure.backport.network;

import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Backport of 1.20.5+ {@code CustomPacketPayload}: a packet identified by {@link Type} and written by a
 * {@link StreamCodec}. {@link io.github.mortuusars.exposure.network.Packets} carries these over one Forge
 * SimpleChannel, prefixed with the type id.
 */
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {
    }

    record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(Type<T> type, StreamCodec<B, T> codec) {
    }
}
