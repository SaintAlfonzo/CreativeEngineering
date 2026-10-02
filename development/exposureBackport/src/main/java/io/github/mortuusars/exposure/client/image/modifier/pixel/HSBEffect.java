package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class HSBEffect implements PixelEffect {
   protected final float h;
   protected final float s;
   protected final float b;

   public HSBEffect(float h, float s, float b) {
      Preconditions.checkArgument(h >= -1.0F && h <= 1.0F, "h must be in -1 to 1 range.");
      Preconditions.checkArgument(s >= -1.0F && s <= 1.0F, "s must be in -1 to 1 range.");
      Preconditions.checkArgument(b >= -1.0F && b <= 1.0F, "b must be in -1 to 1 range.");
      this.h = h;
      this.s = s;
      this.b = b;
   }

   @Override
   public String getIdentifier() {
      return "hsb-h%s-s%s-b%s".formatted(this.h, this.s, this.b);
   }

   @Override
   public int modify(int colorARGB) {
      int alpha = ARGB32.alpha(colorARGB);
      int red = ARGB32.red(colorARGB);
      int green = ARGB32.green(colorARGB);
      int blue = ARGB32.blue(colorARGB);
      float[] hsb = Color.HSB.RGBtoHSB(red, green, blue);
      hsb[0] = (hsb[0] + this.h) % 1.0F;
      if (hsb[0] < 0.0F) {
         hsb[0]++;
      }

      hsb[1] = Mth.clamp(hsb[1] * (this.s + 1.0F), 0.0F, 1.0F);
      hsb[2] = Mth.clamp(hsb[2] * (this.b + 1.0F), 0.0F, 1.0F);
      return alpha << 24 | Color.HSB.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
   }
}
