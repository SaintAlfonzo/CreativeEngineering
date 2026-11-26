// SPDX-License-Identifier: GPL-3.0-only
// New in the backport: the client half of Yuushya Modelling 2.4.1's ColorPickerItem.pickColor (MIT, see
// NOTICE-upstream-MIT.txt), moved out of the common item class so a dedicated server never loads Minecraft,
// ClientLevel, Window or GL11 (upstream checked `level instanceof ClientLevel` in common code).
package com.yuushya.modelling.client;

import com.mojang.blaze3d.platform.Window;
import com.yuushya.modelling.utils.ClientMethod;
import com.yuushya.modelling.utils.YuushyaDataTags;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class ColorPickerClient {
   private ColorPickerClient() {
   }

   /** Reads the pixel under the crosshair (drawn inverted, so the read value is inverted back), stores and copies it. */
   public static void pickColor(ItemStack handItemStack) {
      Window window = Minecraft.getInstance().getWindow();
      int centerX = window.getScreenWidth() / 2;
      int centerY = window.getScreenHeight() / 2;
      ByteBuffer buffer = BufferUtils.createByteBuffer(4);
      GL11.glReadPixels(centerX, centerY, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
      int r = 255 - buffer.get(0) & 0xFF;
      int g = 255 - buffer.get(1) & 0xFF;
      int b = 255 - buffer.get(2) & 0xFF;
      int color = 0xFF000000 | r << 16 | g << 8 | b;
      YuushyaDataTags.setColorData(handItemStack, color);
      ClientMethod.setClipboard(String.format("#%06X", color));
   }

   public static void copyColor(int color) {
      ClientMethod.setClipboard(String.format("#%06X", color));
   }
}
