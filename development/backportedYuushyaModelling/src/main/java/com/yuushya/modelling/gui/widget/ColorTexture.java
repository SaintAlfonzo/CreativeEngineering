// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ColorTexture.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.mojang.blaze3d.platform.NativeImage;
import com.yuushya.modelling.utils.ShareUtils;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class ColorTexture implements AutoCloseable {
   private static ResourceLocation HueTextureLocation;
   private static ResourceLocation WhiteTextureLocation;
   private static ResourceLocation BlackTextureLocation;
   private final DynamicTexture HueTexture = new DynamicTexture(256, 1, false);
   private final DynamicTexture WhiteTexture = new DynamicTexture(256, 1, false);
   private final DynamicTexture BlackTexture = new DynamicTexture(1, 256, false);

   public ColorTexture() {
      HueTextureLocation = Minecraft.getInstance().getTextureManager().register("dynamic", this.HueTexture);
      WhiteTextureLocation = Minecraft.getInstance().getTextureManager().register("dynamic", this.WhiteTexture);
      BlackTextureLocation = Minecraft.getInstance().getTextureManager().register("dynamic", this.BlackTexture);
      NativeImage lightPixels = this.HueTexture.getPixels();
      NativeImage whitePixels = this.WhiteTexture.getPixels();
      NativeImage blackPixels = this.BlackTexture.getPixels();

      for (int x = 0; x < 256; x++) {
         float hue = (float)x / 256.0F;
         int rgba = Color.HSBtoRGB(hue, 1.0F, 1.0F);
         int abgr = ShareUtils.getABGR(rgba);

         for (int y = 0; y < 1; y++) {
            lightPixels.setPixelRGBA(x, y, abgr);
         }
      }

      for (int x = 0; x < 256; x++) {
         int alpha = 255 - x * 255 / 255;
         int r = 255;
         int g = 255;
         int b = 255;
         int rgba = alpha << 24 | r << 16 | g << 8 | b;
         int abgr = ShareUtils.getABGR(rgba);

         for (int y = 0; y < 1; y++) {
            whitePixels.setPixelRGBA(x, y, abgr);
         }
      }

      for (int x = 0; x < 256; x++) {
         int alpha = x * 255 / 255;
         int r = 0;
         int g = 0;
         int b = 0;
         int rgba = alpha << 24 | r << 16 | g << 8 | b;
         int abgr = ShareUtils.getABGR(rgba);

         for (int y = 0; y < 1; y++) {
            blackPixels.setPixelRGBA(y, x, abgr);
         }
      }

      this.HueTexture.upload();
      this.WhiteTexture.upload();
      this.BlackTexture.upload();
   }

   @Override
   public void close() {
      this.HueTexture.close();
      this.WhiteTexture.close();
      this.BlackTexture.close();
   }

   public static ResourceLocation getHueTextureLocation() {
      return HueTextureLocation;
   }

   public static ResourceLocation getWhiteTextureLocation() {
      return WhiteTextureLocation;
   }

   public static ResourceLocation getBlackTextureLocation() {
      return BlackTextureLocation;
   }
}
