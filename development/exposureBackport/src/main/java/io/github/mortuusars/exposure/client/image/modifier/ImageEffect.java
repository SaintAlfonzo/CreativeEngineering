package io.github.mortuusars.exposure.client.image.modifier;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.image.CensoredImage;
import io.github.mortuusars.exposure.client.image.CroppedImage;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.ResizedImage;
import io.github.mortuusars.exposure.client.image.modifier.pixel.AgedHSBEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.BlackAndWhiteEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.ColorBalanceEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.ContrastEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.ExposureEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.HSBEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.LevelsEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.NegativeEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.NegativeFilmEffect;
import io.github.mortuusars.exposure.client.image.modifier.pixel.NoiseEffect;
import io.github.mortuusars.exposure.util.Rect2i;
import io.github.mortuusars.exposure.world.camera.ColorChannel;
import io.github.mortuusars.exposure.world.camera.film.properties.HSB;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;

public interface ImageEffect {
   ImageEffect EMPTY = new ImageEffect.Instance("", Function.identity());
   ImageEffect CENSORED = new ImageEffect.Instance("censored", CensoredImage::new);
   ImageEffect NEGATIVE = new NegativeEffect();
   ImageEffect NEGATIVE_FILM = new NegativeFilmEffect();
   ImageEffect AGED = new AgedHSBEffect(14264419, 0.65F, 40, 255);
   ImageEffect BLACK_AND_WHITE = new BlackAndWhiteEffect(0.299F, 0.587F, 0.114F);

   String getIdentifier();

   Image modify(Image var1);

   static ImageEffect exposure(float exposure) {
      return (ImageEffect)(exposure != 1.0F ? new ExposureEffect(exposure) : EMPTY);
   }

   static ImageEffect contrast(float contrast) {
      return (ImageEffect)(contrast != 0.0F ? new ContrastEffect(contrast) : EMPTY);
   }

   static ImageEffect noise(float noise) {
      return (ImageEffect)(noise != 0.0F ? new NoiseEffect(Mth.clamp(noise, 0.0F, 1.0F)) : EMPTY);
   }

   static ImageEffect levels(Levels levels) {
      return (ImageEffect)(!levels.equals(Levels.EMPTY) ? new LevelsEffect(levels) : EMPTY);
   }

   static ImageEffect hsb(HSB hsb) {
      return hsb(hsb.hue(), hsb.saturation(), hsb.brightness());
   }

   static ImageEffect hsb(float hue, float saturation, float brightness) {
      return (ImageEffect)(hue == 0.0F && saturation == 0.0F && brightness == 0.0F ? EMPTY : new HSBEffect(hue, saturation, brightness));
   }

   static ImageEffect colorBalance(float r, float g, float b) {
      return new ColorBalanceEffect(r, g, b);
   }

   static ImageEffect singleChannelBlackAndWhite(ColorChannel colorChannel) {
      return switch (colorChannel) {
         case RED -> new BlackAndWhiteEffect(1.0F, 0.05F, 0.05F);
         case GREEN -> new BlackAndWhiteEffect(0.05F, 1.0F, 0.05F);
         case BLUE -> new BlackAndWhiteEffect(0.05F, 0.05F, 1.0F);
      };
   }

   static ImageEffect composite(ImageEffect... modifiers) {
      return (ImageEffect)(modifiers.length == 0 ? EMPTY : new ImageEffect.Composite(modifiers));
   }

   static Function<Image, Image> chain(ImageEffect... modifiers) {
      return composite(modifiers)::modify;
   }

   static ImageEffect optional(boolean condition, ImageEffect modifier) {
      return condition ? modifier : EMPTY;
   }

   static ImageEffect optional(boolean condition, Supplier<ImageEffect> supplier) {
      return condition ? supplier.get() : EMPTY;
   }

   static <T> ImageEffect optional(Function<T, ImageEffect> modifier, Optional<T> optional) {
      return optional.map(modifier).orElse(EMPTY);
   }

   public static record Composite(ImageEffect... modifiers) implements ImageEffect {
      @Override
      public Image modify(Image image) {
         for (ImageEffect modifier : this.modifiers) {
            image = modifier.modify(image);
         }

         return image;
      }

      @Override
      public String getIdentifier() {
         return Arrays.stream(this.modifiers).filter(filter -> !filter.equals(EMPTY)).map(ImageEffect::getIdentifier).collect(Collectors.joining("_"));
      }
   }

   public interface Crop extends ImageEffect {
      ImageEffect SQUARE_CENTER = new ImageEffect.Instance("crop-square", image -> {
         int smallerSide = Math.min(image.width(), image.height());
         int x = (image.width() - smallerSide) / 2;
         int y = (image.height() - smallerSide) / 2;
         return new CroppedImage(image, new Rect2i(x, y, smallerSide, smallerSide));
      });

      static ImageEffect factor(double factor) {
         if (factor == 1.0) {
            return EMPTY;
         } else {
            double clampedFactor = Math.min(1.0, factor);
            return new ImageEffect.Instance("crop-factor-" + String.format("%,.4f", clampedFactor), image -> {
               int newWidth = (int)((double)image.width() * clampedFactor);
               int newHeight = (int)((double)image.height() * clampedFactor);
               int x = (image.width() - newWidth) / 2;
               int y = (image.height() - newHeight) / 2;
               return new CroppedImage(image, new Rect2i(x, y, newWidth, newHeight));
            });
         }
      }

      static ImageEffect factor(float factor) {
         return factor((double)factor);
      }
   }

   public static record Instance(String identifier, Function<Image, Image> processingFunction) implements ImageEffect {
      @Override
      public Image modify(Image image) {
         return this.processingFunction.apply(image);
      }

      @Override
      public String getIdentifier() {
         return this.identifier;
      }
   }

   public interface Resize extends ImageEffect {
      static ImageEffect to(int width, int height) {
         return new ImageEffect.Instance("resized-%sx%s".formatted(width, height), image -> new ResizedImage(image, width, height));
      }

      static ImageEffect to(int size) {
         return to(size, size);
      }

      static ImageEffect multiplier(int multiplier) {
         Preconditions.checkArgument(multiplier > 0, "multiplier should be larger than 0.");
         return new ImageEffect.Instance(
            "resized-%sx".formatted(multiplier), image -> new ResizedImage(image, image.width() * multiplier, image.height() * multiplier)
         );
      }
   }
}
