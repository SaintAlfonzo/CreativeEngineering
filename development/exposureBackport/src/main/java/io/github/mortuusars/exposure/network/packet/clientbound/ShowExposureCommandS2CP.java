package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ShowExposureCommandS2CP(List<Frame> frames, boolean negative) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("show_exposure_command");
   public static final Type<ShowExposureCommandS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ShowExposureCommandS2CP> STREAM_CODEC = StreamCodec.composite(
      Frame.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ShowExposureCommandS2CP::frames,
      ByteBufCodecs.BOOL,
      ShowExposureCommandS2CP::negative,
      ShowExposureCommandS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   public static ShowExposureCommandS2CP identifier(ExposureIdentifier identifier, boolean negative) {
      Frame frame = Frame.create().setIdentifier(identifier).toImmutable();
      return new ShowExposureCommandS2CP(List.of(frame), negative);
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.showExposure(this);
      return true;
   }
}
