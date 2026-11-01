package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.client.network.BnCClientPacketHandler;
import umpaz.brewinandchewin.common.network.BnCPacket;

public record ClearKegFluidContainerComponentsClientboundPacket() implements BnCPacket {
   public ClearKegFluidContainerComponentsClientboundPacket(FriendlyByteBuf buf) {
      this();
   }

   @Override
   public void encode(FriendlyByteBuf buf) {
   }

   @Override
   public void handle(@Nullable ServerPlayer sender) {
      BnCClientPacketHandler.clearKegFluidContainerComponents();
   }
}
