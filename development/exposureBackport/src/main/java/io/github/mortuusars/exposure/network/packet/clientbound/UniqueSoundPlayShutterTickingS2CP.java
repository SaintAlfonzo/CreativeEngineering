package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record UniqueSoundPlayShutterTickingS2CP(int entityId, CameraId cameraId, float volume, float pitch, int durationTicks) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("unique_sound_play_shutter_ticking");
   public static final Type<UniqueSoundPlayShutterTickingS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, UniqueSoundPlayShutterTickingS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      UniqueSoundPlayShutterTickingS2CP::entityId,
      CameraId.STREAM_CODEC,
      UniqueSoundPlayShutterTickingS2CP::cameraId,
      ByteBufCodecs.FLOAT,
      UniqueSoundPlayShutterTickingS2CP::volume,
      ByteBufCodecs.FLOAT,
      UniqueSoundPlayShutterTickingS2CP::pitch,
      ByteBufCodecs.VAR_INT,
      UniqueSoundPlayShutterTickingS2CP::durationTicks,
      UniqueSoundPlayShutterTickingS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.playShutterTickingSound(this);
      return true;
   }
}
