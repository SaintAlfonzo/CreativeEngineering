package io.github.mortuusars.exposure.world.camera.film.properties;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.mortuusars.exposure.util.Codecs;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public record FilmStyle(Float sensitivity, Float contrast, Levels levels, HSB hsb, ColorBalance colorBalance, Float noise) {
   public static final Codec<FilmStyle> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codecs.floatRange(-10.0F, 10.0F).optionalFieldOf("sensitivity", 0.0F).forGetter(FilmStyle::sensitivity),
               Codecs.floatRange(-1.0F, 1.0F).optionalFieldOf("contrast", 0.0F).forGetter(FilmStyle::contrast),
               Levels.CODEC.optionalFieldOf("levels", Levels.EMPTY).forGetter(FilmStyle::levels),
               HSB.CODEC.optionalFieldOf("hsb", HSB.EMPTY).forGetter(FilmStyle::hsb),
               ColorBalance.CODEC.optionalFieldOf("color_balance", ColorBalance.EMPTY).forGetter(FilmStyle::colorBalance),
               Codecs.floatRange(0.0F, 1.0F).optionalFieldOf("noise", 0.0F).forGetter(FilmStyle::noise)
            )
            .apply(instance, FilmStyle::new)
   );
   public static final StreamCodec<FriendlyByteBuf, FilmStyle> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, FilmStyle>() {
      @NotNull
      public FilmStyle decode(FriendlyByteBuf buffer) {
         return new FilmStyle(
            (Float)ByteBufCodecs.FLOAT.decode(buffer),
            (Float)ByteBufCodecs.FLOAT.decode(buffer),
            (Levels)Levels.STREAM_CODEC.decode(buffer),
            (HSB)HSB.STREAM_CODEC.decode(buffer),
            (ColorBalance)ColorBalance.STREAM_CODEC.decode(buffer),
            (Float)ByteBufCodecs.FLOAT.decode(buffer)
         );
      }

      public void encode(FriendlyByteBuf buffer, FilmStyle data) {
         ByteBufCodecs.FLOAT.encode(buffer, data.sensitivity);
         ByteBufCodecs.FLOAT.encode(buffer, data.contrast);
         Levels.STREAM_CODEC.encode(buffer, data.levels);
         HSB.STREAM_CODEC.encode(buffer, data.hsb);
         ColorBalance.STREAM_CODEC.encode(buffer, data.colorBalance);
         ByteBufCodecs.FLOAT.encode(buffer, data.noise);
      }
   };
   public static final FilmStyle EMPTY = new FilmStyle(0.0F, 0.0F, Levels.EMPTY, HSB.EMPTY, ColorBalance.EMPTY, 0.0F);

   public FilmStyle(Float sensitivity, Float contrast, Levels levels, HSB hsb, ColorBalance colorBalance, Float noise) {
      Preconditions.checkArgument(sensitivity >= -10.0F && sensitivity <= 10.0F, "sensitivity must be >=-10 and <= 10. Got: " + sensitivity);
      Preconditions.checkArgument(contrast >= -1.0F && contrast <= 1.0F, "contrast must be >=-1 and <= 1. Got: " + contrast);
      Preconditions.checkArgument(noise >= 0.0F && noise <= 1.0F, "noise must be >=0 and <=1. Got: " + noise);
      this.sensitivity = sensitivity;
      this.contrast = contrast;
      this.levels = levels;
      this.hsb = hsb;
      this.colorBalance = colorBalance;
      this.noise = noise;
   }

   public static FilmStyle create() {
      return EMPTY;
   }

   public FilmStyle withSensitivity(@Nullable Float sensitivity) {
      return new FilmStyle(sensitivity, this.contrast, this.levels, this.hsb, this.colorBalance, this.noise);
   }

   public FilmStyle withContrast(@Nullable Float contrast) {
      return new FilmStyle(this.sensitivity, contrast, this.levels, this.hsb, this.colorBalance, this.noise);
   }

   public FilmStyle withLevels(@Nullable Levels levels) {
      return new FilmStyle(this.sensitivity, this.contrast, levels, this.hsb, this.colorBalance, this.noise);
   }

   public FilmStyle withHSB(@Nullable HSB hsb) {
      return new FilmStyle(this.sensitivity, this.contrast, this.levels, hsb, this.colorBalance, this.noise);
   }

   public FilmStyle withColorBalance(@Nullable ColorBalance colorBalance) {
      return new FilmStyle(this.sensitivity, this.contrast, this.levels, this.hsb, colorBalance, this.noise);
   }

   public FilmStyle withNoise(@Nullable Float noise) {
      return new FilmStyle(this.sensitivity, this.contrast, this.levels, this.hsb, this.colorBalance, noise);
   }
}
