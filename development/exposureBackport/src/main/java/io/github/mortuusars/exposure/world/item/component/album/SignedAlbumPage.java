package io.github.mortuusars.exposure.world.item.component.album;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record SignedAlbumPage(ItemStack photograph, Component note) {
   public static final Codec<SignedAlbumPage> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ItemStack.CODEC.optionalFieldOf("photograph", ItemStack.EMPTY).forGetter(SignedAlbumPage::photograph),
               BackportCodecs.COMPONENT.optionalFieldOf("note", Component.empty()).forGetter(SignedAlbumPage::note)
            )
            .apply(instance, SignedAlbumPage::new)
   );
   public static final StreamCodec<FriendlyByteBuf, SignedAlbumPage> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.ITEM_STACK, SignedAlbumPage::photograph, ByteBufCodecs.COMPONENT, SignedAlbumPage::note, SignedAlbumPage::new
   );
   public static final SignedAlbumPage EMPTY = new SignedAlbumPage(ItemStack.EMPTY, Component.empty());

   public boolean isEmpty() {
      return this.equals(EMPTY) || this.photograph().isEmpty() && this.note().getString().isEmpty();
   }
}
