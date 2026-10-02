package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.server.CameraInstances;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.world.camera.CameraId;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record InterplanarProjectionFinishedC2SP(CameraId cameraId, boolean successful, Optional<TranslatableError> error) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("interplanar_projection_finished");
   public static final Type<InterplanarProjectionFinishedC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, InterplanarProjectionFinishedC2SP> STREAM_CODEC = StreamCodec.composite(
      CameraId.STREAM_CODEC,
      InterplanarProjectionFinishedC2SP::cameraId,
      ByteBufCodecs.BOOL,
      InterplanarProjectionFinishedC2SP::successful,
      ByteBufCodecs.optional(TranslatableError.STREAM_CODEC),
      InterplanarProjectionFinishedC2SP::error,
      InterplanarProjectionFinishedC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      CameraInstances.ifPresent(this.cameraId, cameraInstance -> cameraInstance.setProjectionResult(player.level, this.successful, this.error));
      return true;
   }
}
