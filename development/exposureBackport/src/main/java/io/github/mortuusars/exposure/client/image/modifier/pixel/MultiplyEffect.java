package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.FastColor.ARGB32;

public class MultiplyEffect implements PixelEffect {
   protected final int multiplyColor;

   public MultiplyEffect(int multiplyColor) {
      this.multiplyColor = multiplyColor;
   }

   @Override
   public String getIdentifier() {
      return this.multiplyColor != 0 ? "multiply-" + Integer.toHexString(this.multiplyColor) : "";
   }

   @Override
   public int modify(int colorARGB) {
      if (this.multiplyColor == 0) {
         return colorARGB;
      } else {
         int alpha = ARGB32.alpha(colorARGB);
         int red = ARGB32.red(colorARGB);
         int green = ARGB32.green(colorARGB);
         int blue = ARGB32.blue(colorARGB);
         int tintAlpha = ARGB32.alpha(colorARGB);
         int tintRed = ARGB32.red(colorARGB);
         int tintGreen = ARGB32.green(colorARGB);
         int tintBlue = ARGB32.blue(colorARGB);
         alpha = Math.min(255, alpha * tintAlpha / 255);
         red = Math.min(255, red * tintRed / 255);
         green = Math.min(255, green * tintGreen / 255);
         blue = Math.min(255, blue * tintBlue / 255);
         return ARGB32.color(alpha, green, red, blue);
      }
   }
}
