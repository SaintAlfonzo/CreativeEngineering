package io.github.mortuusars.exposure.network.packet.clientbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.handler.ClientPacketsHandler;
import io.github.mortuusars.exposure.network.packet.Packet;
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

public record ShaderApplyS2CP(Optional<ResourceLocation> shaderLocation) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("shader_apply");
   public static final Type<ShaderApplyS2CP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, ShaderApplyS2CP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.optional(ByteBufCodecs.RESOURCE_LOCATION), ShaderApplyS2CP::shaderLocation, ShaderApplyS2CP::new
   );
   public static final ShaderApplyS2CP REMOVE = new ShaderApplyS2CP(Optional.empty());

   public ShaderApplyS2CP(ResourceLocation location) {
      this(Optional.of(location));
   }

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow direction, Player player) {
      ClientPacketsHandler.applyShader(this);
      return true;
   }
}
