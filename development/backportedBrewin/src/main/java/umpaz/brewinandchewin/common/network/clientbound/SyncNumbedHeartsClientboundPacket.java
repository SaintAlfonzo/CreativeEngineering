package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.client.network.BnCClientPacketHandler;
import umpaz.brewinandchewin.common.network.BnCPacket;

public record SyncNumbedHeartsClientboundPacket(int entityId, float numbedHealth, int ticksUntilDamage) implements BnCPacket {
   public SyncNumbedHeartsClientboundPacket(FriendlyByteBuf buf) {
      this(buf.readInt(), buf.readFloat(), buf.readInt());
   }

   @Override
   public void encode(FriendlyByteBuf buf) {
      buf.writeInt(this.entityId);
      buf.writeFloat(this.numbedHealth);
      buf.writeInt(this.ticksUntilDamage);
   }

   @Override
   public void handle(@Nullable ServerPlayer sender) {
      BnCClientPacketHandler.syncNumbedHearts(this);
   }
}
