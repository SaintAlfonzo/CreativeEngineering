package io.github.mortuusars.exposure.network.packet.common;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.world.entity.CameraOperator;

public class ActiveCameraDeactivateCommonPacket implements Packet {
   public static final ActiveCameraDeactivateCommonPacket INSTANCE = new ActiveCameraDeactivateCommonPacket();
   public static final ResourceLocation ID = Exposure.resource("active_camera_deactivate");
   public static final Type<ActiveCameraDeactivateCommonPacket> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ActiveCameraDeactivateCommonPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   private ActiveCameraDeactivateCommonPacket() {
   }

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ((CameraOperator)player).getActiveExposureCameraOptional().ifPresent(camera -> {
         camera.map((item, stack) -> item.deactivate(camera.getHolder().asHolderEntity(), stack));
         ((CameraOperator)player).removeActiveExposureCamera();
      });
      return true;
   }
}
