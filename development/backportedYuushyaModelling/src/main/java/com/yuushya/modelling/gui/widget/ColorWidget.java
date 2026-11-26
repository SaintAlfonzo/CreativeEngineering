// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ColorWidget.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.awt.Color;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import org.jetbrains.annotations.NotNull;
import com.mojang.math.Matrix4f;

public class ColorWidget extends AbstractWidget {
   private final AbstractColorScreen colorScreen;
   private final int WIDTH = 75;
   private final int xPadding = 5;
   private final int yPadding = 5;
   private final int yHeight = 65;
   float[] hsbVals = new float[3];

   public ColorWidget(int posX, int posY, int width, int height, int finalColor, Component message, AbstractColorScreen colorScreen) {
      super(posX, posY, width, height, message);
      this.colorScreen = colorScreen;
      Color color = new Color(finalColor);
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), this.hsbVals);
   }

   @Override
   public void renderButton(PoseStack poseStack, int i, int j, float f) {
      GuiGraphics guiGraphics = new GuiGraphics(poseStack);
      String hsv = String.format("H: %.2f S: %.2f V: %.2f", this.hsbVals[0], this.hsbVals[1], this.hsbVals[2]);
      guiGraphics.fill(
         this.x - 1,
         this.y - 1,
         this.x + this.getWidth() + 1,
         this.y + this.getHeight() + 26,
         ARGB32.color(72, 0, 0, 0)
      );
      guiGraphics.fill(
         this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight() + 25, ARGB32.color(72, 0, 0, 0)
      );
      guiGraphics.drawString(this.colorScreen.getColorFont(), hsv, this.x + 5, this.y + 145, ARGB32.color(255, 255, 255, 255), false);
      RenderSystem.enableBlend();
      guiGraphics.blit(ColorTexture.getHueTextureLocation(), this.x + 5, this.y + 35, 0, 0.0F, 0.0F, 75, 10, 75, 10);
      PoseStack pose = guiGraphics.pose();
      this.renderSolidColor(
         pose,
         this.x + 5 - 1,
         this.x + 5 + 75 + 1,
         this.y + 65 - 1,
         this.y + 65 + 75 + 1,
         ARGB32.color(72, 255, 255, 255)
      );
      this.renderSolidColor(
         pose, this.x + 5, this.x + 5 + 75, this.y + 65, this.y + 65 + 75, Color.HSBtoRGB(this.hsbVals[0], 1.0F, 1.0F)
      );
      this.innerBlit(
         ColorTexture.getWhiteTextureLocation(), pose, this.x + 5, this.x + 5 + 75, this.y + 65, this.y + 65 + 75
      );
      this.innerBlit(
         ColorTexture.getBlackTextureLocation(), pose, this.x + 5, this.x + 5 + 75, this.y + 65, this.y + 65 + 75
      );
      guiGraphics.fill(
         this.x + 5 - 1, this.y + 5 - 1, this.x + 25 + 1, this.y + 25 + 1, ARGB32.color(72, 255, 255, 255)
      );
      guiGraphics.fill(
         this.x + 5,
         this.y + 5,
         this.x + 25,
         this.y + 25,
         Color.HSBtoRGB(this.hsbVals[0], this.hsbVals[1], this.hsbVals[2])
      );
      RenderSystem.disableBlend();
      int hueX = (int)((float)(this.x + 5) + this.hsbVals[0] * 75.0F);
      int hueY = this.y + 35;
      this.drawArrow(guiGraphics, hueX, hueY);
      int satX = (int)((float)(this.x + 5) + this.hsbVals[1] * 75.0F);
      int valY = (int)((float)(this.y + 65) + (1.0F - this.hsbVals[2]) * 75.0F);
      this.drawCross(guiGraphics, satX, valY);
   }

   private void drawCross(GuiGraphics guiGraphics, int x, int y) {
      guiGraphics.hLine(x - 2, x + 2, y, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x, y - 3, y + 3, ARGB32.color(255, 0, 0, 0));
   }

   private void drawArrow(GuiGraphics guiGraphics, int x, int y) {
      guiGraphics.vLine(x, y - 4, y + 4, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x - 1, y - 4, y + 3, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x + 1, y - 4, y + 3, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x - 2, y - 4, y + 2, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x + 2, y - 4, y + 2, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x - 3, y - 4, y + 1, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x + 3, y - 4, y + 1, ARGB32.color(255, 0, 0, 0));
      guiGraphics.vLine(x, y - 3, y + 3, ARGB32.color(255, 255, 255, 255));
      guiGraphics.vLine(x - 1, y - 3, y + 2, ARGB32.color(255, 255, 255, 255));
      guiGraphics.vLine(x + 1, y - 3, y + 2, ARGB32.color(255, 255, 255, 255));
      guiGraphics.vLine(x - 2, y - 3, y + 1, ARGB32.color(255, 255, 255, 255));
      guiGraphics.vLine(x + 2, y - 3, y + 1, ARGB32.color(255, 255, 255, 255));
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.colorScreen.setColorFocused(this);
      this.colorScreen.setColorDragging(true);
      this.changeColor(mouseX, mouseY);
      return super.mouseClicked(mouseX, mouseY, button);
   }

   protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
      super.onDrag(mouseX, mouseY, dragX, dragY);
      this.changeColor(mouseX, mouseY);
   }

   private void changeColor(double mouseX, double mouseY) {
      if (this.visible) {
         mouseX -= (double)this.x;
         mouseY -= (double)this.y;
         int MARGIN = 4;
         if (mouseY >= 35.0 && mouseY <= 45.0 && mouseX >= 1.0 && mouseX <= 84.0) {
            double clampedX = Math.max(5.0, Math.min(mouseX, 80.0));
            float hue = (float)((clampedX - 5.0) / 75.0);
            this.hsbVals[0] = hue;
            this.updateData();
            this.setEditBox();
         }

         if (mouseY >= 61.0 && mouseY <= 144.0 && mouseX >= 1.0 && mouseX <= 84.0) {
            double clampedX = Math.max(5.0, Math.min(mouseX, 80.0));
            double clampedY = Math.max(65.0, Math.min(mouseY, 140.0));
            float brightness = 1.0F - (float)((clampedY - 65.0) / 75.0);
            float saturation = (float)((clampedX - 5.0) / 75.0);
            this.hsbVals[1] = saturation;
            this.hsbVals[2] = brightness;
            this.updateData();
            this.setEditBox();
         }
      }
   }

   private void setEditBox() {
      int rgb = Color.getHSBColor(this.hsbVals[0], this.hsbVals[1], this.hsbVals[2]).getRGB();
      String hex = String.format("#%06X", 16777215 & rgb);
      this.colorScreen.getColorEditBox().setValue(hex);
   }

   private void updateData() {
      this.colorScreen.updateColorData(Color.HSBtoRGB(this.hsbVals[0], this.hsbVals[1], this.hsbVals[2]));
   }

   public int getColor() {
      return Color.HSBtoRGB(this.hsbVals[0], this.hsbVals[1], this.hsbVals[2]);
   }

   public void setColor(int color) {
      Color c = new Color(color);
      Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), this.hsbVals);
   }

   @Override
   public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
   }

   void innerBlit(ResourceLocation atlasLocation, PoseStack poseStack, int x1, int x2, int y1, int y2) {
      RenderSystem.setShaderTexture(0, atlasLocation);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      Matrix4f matrix4f = poseStack.last().pose();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix4f, (float)x1, (float)y1, 0.0F).uv(0.0F, 0.0F).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x1, (float)y2, 0.0F).uv(0.0F, 1.0F).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x2, (float)y2, 0.0F).uv(1.0F, 1.0F).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x2, (float)y1, 0.0F).uv(1.0F, 0.0F).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
   }

   void renderSolidColor(PoseStack poseStack, int x1, int x2, int y1, int y2, int color) {
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Matrix4f matrix4f = poseStack.last().pose();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferbuilder.vertex(matrix4f, (float)x1, (float)y1, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x1, (float)y2, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x2, (float)y2, 0.0F).color(color).endVertex();
      bufferbuilder.vertex(matrix4f, (float)x2, (float)y1, 0.0F).color(color).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
   }
}
