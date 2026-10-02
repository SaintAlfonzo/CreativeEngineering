package io.github.mortuusars.exposure.client.image.modifier.pixel;

import io.github.mortuusars.exposure.util.color.converter.HUSLColorConverter;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import org.apache.commons.lang3.StringUtils;

public class AgedHSLUVEffect implements PixelEffect {
   protected final int tintColor;
   protected final double[] tintColorHsluv;
   protected final float tintOpacity;
   protected final int blackPoint;
   protected final int whitePoint;

   public AgedHSLUVEffect(int tintColor, float tintOpacity, int blackPoint, int whitePoint) {
      this.tintColor = tintColor;
      String hexStr = StringUtils.leftPad(Integer.toHexString(tintColor & 16777215), 6, "0");
      this.tintColorHsluv = HUSLColorConverter.hexToHsluv("#" + hexStr);
      this.tintOpacity = tintOpacity;
      this.blackPoint = blackPoint & 0xFF;
      this.whitePoint = whitePoint & 0xFF;
   }

   @Override
   public String getIdentifier() {
      return "aged";
   }

   @Override
   public int modify(int colorARGB) {
      int alpha = ARGB32.alpha(colorARGB);
      int red = ARGB32.red(colorARGB);
      int green = ARGB32.green(colorARGB);
      int blue = ARGB32.blue(colorARGB);
      red = (int)Mth.map((float)red, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      green = (int)Mth.map((float)green, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      blue = (int)Mth.map((float)blue, 0.0F, 255.0F, (float)this.blackPoint, (float)this.whitePoint);
      double[] hsluv = HUSLColorConverter.rgbToHsluv(
         new double[]{(double)((float)red / 255.0F), (double)((float)green / 255.0F), (double)((float)blue / 255.0F)}
      );
      hsluv[0] = this.tintColorHsluv[0];
      hsluv[1] = this.tintColorHsluv[1];
      double[] rgb = HUSLColorConverter.hsluvToRgb(hsluv);
      int newRed = Mth.clamp((int)Mth.lerp((double)this.tintOpacity, (double)red, rgb[0] * 255.0), 0, 255);
      int newGreen = Mth.clamp((int)Mth.lerp((double)this.tintOpacity, (double)green, rgb[1] * 255.0), 0, 255);
      int newBlue = Mth.clamp((int)Mth.lerp((double)this.tintOpacity, (double)blue, rgb[2] * 255.0), 0, 255);
      return ARGB32.color(alpha, newRed, newGreen, newBlue);
   }

   @Override
   public String toString() {
      return "AgedHSLUVPixelModifier{tintColor=#"
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
