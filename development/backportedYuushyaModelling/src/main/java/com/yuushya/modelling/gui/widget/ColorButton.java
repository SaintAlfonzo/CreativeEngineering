// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ColorButton.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;

public class ColorButton extends Button {
   public int color = Color.WHITE.getRGB();
   public boolean showEditor;

   public ColorButton(int x, int y, int width, int height, OnPress onPress, int color) {
      super(x, y, width, height, Component.empty(), onPress);
      this.color = color;
   }

   public ColorButton(int x, int y, int width, int height, OnPress onPress) {
      super(x, y, width, height, Component.empty(), onPress);
   }

   @Override
   public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      GuiGraphics guiGraphics = new GuiGraphics(poseStack);
      if (this.showEditor) {
         guiGraphics.fill(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.BLACK.getRGB());
      }

      guiGraphics.fill(
         this.x + 2, this.y + 2, this.x + this.getWidth() - 2, this.y + this.getHeight() - 2, this.color | 0xFF000000
      );
   }
}
