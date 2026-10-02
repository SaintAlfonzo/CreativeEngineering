package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CameraStandSetRotationsS2CP(int entityId, float yRot, float xRot) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("camera_stand_set_rotations");
   public static final Type<CameraStandSetRotationsS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, CameraStandSetRotationsS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      CameraStandSetRotationsS2CP::entityId,
      ByteBufCodecs.FLOAT,
      CameraStandSetRotationsS2CP::yRot,
      ByteBufCodecs.FLOAT,
      CameraStandSetRotationsS2CP::xRot,
      CameraStandSetRotationsS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow direction, Player player) {
      if (player.level.getEntity(this.entityId) instanceof CameraStandEntity stand) {
         stand.setYRot(this.yRot);
         stand.setXRot(this.xRot);
      }

      return true;
   }
}
