package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class NegativeFilmEffect implements PixelEffect {
   @Override
   public String getIdentifier() {
      return "negative-film";
   }

   @Override
   public int modify(int ARGB) {
      int alpha = ARGB32.alpha(ARGB);
      int red = ARGB32.red(ARGB);
      int green = ARGB32.green(ARGB);
      int blue = ARGB32.blue(ARGB);
      int lightness = (red + green + blue) / 3;
      int opacity = (int)Mth.clamp((float)lightness * 1.5F, 0.0F, 255.0F);
      alpha = alpha * opacity / 255;
      red = 255 - red;
      green = 255 - green;
      blue = 255 - blue;
      return ARGB32.color(alpha, red, green, blue);
   }
}
