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

public class ShutterOpenedS2CP implements Packet {
   public static final ShutterOpenedS2CP INSTANCE = new ShutterOpenedS2CP();
   public static final ResourceLocation ID = Exposure.resource("shutter_opened");
   public static final Type<ShutterOpenedS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ShutterOpenedS2CP> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   private ShutterOpenedS2CP() {
   }

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      ClientPacketsHandler.shutterOpened();
      return true;
   }
}
