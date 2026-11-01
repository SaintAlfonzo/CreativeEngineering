package umpaz.brewinandchewin.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Port: 1.21 CustomPacketPayload + StreamCodec -> one interface for Forge's SimpleChannel (forge/network/BnCNetwork).
 * Each packet has a FriendlyByteBuf constructor for decoding. Clientbound packets hand off to
 * client/network/BnCClientPacketHandler, so the dedicated server never loads client classes.
 */
public interface BnCPacket {
   void encode(FriendlyByteBuf buf);

   /** Runs on the receiving side's main thread; `sender` is the player for serverbound packets, else null. */
   void handle(@Nullable ServerPlayer sender);
}
