package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record CameraStandStopControllingS2CP(int standId) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("camera_stand_stop_controlling");
   public static final Type<CameraStandStopControllingS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, CameraStandStopControllingS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, CameraStandStopControllingS2CP::standId, CameraStandStopControllingS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.stopControllingCameraStand(this);
      return true;
   }
}
