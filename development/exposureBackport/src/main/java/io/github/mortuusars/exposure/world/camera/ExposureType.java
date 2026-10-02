package io.github.mortuusars.exposure.world.camera;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.util.color.Color;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.github.mortuusars.exposure.backport.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import io.github.mortuusars.exposure.backport.util.ByIdMap.OutOfBoundsStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ExposureType implements StringRepresentable {
   COLOR("color", Color.rgb(180, 130, 110), new FilmColor(1.2F, 0.96F, 0.75F, 1.0F)),
   BLACK_AND_WHITE("black_and_white", Color.WHITE, new FilmColor(1.0F, 1.0F, 1.0F, 1.0F));

   public static final Codec<ExposureType> CODEC = StringRepresentable.fromEnum(ExposureType::values);
   public static final StreamCodec<ByteBuf, ExposureType> STREAM_CODEC = ByteBufCodecs.idMapper(
      ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO), Enum::ordinal
   );
   private final String name;
   private final Color imageColor;
   private final FilmColor filmColor;

   private ExposureType(String name, Color imageColor, FilmColor filmColor) {
      this.name = name;
      this.imageColor = imageColor;
      this.filmColor = filmColor;
   }

   @NotNull
   public String getSerializedName() {
      return this.name;
   }

   public Color getImageColor() {
      return this.imageColor;
   }

   public FilmColor getFilmColor() {
      return this.filmColor;
   }

   public static Optional<ExposureType> byName(@Nullable String name) {
      for (ExposureType type : values()) {
         if (type.name().equals(name)) {
            return Optional.of(type);
         }
      }

      return Optional.empty();
   }
}
