package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record AlbumPage(ItemStack photograph, String note) {
   public static final Codec<AlbumPage> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ItemStack.CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(AlbumPage::photograph),
               BackportCodecs.string(0, 512).optionalFieldOf("note", "").forGetter(AlbumPage::note)
            )
            .apply(instance, AlbumPage::new)
   );
   public static final StreamCodec<FriendlyByteBuf, AlbumPage> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.ITEM_STACK, AlbumPage::photograph, ByteBufCodecs.STRING_UTF8, AlbumPage::note, AlbumPage::new
   );
   public static final AlbumPage EMPTY = new AlbumPage(ItemStack.EMPTY, "");

   public boolean isEmpty() {
      return this.photograph().isEmpty() && this.note().isEmpty();
   }

   public AlbumPage setPhotograph(ItemStack stack) {
      return new AlbumPage(stack, this.note);
   }

   public AlbumPage setNote(String note) {
      return new AlbumPage(this.photograph, note);
   }

   public SignedAlbumPage convertToSigned() {
      return new SignedAlbumPage(this.photograph, Component.literal(this.note));
   }
}
