package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.util.color.Color;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;

public class AgedHSBEffect implements PixelEffect {
   protected final int tintColor;
   protected final float tintOpacity;
   protected final int blackPoint;
   protected final int whitePoint;

   public AgedHSBEffect(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
      this.tintColor = tintColor;
      this.tintOpacity = tintOpacity;
      this.blackPoint = blackPoint & 0xFF;
      this.whitePoint = whitePoint & 0xFF;
   }

   @Override
   public String getIdentifier() {
      return "aged";
   }

   @Override
   public int modify(int ARGB) {
      int alpha = ARGB32.alpha(ARGB);
      int red = ARGB32.red(ARGB);
      int green = ARGB32.green(ARGB);
      int blue = ARGB32.blue(ARGB);
      red = (int)Mth.map((float)red, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      green = (int)Mth.map((float)green, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      blue = (int)Mth.map((float)blue, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      float[] baseHSB = new float[3];
      Color.HSB.RGBtoHSB(red, green, blue, baseHSB);
      float[] tintHSB = new float[3];
      Color.HSB.RGBtoHSB(ARGB32.red(this.tintColor), ARGB32.green(this.tintColor), ARGB32.blue(this.tintColor), tintHSB);
      int luma = Mth.clamp((int)(0.45 * (double)red + 0.65 * (double)green + 0.2 * (double)blue), 0, 255);
      int tintedRGB = Color.HSB.HSBtoRGB(tintHSB[0], tintHSB[1], (float)luma / 255.0F);
      int newBlue = Mth.clamp((int)Mth.lerp(this.tintOpacity, (float)blue, (float)ARGB32.blue(tintedRGB)), 0, 255);
      int newGreen = Mth.clamp((int)Mth.lerp(this.tintOpacity, (float)green, (float)ARGB32.green(tintedRGB)), 0, 255);
      int newRed = Mth.clamp((int)Mth.lerp(this.tintOpacity, (float)red, (float)ARGB32.red(tintedRGB)), 0, 255);
      return ARGB32.color(alpha, newRed, newGreen, newBlue);
   }

   @Override
   public String toString() {
      return "AgedHSBPixelModifier{tintColor=#"
         + Integer.toHexString(this.tintColor)
         + ", tintOpacity="
         + this.tintOpacity
         + ", blackPoint="
         + this.blackPoint
         + ", whitePoint="
         + this.whitePoint
         + "}";
   }
}
