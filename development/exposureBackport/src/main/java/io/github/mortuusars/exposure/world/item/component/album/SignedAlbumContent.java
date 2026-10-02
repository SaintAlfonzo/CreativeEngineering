package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record SignedAlbumContent(String title, String author, List<SignedAlbumPage> pages) {
   public static final Codec<SignedAlbumContent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.STRING.fieldOf("title").forGetter(SignedAlbumContent::title),
               Codec.STRING.fieldOf("author").forGetter(SignedAlbumContent::author),
               BackportCodecs.sizeLimitedListOf(SignedAlbumPage.CODEC, 16).fieldOf("pages").forGetter(SignedAlbumContent::pages)
            )
            .apply(instance, SignedAlbumContent::new)
   );
   public static final StreamCodec<FriendlyByteBuf, SignedAlbumContent> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      SignedAlbumContent::title,
      ByteBufCodecs.STRING_UTF8,
      SignedAlbumContent::author,
      SignedAlbumPage.STREAM_CODEC.apply(ByteBufCodecs.list(16)),
      SignedAlbumContent::pages,
      SignedAlbumContent::new
   );
   public static final SignedAlbumContent EMPTY = new SignedAlbumContent("", "", Collections.emptyList());

   public SignedAlbumContent(String title, String author, List<SignedAlbumPage> pages) {
      Preconditions.checkArgument(pages.size() <= 16, "Too many pages for signed album. Max is 16");
      this.title = title;
      this.author = author;
      this.pages = pages;
   }
}
