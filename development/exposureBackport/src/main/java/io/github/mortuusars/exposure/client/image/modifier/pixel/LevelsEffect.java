package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import net.minecraft.util.FastColor.ARGB32;

public class LevelsEffect implements PixelEffect {
   protected final int shadows;
   protected final int midtones;
   protected final int highlights;
   protected final int black;
   protected final int white;

   public LevelsEffect(int shadows, int midtones, int highlights, int black, int white) {
      checkRange(shadows, "shadows");
      checkRange(midtones, "midtones");
      checkRange(highlights, "highlights");
      checkRange(black, "black");
      checkRange(white, "white");
      this.shadows = shadows;
      this.midtones = midtones;
      this.highlights = highlights;
      this.black = black;
      this.white = white;
   }

   public LevelsEffect(Levels levels) {
      this(levels.shadows(), levels.midtones(), levels.highlights(), levels.black(), levels.white());
   }

   private static void checkRange(int value, String name) {
      Preconditions.checkArgument(value >= 0 && value <= 255, name + " '" + value + "' is not valid. 0-255.");
   }

   @Override
   public String getIdentifier() {
      return "levels-s%s-m%s-h%s-b%s-w%s".formatted(this.shadows, this.midtones, this.highlights, this.black, this.white);
   }

   @Override
   public int modify(int colorARGB) {
      int alpha = ARGB32.alpha(colorARGB);
      int red = ARGB32.red(colorARGB);
      int green = ARGB32.green(colorARGB);
      int blue = ARGB32.blue(colorARGB);
      double gammaCorrection = this.calculateGammaCorrection(this.midtones);
      red = this.adjustChannel(red, this.shadows, this.highlights, this.black, this.white, gammaCorrection);
      green = this.adjustChannel(green, this.shadows, this.highlights, this.black, this.white, gammaCorrection);
      blue = this.adjustChannel(blue, this.shadows, this.highlights, this.black, this.white, gammaCorrection);
      return ARGB32.color(alpha, red, green, blue);
   }

   protected double calculateGammaCorrection(int inputMid) {
      double midtoneNormal = (double)inputMid / 255.0;
      double gamma = 1.0;
      if (inputMid < 128) {
         midtoneNormal *= 2.0;
         gamma = 1.0 + 9.0 * (1.0 - midtoneNormal);
         gamma = Math.min(gamma, 9.99);
      } else if (inputMid > 128) {
         midtoneNormal = midtoneNormal * 2.0 - 1.0;
         gamma = 1.0 - midtoneNormal;
         gamma = Math.max(gamma, 0.01);
      }

      return 1.0 / gamma;
   }

   protected int adjustChannel(int value, int inputBlack, int inputWhite, int outputBlack, int outputWhite, double gammaCorrection) {
      value = (int)(255.0 * (double)(value - inputBlack) / (double)(inputWhite - inputBlack));
      value = Math.max(0, Math.min(255, value));
      if (gammaCorrection != 1.0) {
         value = (int)(255.0 * Math.pow((double)value / 255.0, gammaCorrection));
      }

      value = (int)((double)value / 255.0 * (double)(outputWhite - outputBlack) + (double)outputBlack);
      return Math.max(0, Math.min(255, value));
   }
}
