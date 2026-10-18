package umpaz.brewinandchewin.common.network.clientbound;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.client.network.BnCClientPacketHandler;
import umpaz.brewinandchewin.common.network.BnCPacket;

public record SyncRagingStacksClientboundPacket(int entityId, Optional<Integer> stacks) implements BnCPacket {
   public SyncRagingStacksClientboundPacket(FriendlyByteBuf buf) {
      this(buf.readInt(), buf.readOptional(FriendlyByteBuf::readInt));
   }

   @Override
   public void encode(FriendlyByteBuf buf) {
      buf.writeInt(this.entityId);
      buf.writeOptional(this.stacks, FriendlyByteBuf::writeInt);
   }

   @Override
   public void handle(@Nullable ServerPlayer sender) {
      BnCClientPacketHandler.syncRagingStacks(this);
   }
}
