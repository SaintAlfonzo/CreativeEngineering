package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.capture.CaptureParameters;
import java.util.List;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import net.minecraft.network.FriendlyByteBuf;

public record CaptureStartDebugRGBS2CP(ResourceLocation templateId, List<CaptureParameters> captureProperties) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("capture_start_debug_rgb");
   public static final Type<CaptureStartDebugRGBS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, CaptureStartDebugRGBS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.RESOURCE_LOCATION,
      CaptureStartDebugRGBS2CP::templateId,
      CaptureParameters.STREAM_CODEC.apply(ByteBufCodecs.list(3)),
      CaptureStartDebugRGBS2CP::captureProperties,
      CaptureStartDebugRGBS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.startDebugRGBCapture(this);
      return true;
   }
}
