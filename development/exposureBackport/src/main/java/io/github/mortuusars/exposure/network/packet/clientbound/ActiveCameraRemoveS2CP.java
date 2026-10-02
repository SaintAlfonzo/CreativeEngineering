package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ActiveCameraRemoveS2CP(int operatorEntityId) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("active_camera_remove");
   public static final Type<ActiveCameraRemoveS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ActiveCameraRemoveS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, ActiveCameraRemoveS2CP::operatorEntityId, ActiveCameraRemoveS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      if (player.level.getEntity(this.operatorEntityId) instanceof CameraOperator operator) {
         ((CameraOperator)operator).removeActiveExposureCamera();
      }

      return true;
   }
}
