package io.github.mortuusars.exposure.network.packet.serverbound;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record OpenCameraAttachmentsInCreativePacketC2SP(int cameraSlotIndex) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("open_camera_attachments");
   public static final Type<OpenCameraAttachmentsInCreativePacketC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, OpenCameraAttachmentsInCreativePacketC2SP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT, OpenCameraAttachmentsInCreativePacketC2SP::cameraSlotIndex, OpenCameraAttachmentsInCreativePacketC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         serverPlayer.server.execute(() -> {
            ItemStack stack = player.getInventory().getItem(this.cameraSlotIndex);
            if (stack.getItem() instanceof CameraItem cameraItem) {
               cameraItem.openCameraAttachments(player, this.cameraSlotIndex, true);
            }
         });
      }

      return true;
   }
}
