package io.github.mortuusars.exposure.client.image.modifier.pixel;

import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class BlackAndWhiteEffect implements PixelEffect {
   protected final float rWeight;
   protected final float gWeight;
   protected final float bWeight;
   protected String identifier;

   public BlackAndWhiteEffect(float rWeight, float gWeight, float bWeight) {
      this.rWeight = rWeight;
      this.gWeight = gWeight;
      this.bWeight = bWeight;
      this.identifier = "bw-" + rWeight + "-" + gWeight + "-" + bWeight;
   }

   @Override
   public String getIdentifier() {
      return this.identifier;
   }

   @Override
   public int modify(int colorARGB) {
      int alpha = ARGB32.alpha(colorARGB);
      int red = ARGB32.red(colorARGB);
      int green = ARGB32.green(colorARGB);
      int blue = ARGB32.blue(colorARGB);
      int value = Mth.clamp((int)(this.rWeight * (float)red + this.gWeight * (float)green + this.bWeight * (float)blue), 0, 255);
      return ARGB32.color(alpha, value, value, value);
   }

   @Override
   public String toString() {
      return "BlackAndWhiteProcessor{weights:%s,%s,%s}".formatted(this.rWeight, this.gWeight, this.bWeight);
   }
}
