package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class ContrastEffect implements PixelEffect {
   protected final float contrast;

   public ContrastEffect(float contrast) {
      Preconditions.checkArgument(contrast >= -1.0F && contrast <= 1.0F, "contrast must be in -1 to 1 range.");
      this.contrast = contrast;
   }

   @Override
   public String getIdentifier() {
      return "contrast-" + this.contrast;
   }

   @Override
   public int modify(int colorARGB) {
      if (this.contrast == 0.0F) {
         return colorARGB;
      } else {
         int alpha = ARGB32.alpha(colorARGB);
         int red = ARGB32.red(colorARGB);
         int green = ARGB32.green(colorARGB);
         int blue = ARGB32.blue(colorARGB);
         int contrastValue = Math.round(127.0F * (1.0F + this.contrast));
         red = Mth.clamp((red - 127) * contrastValue / 127 + 127, 0, 255);
         green = Mth.clamp((green - 127) * contrastValue / 127 + 127, 0, 255);
         blue = Mth.clamp((blue - 127) * contrastValue / 127 + 127, 0, 255);
         return ARGB32.color(alpha, red, green, blue);
      }
   }

   @Override
   public String toString() {
      return "Contrast{contrast=" + this.contrast + "}";
   }
}
