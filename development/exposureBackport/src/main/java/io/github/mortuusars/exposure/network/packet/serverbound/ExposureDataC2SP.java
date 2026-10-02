package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureServer;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.level.storage.ExposureData;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ExposureDataC2SP(String id, ExposureData exposure) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("exposure_data");
   public static final Type<ExposureDataC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ExposureDataC2SP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, ExposureDataC2SP::id, ExposureData.STREAM_CODEC, ExposureDataC2SP::exposure, ExposureDataC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ExposureServer.exposureRepository().receiveClientUpload((ServerPlayer)player, this.id, this.exposure);
      return true;
   }
}
