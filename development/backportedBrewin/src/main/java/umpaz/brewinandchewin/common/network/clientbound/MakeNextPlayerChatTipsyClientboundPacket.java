package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.client.network.BnCClientPacketHandler;
import umpaz.brewinandchewin.common.network.BnCPacket;

/**
 * Sent to every receiver just before a Tipsy player's chat or /msg goes out. Port (alcohol.md 4.3): `level` is the
 * sender's Tipsy amplifier whatever it is; each client compares it with its own levelChatScramble setting.
 */
public record MakeNextPlayerChatTipsyClientboundPacket(int level, long randomSeed, int clearDelayAmount) implements BnCPacket {
   public MakeNextPlayerChatTipsyClientboundPacket(FriendlyByteBuf buf) {
      this(buf.readInt(), buf.readLong(), buf.readInt());
   }

   @Override
   public void encode(FriendlyByteBuf buf) {
      buf.writeInt(this.level);
      buf.writeLong(this.randomSeed);
      buf.writeInt(this.clearDelayAmount);
   }

   @Override
   public void handle(@Nullable ServerPlayer sender) {
      BnCClientPacketHandler.makeNextPlayerChatTipsy(this);
   }
}
