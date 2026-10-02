package io.github.mortuusars.exposure.util.color;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.util.Mth;

public record Color(int a, int r, int g, int b) {
   public static final Codec<Color> CODEC = Codec.INT.xmap(Color::argb, Color::getARGB);
   public static final Codec<Color> HEX_STRING_CODEC = Codec.STRING.xmap(Color::fromHex, Color::asHexString);
   public static final Color WHITE = new Color(255, 255, 255, 255);
   public static final Color BLACK = new Color(255, 0, 0, 0);
   public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

   public Color(int a, int r, int g, int b) {
      validate(a, r, g, b);
      this.a = a;
      this.r = r;
      this.g = g;
      this.b = b;
   }

   private static void validate(int a, int r, int g, int b) {
      boolean rangeError = false;
      String badComponentString = "";
      if (a < 0 || a > 255) {
         rangeError = true;
         badComponentString = badComponentString + " Alpha";
      }

      if (r < 0 || r > 255) {
         rangeError = true;
         badComponentString = badComponentString + " Red";
      }

      if (g < 0 || g > 255) {
         rangeError = true;
         badComponentString = badComponentString + " Green";
      }

      if (b < 0 || b > 255) {
         rangeError = true;
         badComponentString = badComponentString + " Blue";
      }

      if (rangeError) {
         throw new IllegalArgumentException("Color parameter outside of expected range:" + badComponentString);
      }
   }

   public static Color argb(int a, int r, int g, int b) {
      return new Color(a, r, g, b);
   }

   public static Color argb(int argb) {
      return argb(argb >>> 24, argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF);
   }

   public static Color rgb(int r, int g, int b) {
      return new Color(255, r, g, b);
   }

   public static Color rgb(int rgb) {
      return rgb(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
   }

   public static Color abgr(int abgr) {
      return argb(abgr >>> 24, abgr & 0xFF, abgr >> 8 & 0xFF, abgr >> 16 & 0xFF);
   }

   public static Color bgr(int bgr) {
      return rgb(bgr & 0xFF, bgr >> 8 & 0xFF, bgr >> 16 & 0xFF);
   }

   public static Color argbF(float a, float r, float g, float b) {
      return argb(clamp((int)(a * 255.0F)), clamp((int)(r * 255.0F)), clamp((int)(g * 255.0F)), clamp((int)(b * 255.0F)));
   }

   public static Color rgbF(float r, float g, float b) {
      return rgb(clamp((int)(r * 255.0F)), clamp((int)(g * 255.0F)), clamp((int)(b * 255.0F)));
   }

   public int getA() {
      return this.a;
   }

   public int getR() {
      return this.r;
   }

   public int getG() {
      return this.g;
   }

   public int getB() {
      return this.b;
   }

   public float getAF() {
      return (float)this.a / 255.0F;
   }

   public float getRF() {
      return (float)this.r / 255.0F;
   }

   public float getGF() {
      return (float)this.g / 255.0F;
   }

   public float getBF() {
      return (float)this.b / 255.0F;
   }

   public int getARGB() {
      return this.a << 24 | this.r << 16 | this.g << 8 | this.b;
   }

   public int getABGR() {
      return this.a << 24 | this.b << 16 | this.g << 8 | this.r;
   }

   public int getRGB() {
      return 0xFF000000 | this.r << 16 | this.g << 8 | this.b;
   }

   public int getBGR() {
      return 0xFF000000 | this.b << 16 | this.g << 8 | this.r;
   }

   public String asHexString() {
      return String.format("%08X", this.getARGB());
   }

   public Color withAlpha(int alpha) {
      return new Color(alpha, this.getR(), this.getG(), this.getB());
   }

   public Color withAlphaF(float alpha) {
      return new Color(clamp((int)(alpha / 255.0F)), this.getR(), this.getG(), this.getB());
   }

   public Color add(Color other) {
      return argb(clamp(this.a + other.a), clamp(this.r + other.r), clamp(this.g + other.g), clamp(this.b + other.b));
   }

   public Color.Unbounded addUnbounded(Color other) {
      return new Color.Unbounded(this.r + other.r, this.g + other.g, this.b + other.b, this.a + other.a);
   }

   public Color.Unbounded addUnbounded(Color.Unbounded other) {
      return new Color.Unbounded(this.r + other.r, this.g + other.g, this.b + other.b, this.a + other.a);
   }

   public Color subtract(Color other) {
      return argb(clamp(this.a - other.a), clamp(this.r - other.r), clamp(this.g - other.g), clamp(this.b - other.b));
   }

   public Color.Unbounded subtractUnbounded(Color other) {
      return new Color.Unbounded(this.r - other.r, this.g - other.g, this.b - other.b, this.a - other.a);
   }

   public Color multiply(double scalar) {
      return argb(
         clamp((int)((double)this.a * scalar)),
         clamp((int)((double)this.r * scalar)),
         clamp((int)((double)this.g * scalar)),
         clamp((int)((double)this.b * scalar))
      );
   }

   public int squaredDifferenceTo(Color color) {
      int rDiff = Math.abs(this.r - color.r);
      int gDiff = Math.abs(this.g - color.g);
      int bDiff = Math.abs(this.b - color.b);
      return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
   }

   public int squaredDifferenceTo(int argb) {
      int rDiff = Math.abs(this.r - red(argb));
      int gDiff = Math.abs(this.g - green(argb));
      int bDiff = Math.abs(this.b - blue(argb));
      return rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Color color = (Color)o;
         return this.a == color.a && this.r == color.r && this.g == color.g && this.b == color.b;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.a, this.r, this.g, this.b);
   }

