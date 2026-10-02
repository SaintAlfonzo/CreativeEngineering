package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.export.ExportLook;
import io.github.mortuusars.exposure.data.export.ExportSize;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
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

public record ExportS2CP(List<String> ids, ExportSize size, ExportLook look) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("export");
   public static final Type<ExportS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ExportS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      ExportS2CP::ids,
      ByteBufCodecs.VAR_INT.map(i -> ExportSize.values()[i], Enum::ordinal),
      ExportS2CP::size,
      ByteBufCodecs.VAR_INT.map(i -> ExportLook.values()[i], Enum::ordinal),
      ExportS2CP::look,
      ExportS2CP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow direction, Player player) {
      ClientPacketsHandler.exportExposures(this);
      return true;
   }
}
