package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import io.github.mortuusars.exposure.backport.util.Backport;

public class ExposureEffect implements PixelEffect {
   protected final float brightness;

   public ExposureEffect(float brightness) {
      this.brightness = brightness;
   }

   public ExposureEffect(float brightnessStops, float brightnessPerStop) {
      this(1.0F + brightnessStops * brightnessPerStop);
   }

   public ExposureEffect(ShutterSpeed shutterSpeed) {
      this(shutterSpeed.getStops(), 0.2F);
   }

   @Override
   public String getIdentifier() {
      return "brightness-" + this.brightness;
   }

   @Override
   public int modify(int colorARGB) {
      if (this.brightness == 1.0F) {
         return colorARGB;
      } else {
         int alpha = ARGB32.alpha(colorARGB);
         int red = ARGB32.red(colorARGB);
         int green = ARGB32.green(colorARGB);
         int blue = ARGB32.blue(colorARGB);
         float lightness = (float)(blue + green + red) / 765.0F;
         float bias;
         if (this.brightness < 1.0F) {
            bias = (1.0F - lightness) * 0.8F + 0.2F;
         } else {
            float curve = (float)Math.pow(Math.sin((double)lightness * Math.PI), 2.0);
            bias = lightness > 0.5F ? curve * 0.8F + 0.2F : curve * 0.5F + 0.5F;
         }

         float b = Mth.lerp(bias, (float)blue, (float)blue * this.brightness);
         float g = Mth.lerp(bias, (float)green, (float)green * this.brightness);
         float r = Mth.lerp(bias, (float)red, (float)red * this.brightness);
         int[] rdst = this.redistribute(r, g, b);
         return ARGB32.color(
            alpha,
            Mth.clamp(Backport.lerpInt(0.5F, (int)r, rdst[0]), 0, 255),
            Mth.clamp(Backport.lerpInt(0.5F, (int)g, rdst[1]), 0, 255),
            Mth.clamp(Backport.lerpInt(0.5F, (int)b, rdst[2]), 0, 255)
         );
      }
   }

   private int[] redistribute(float red, float green, float blue) {
      float threshold = 255.999F;
      float max = Math.max(red, Math.max(green, blue));
      if (max <= threshold) {
         return new int[]{Mth.clamp(Math.round(red), 0, 255), Mth.clamp(Math.round(green), 0, 255), Mth.clamp(Math.round(blue), 0, 255)};
      } else {
         float total = red + green + blue;
         if (total >= 3.0F * threshold) {
            return new int[]{(int)threshold, (int)threshold, (int)threshold};
         } else {
            float x = (3.0F * threshold - total) / (3.0F * max - total);
            float gray = threshold - x * max;
            return new int[]{
               Mth.clamp(Math.round(gray + x * red), 0, 255), Mth.clamp(Math.round(gray + x * green), 0, 255), Mth.clamp(Math.round(gray + x * blue), 0, 255)
            };
         }
      }
   }

   @Override
   public String toString() {
      return "BrightnessProcessor[brightness=" + this.brightness + "]";
   }
}