   public static int alpha(int argb) {
      return argb >>> 24;
   }

   public static int red(int argb) {
      return argb >> 16 & 0xFF;
   }

   public static int green(int argb) {
      return argb >> 8 & 0xFF;
   }

   public static int blue(int argb) {
      return argb & 0xFF;
   }

   public static float alphaF(int argb) {
      return (float)(argb >>> 24) / 255.0F;
   }

   public static float redF(int argb) {
      return (float)(argb >> 16 & 0xFF) / 255.0F;
   }

   public static float greenF(int argb) {
      return (float)(argb >> 8 & 0xFF) / 255.0F;
   }

   public static float blueF(int argb) {
      return (float)(argb & 0xFF) / 255.0F;
   }

   public static int pack(int alpha, int red, int green, int blue) {
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   public static int clamp(int channel) {
      return Mth.clamp(channel, 0, 255);
   }

   public static int ABGRtoARGB(int ABGR) {
      int a = ABGR >> 24 & 0xFF;
      int b = ABGR >> 16 & 0xFF;
      int g = ABGR >> 8 & 0xFF;
      int r = ABGR & 0xFF;
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static int ARGBtoABGR(int ARGB) {
      return ABGRtoARGB(ARGB);
   }

   public static Color fromHex(String hexColor) {
      return argb((int)Long.parseLong(hexColor.replace("#", ""), 16));
   }

   public static class HSB {
      public static int HSBtoRGB(float[] hsb) {
         Preconditions.checkArgument(hsb.length == 3, "hsb is not correct. Must have 3 float values.");
         return HSBtoRGB(hsb[0], hsb[1], hsb[2]);
      }

      public static int HSBtoRGB(float hue, float saturation, float brightness) {
         int r = 0;
         int g = 0;
         int b = 0;
         if (saturation == 0.0F) {
            r = g = b = (int)(brightness * 255.0F + 0.5F);
         } else {
            float h = (hue - (float)Math.floor((double)hue)) * 6.0F;
            float f = h - (float)Math.floor((double)h);
            float p = brightness * (1.0F - saturation);
            float q = brightness * (1.0F - saturation * f);
            float t = brightness * (1.0F - saturation * (1.0F - f));
            switch ((int)h) {
               case 0:
                  r = (int)(brightness * 255.0F + 0.5F);
                  g = (int)(t * 255.0F + 0.5F);
                  b = (int)(p * 255.0F + 0.5F);
                  break;
               case 1:
                  r = (int)(q * 255.0F + 0.5F);
                  g = (int)(brightness * 255.0F + 0.5F);
                  b = (int)(p * 255.0F + 0.5F);
                  break;
               case 2:
                  r = (int)(p * 255.0F + 0.5F);
                  g = (int)(brightness * 255.0F + 0.5F);
                  b = (int)(t * 255.0F + 0.5F);
                  break;
               case 3:
                  r = (int)(p * 255.0F + 0.5F);
                  g = (int)(q * 255.0F + 0.5F);
                  b = (int)(brightness * 255.0F + 0.5F);
                  break;
               case 4:
                  r = (int)(t * 255.0F + 0.5F);
                  g = (int)(p * 255.0F + 0.5F);
                  b = (int)(brightness * 255.0F + 0.5F);
                  break;
               case 5:
                  r = (int)(brightness * 255.0F + 0.5F);
                  g = (int)(p * 255.0F + 0.5F);
                  b = (int)(q * 255.0F + 0.5F);
            }
         }

         return 0xFF000000 | r << 16 | g << 8 | b;
      }

      public static float[] RGBtoHSB(int r, int g, int b, float[] output) {
         int cmax = Math.max(r, g);
         if (b > cmax) {
            cmax = b;
         }

         int cmin = Math.min(r, g);
         if (b < cmin) {
            cmin = b;
         }

         float brightness = (float)cmax / 255.0F;
         float saturation;
         if (cmax != 0) {
            saturation = (float)(cmax - cmin) / (float)cmax;
         } else {
            saturation = 0.0F;
         }

         float hue;
         if (saturation == 0.0F) {
            hue = 0.0F;
         } else {
            float redc = (float)(cmax - r) / (float)(cmax - cmin);
            float greenc = (float)(cmax - g) / (float)(cmax - cmin);
            float bluec = (float)(cmax - b) / (float)(cmax - cmin);
            if (r == cmax) {
               hue = bluec - greenc;
            } else if (g == cmax) {
               hue = 2.0F + redc - bluec;
            } else {
               hue = 4.0F + greenc - redc;
            }

            hue /= 6.0F;
            if (hue < 0.0F) {
               hue++;
            }
         }

         output[0] = hue;
         output[1] = saturation;
         output[2] = brightness;
         return output;
      }

      public static float[] RGBtoHSB(int r, int g, int b) {
         return RGBtoHSB(r, g, b, new float[3]);
      }
   }

   public static record Unbounded(int r, int g, int b, int a) {
      public Color.Unbounded multiply(double scalar) {
         return new Color.Unbounded(
            (int)((double)this.r * scalar), (int)((double)this.g * scalar), (int)((double)this.b * scalar), (int)((double)this.a * scalar)
         );
      }

      public Color clamp() {
         return Color.argb(Color.clamp(this.a), Color.clamp(this.r), Color.clamp(this.g), Color.clamp(this.b));
      }
   }
}
