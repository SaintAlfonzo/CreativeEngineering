package io.github.mortuusars.exposure.client.image.modifier.pixel;

import com.google.common.base.Preconditions;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class ColorBalanceEffect implements PixelEffect {
   protected final float r;
   protected final float g;
   protected final float b;

   public ColorBalanceEffect(float r, float g, float b) {
      Preconditions.checkArgument(r >= -1.0F && r <= 1.0F, "r must be in -1 to 1 range.");
      Preconditions.checkArgument(g >= -1.0F && g <= 1.0F, "g must be in -1 to 1 range.");
      Preconditions.checkArgument(b >= -1.0F && b <= 1.0F, "b must be in -1 to 1 range.");
      this.r = r;
      this.g = g;
      this.b = b;
   }

   @Override
   public String getIdentifier() {
      return "color-balance-r%s-g%s-b%s".formatted(this.r, this.g, this.b);
   }

   @Override
   public int modify(int colorARGB) {
      if (this.r == 0.0F && this.g == 0.0F && this.b == 0.0F) {
         return colorARGB;
      } else {
         int alpha = ARGB32.alpha(colorARGB);
         int red = ARGB32.red(colorARGB);
         int green = ARGB32.green(colorARGB);
         int blue = ARGB32.blue(colorARGB);
         red = Mth.clamp(Mth.floor((float)red * (this.r + 1.0F)), 0, 255);
         green = Mth.clamp(Mth.floor((float)green * (this.g + 1.0F)), 0, 255);
         blue = Mth.clamp(Mth.floor((float)blue * (this.b + 1.0F)), 0, 255);
         return ARGB32.color(alpha, red, green, blue);
      }
   }

   @Override
   public String toString() {
      return "ColorBalance{r=" + this.r + ", g=" + this.g + ", b=" + this.b + "}";
   }
}
