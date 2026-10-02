package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.world.camera.FilmColor;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class TintedNegativeFilmEffect implements PixelEffect {
   private final FilmColor tintColor;

   public TintedNegativeFilmEffect(FilmColor tintColor) {
      this.tintColor = tintColor;
   }

   @Override
   public String getIdentifier() {
      return "tinted-negative-film-" + this.tintColor;
   }

   @Override
   public int modify(int ARGB) {
      int alpha = ARGB32.alpha(ARGB);
      int red = ARGB32.red(ARGB);
      int green = ARGB32.green(ARGB);
      int blue = ARGB32.blue(ARGB);
      int brightness = (red + green + blue) / 3;
      int opacity = (int)Mth.clamp((float)brightness * 1.5F, 0.0F, 255.0F);
      alpha = alpha * opacity / 255;
      red = 255 - red;
      green = 255 - green;
      blue = 255 - blue;
      red = (int)((float)red * this.tintColor.r() / 255.0F);
      green = (int)((float)green * this.tintColor.g() / 255.0F);
      blue = (int)((float)blue * this.tintColor.b() / 255.0F);
      return ARGB32.color(alpha, red, green, blue);
   }
}
