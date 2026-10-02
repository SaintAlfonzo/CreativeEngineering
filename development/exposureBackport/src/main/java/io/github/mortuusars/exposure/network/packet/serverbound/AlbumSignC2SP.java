package io.github.mortuusars.exposure.network.packet.serverbound;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.network.packet.Packet;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload;
import io.github.mortuusars.exposure.backport.network.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AlbumSignC2SP(int slot, String title, String author) implements Packet {
   public static final ResourceLocation ID = Exposure.resource("album_sign");
   public static final Type<AlbumSignC2SP> TYPE = new Type(ID);
   public static final StreamCodec<FriendlyByteBuf, AlbumSignC2SP> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      AlbumSignC2SP::slot,
      ByteBufCodecs.STRING_UTF8,
      AlbumSignC2SP::title,
      ByteBufCodecs.STRING_UTF8,
      AlbumSignC2SP::author,
      AlbumSignC2SP::new
   );

   @NotNull
   public Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }

   @Override
   public boolean handle(PacketFlow flow, Player player) {
      Preconditions.checkState(player != null, "Cannot handle packet: Player was null");
      ItemStack albumStack = player.getInventory().getItem(this.slot());
      if (albumStack.getItem() instanceof AlbumItem albumItem) {
         ItemStack signedAlbumStack = albumItem.sign(albumStack, this.title(), this.author());
         player.getInventory().setItem(this.slot(), signedAlbumStack);
         player.level.playSound(null, player, Exposure.SoundEvents.WRITE.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
      }

      return true;
   }
}
