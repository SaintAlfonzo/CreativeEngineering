package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureDataChangedS2CP(String id) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("exposure_data_changed");
   public static final Type<ExposureDataChangedS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ExposureDataChangedS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, ExposureDataChangedS2CP::id, ExposureDataChangedS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.exposureDataChanged(this);
      return true;
   }
}
