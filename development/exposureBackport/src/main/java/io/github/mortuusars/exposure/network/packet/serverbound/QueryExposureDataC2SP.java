package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.level.storage.ExposureIdentifier;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record QueryExposureDataC2SP(ExposureIdentifier identifier) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("query_exposure_data");
   public static final Type<QueryExposureDataC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, QueryExposureDataC2SP> STREAM_CODEC = StreamCodec.composite(
      ExposureIdentifier.STREAM_CODEC, QueryExposureDataC2SP::identifier, QueryExposureDataC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      Preconditions.checkArgument(player instanceof ServerPlayer, "Cannot handle packet: Player was is not available.");
      return true;
   }
}
