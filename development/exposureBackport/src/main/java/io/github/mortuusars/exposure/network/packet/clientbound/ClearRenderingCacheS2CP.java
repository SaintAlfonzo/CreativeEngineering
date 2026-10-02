package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClearRenderingCacheS2CP implements Packet {
   public static final ClearRenderingCacheS2CP INSTANCE = new ClearRenderingCacheS2CP();
   public static final ResourceLocation ID = Exposure.resource("clear_rendering_cache");
   public static final Type<ClearRenderingCacheS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ClearRenderingCacheS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   private ClearRenderingCacheS2CP() {
   }

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.clearRenderingCache();
      return true;
   }
}
