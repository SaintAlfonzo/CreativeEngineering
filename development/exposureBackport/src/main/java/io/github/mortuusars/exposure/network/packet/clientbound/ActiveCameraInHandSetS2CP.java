package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record ActiveCameraInHandSetS2CP(int operatorEntityId, CameraId cameraId, InteractionHand hand) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("active_camera_in_hand_set");
   public static final Type<ActiveCameraInHandSetS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ActiveCameraInHandSetS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ActiveCameraInHandSetS2CP::operatorEntityId,
      CameraId.STREAM_CODEC,
      ActiveCameraInHandSetS2CP::cameraId,
      ByteBufCodecs.VAR_INT.map(i -> InteractionHand.values()[i], Enum::ordinal),
      ActiveCameraInHandSetS2CP::hand,
      ActiveCameraInHandSetS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      if (player.level.getEntity(this.operatorEntityId) instanceof LivingEntity entity
         && entity instanceof CameraOperator operator
         && entity instanceof CameraHolder holder) {
         ((CameraOperator)operator).setActiveExposureCamera(new CameraInHand(holder, this.cameraId, this.hand));
      }

      return true;
   }
}
