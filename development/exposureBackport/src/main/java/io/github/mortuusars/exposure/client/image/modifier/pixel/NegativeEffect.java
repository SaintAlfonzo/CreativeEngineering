package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.FastColor.ARGB32;

public class NegativeEffect implements PixelEffect {
   @Override
   public String getIdentifier() {
      return "negative";
   }

   @Override
   public int modify(int ARGB) {
      int alpha = ARGB32.alpha(ARGB);
      int red = ARGB32.red(ARGB);
      int green = ARGB32.green(ARGB);
      int blue = ARGB32.blue(ARGB);
      red = 255 - red;
      green = 255 - green;
      blue = 255 - blue;
      return ARGB32.color(alpha, red, green, blue);
   }
}
