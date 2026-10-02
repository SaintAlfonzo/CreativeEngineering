package io.github.mortuusars.exposure.world.item.component.album;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public record AlbumContent(List<AlbumPage> pages) {
   public static final int MAX_PAGES = 16;
   public static final Codec<AlbumContent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(BackportCodecs.sizeLimitedListOf(AlbumPage.CODEC, 16).fieldOf("pages").forGetter(AlbumContent::pages)).apply(instance, AlbumContent::new)
   );
   public static final StreamCodec<FriendlyByteBuf, AlbumContent> STREAM_CODEC = StreamCodec.composite(
      AlbumPage.STREAM_CODEC.apply(ByteBufCodecs.list(16)), AlbumContent::pages, AlbumContent::new
   );
   public static final AlbumContent EMPTY = new AlbumContent(Collections.emptyList());

   public AlbumContent(List<AlbumPage> pages) {
      Preconditions.checkArgument(pages.size() <= 16, "Too many pages for album. Max is 16");
      this.pages = pages;
   }

   public Optional<AlbumPage> getPage(int index) {
      return index < this.pages.size() ? Optional.ofNullable(this.pages.get(index)) : Optional.empty();
   }

   public boolean isEmpty() {
      return this.equals(EMPTY) || this.pages.stream().allMatch(AlbumPage::isEmpty);
   }

   public AlbumContent removeTrailingPages() {
      ArrayList<AlbumPage> pages = new ArrayList<>(this.pages);

      for (int i = pages.size() - 1; i >= 0; i--) {
         AlbumPage page = pages.get(i);
         if (!page.isEmpty()) {
            break;
         }

         pages.remove(i);
      }

      return new AlbumContent(pages);
   }

   public AlbumContent.Mutable toMutable() {
      return new AlbumContent.Mutable(this);
   }

   public static class Mutable {
      private final ArrayList<AlbumPage> pages;

      public Mutable(AlbumContent content) {
         this.pages = new ArrayList<>(content.pages);

         while (this.pages.size() < 16) {
            this.pages.add(AlbumPage.EMPTY);
         }
      }

      public AlbumContent.Mutable setPage(int index, AlbumPage page) {
         Preconditions.checkElementIndex(index, 16);
         this.pages.set(index, page);
         return this;
      }

      public AlbumContent toImmutable() {
         return new AlbumContent(this.pages);
      }
   }
}
