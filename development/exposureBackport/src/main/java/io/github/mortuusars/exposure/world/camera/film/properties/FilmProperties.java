package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.data.ColorPalette;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.capture.DitherMode;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public record FilmProperties(ExposureType type, Optional<Integer> size, ResourceKey<ColorPalette> colorPalette, DitherMode ditherMode, FilmStyle style) {
   public static final Codec<FilmProperties> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ExposureType.CODEC.optionalFieldOf("type", ExposureType.COLOR).forGetter(FilmProperties::type),
               Codec.intRange(0, 2048).optionalFieldOf("frame_size").forGetter(FilmProperties::size),
               ResourceKey.codec(Exposure.Registries.COLOR_PALETTE)
                  .optionalFieldOf("color_palette", ColorPalettes.DEFAULT)
                  .forGetter(FilmProperties::colorPalette),
               DitherMode.CODEC.optionalFieldOf("dither_mode", DitherMode.DITHERED).forGetter(FilmProperties::ditherMode),
               FilmStyle.CODEC.optionalFieldOf("style", FilmStyle.EMPTY).forGetter(FilmProperties::style)
            )
            .apply(instance, FilmProperties::new)
   );
   public static final StreamCodec<FriendlyByteBuf, FilmProperties> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, FilmProperties>() {
      public void encode(FriendlyByteBuf buffer, FilmProperties data) {
         ExposureType.STREAM_CODEC.encode(buffer, data.type());
         ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).encode(buffer, data.size());
         ByteBufCodecs.resourceKey(Exposure.Registries.COLOR_PALETTE).encode(buffer, data.colorPalette());
         DitherMode.STREAM_CODEC.encode(buffer, data.ditherMode());
         FilmStyle.STREAM_CODEC.encode(buffer, data.style());
      }

      @NotNull
      public FilmProperties decode(FriendlyByteBuf buffer) {
         return new FilmProperties(
            (ExposureType)ExposureType.STREAM_CODEC.decode(buffer),
            (Optional<Integer>)ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(buffer),
            (ResourceKey<ColorPalette>)ByteBufCodecs.resourceKey(Exposure.Registries.COLOR_PALETTE).decode(buffer),
            (DitherMode)DitherMode.STREAM_CODEC.decode(buffer),
            (FilmStyle)FilmStyle.STREAM_CODEC.decode(buffer)
         );
      }
   };
   public static final FilmProperties EMPTY = new FilmProperties(
      ExposureType.COLOR, Optional.empty(), ColorPalettes.DEFAULT, DitherMode.DITHERED, FilmStyle.EMPTY
   );

   public FilmProperties(ExposureType type, Optional<Integer> size, ResourceKey<ColorPalette> colorPalette, DitherMode ditherMode, FilmStyle style) {
      size.ifPresent(s -> Preconditions.checkArgument(s > 0 && s <= 2048, "size must be 1-2048: " + size));
      this.type = type;
      this.size = size;
      this.colorPalette = colorPalette;
      this.ditherMode = ditherMode;
      this.style = style;
   }

   public FilmProperties withType(ExposureType type) {
      return new FilmProperties(type, this.size, this.colorPalette, this.ditherMode, this.style);
   }

   public FilmProperties withSize(@Nullable Integer size) {
      return new FilmProperties(this.type, Optional.ofNullable(size), this.colorPalette, this.ditherMode, this.style);
   }

   public FilmProperties withColorPalette(@NotNull ResourceKey<ColorPalette> colorPalette) {
      return new FilmProperties(this.type, this.size, colorPalette, this.ditherMode, this.style);
   }

   public FilmProperties withDitherMode(DitherMode ditherMode) {
      return new FilmProperties(this.type, this.size, this.colorPalette, ditherMode, this.style);
   }

   public FilmProperties withStyle(@NotNull FilmStyle style) {
      return new FilmProperties(this.type, this.size, this.colorPalette, this.ditherMode, style);
   }

   public int getSize() {
      return this.size.orElse((Integer)Config.Server.DEFAULT_FRAME_SIZE.get());
   }

   public Holder<ColorPalette> getColorPalette(RegistryAccess access) {
      return ColorPalettes.get(access, this.colorPalette);
   }
}
