package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class NoiseEffect implements PixelEffect {
   protected final Random random = new Random();
   protected final float intensity;

   public NoiseEffect(float intensity) {
      Preconditions.checkArgument(intensity >= 0.0F && intensity <= 1.0F, "intensity should be in 0-1 range.");
      this.intensity = intensity;
   }

   @Override
   public String getIdentifier() {
      return "noise-" + this.intensity;
   }

   @Override
   public int modify(int colorARGB) {
      int alpha = ARGB32.alpha(colorARGB);
      int red = ARGB32.red(colorARGB);
      int green = ARGB32.green(colorARGB);
      int blue = ARGB32.blue(colorARGB);
      float brightness = (0.299F * (float)red + 0.587F * (float)green + 0.114F * (float)blue) / 255.0F;
      float intensity = this.intensity * (1.0F - brightness);
      intensity = Mth.lerp(0.25F, intensity, this.intensity);
      int noise = (int)(this.random.nextGaussian() * (double)intensity * 155.0);
      int rNoise = (int)(this.random.nextGaussian() * (double)intensity * 100.0);
      int gNoise = (int)(this.random.nextGaussian() * (double)intensity * 100.0);
      int bNoise = (int)(this.random.nextGaussian() * (double)intensity * 100.0);
      red = Mth.clamp(red + noise + rNoise, 0, 255);
      green = Mth.clamp(green + noise + gNoise, 0, 255);
      blue = Mth.clamp(blue + noise + bNoise, 0, 255);
      return ARGB32.color(alpha, red, green, blue);
   }
}
