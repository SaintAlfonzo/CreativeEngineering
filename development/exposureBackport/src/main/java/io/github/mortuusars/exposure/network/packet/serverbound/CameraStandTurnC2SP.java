package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record CameraStandTurnC2SP(int entityId, double yRot, double xRot) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("camera_stand_turn");
   public static final Type<CameraStandTurnC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, CameraStandTurnC2SP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      CameraStandTurnC2SP::entityId,
      ByteBufCodecs.DOUBLE,
      CameraStandTurnC2SP::yRot,
      ByteBufCodecs.DOUBLE,
      CameraStandTurnC2SP::xRot,
      CameraStandTurnC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow direction, Player player) {
      if (!(player.level.getEntity(this.entityId) instanceof CameraStandEntity stand)) {
         return false;
      } else {
         if (player.equals(stand.operator()) || stand.getOwnerPlayer().map(pl -> pl.equals(player)).orElse(false)) {
            stand.turn(this.yRot, this.xRot);
         }

         return true;
      }
   }
}
