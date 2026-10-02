package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
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

public record CreateChromaticExposureS2CP(String id, List<ExposureIdentifier> layers) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("create_chromatic_exposure");
   public static final Type<CreateChromaticExposureS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, CreateChromaticExposureS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CreateChromaticExposureS2CP::id,
      ExposureIdentifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
      CreateChromaticExposureS2CP::layers,
      CreateChromaticExposureS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.createChromaticExposure(this);
      return true;
   }
}
