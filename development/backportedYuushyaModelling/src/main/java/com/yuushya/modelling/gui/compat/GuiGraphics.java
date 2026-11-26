// SPDX-License-Identifier: GPL-3.0-only
// Port helper (not upstream code): the subset of 1.20's GuiGraphics that the Yuushya Modelling 2.4.1 screens use,
// drawn with 1.19.2 PoseStack + GuiComponent + Font calls, so the ported screens keep upstream's drawing code.
// Each 1.19.2 render(PoseStack, ...) override wraps its PoseStack in one of these. See PORT_NOTES.md.
package com.yuushya.modelling.gui.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class GuiGraphics {
   private final Minecraft minecraft;
   private final PoseStack pose;
   private final MultiBufferSource.BufferSource bufferSource;

   public GuiGraphics(PoseStack pose) {
      this.minecraft = Minecraft.getInstance();
      this.pose = pose;
      this.bufferSource = this.minecraft.renderBuffers().bufferSource();
   }

   public PoseStack pose() {
      return this.pose;
   }

   public MultiBufferSource.BufferSource bufferSource() {
      return this.bufferSource;
   }

   public void flush() {
      RenderSystem.disableDepthTest();
      this.bufferSource.endBatch();
      RenderSystem.enableDepthTest();
   }

   public int guiWidth() {
      return this.minecraft.getWindow().getGuiScaledWidth();
   }

   public int guiHeight() {
      return this.minecraft.getWindow().getGuiScaledHeight();
   }

   public void fill(int minX, int minY, int maxX, int maxY, int color) {
      GuiComponent.fill(this.pose, minX, minY, maxX, maxY, color);
   }

   /** 1.20 hLine: inclusive on both ends. */
   public void hLine(int minX, int maxX, int y, int color) {
      if (maxX < minX) {
         int i = minX;
         minX = maxX;
         maxX = i;
      }
      this.fill(minX, y, maxX + 1, y + 1, color);
   }

   /** 1.20 vLine: exclusive on both ends. */
   public void vLine(int x, int minY, int maxY, int color) {
      if (maxY < minY) {
         int i = minY;
         minY = maxY;
         maxY = i;
      }
      this.fill(x, minY + 1, x + 1, maxY, color);
   }

   public int drawString(Font font, String text, int x, int y, int color) {
      return this.drawString(font, text, x, y, color, true);
   }

   public int drawString(Font font, String text, int x, int y, int color, boolean dropShadow) {
      if (text == null) {
         return 0;
      }
      return dropShadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
   }

   public int drawString(Font font, Component text, int x, int y, int color) {
      return this.drawString(font, text, x, y, color, true);
   }

   public int drawString(Font font, Component text, int x, int y, int color, boolean dropShadow) {
      return dropShadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
   }

   public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
      return dropShadow ? font.drawShadow(this.pose, text, (float)x, (float)y, color) : font.draw(this.pose, text, (float)x, (float)y, color);
   }

   public void drawCenteredString(Font font, Component text, int x, int y, int color) {
      GuiComponent.drawCenteredString(this.pose, font, text, x, y, color);
   }

   public void drawCenteredString(Font font, String text, int x, int y, int color) {
      GuiComponent.drawCenteredString(this.pose, font, text, x, y, color);
   }

   /** 1.20 blit(texture, x, y, u, v, width, height) on a 256x256 texture. */
   public void blit(ResourceLocation texture, int x, int y, int uOffset, int vOffset, int width, int height) {
      RenderSystem.setShaderTexture(0, texture);
      GuiComponent.blit(this.pose, x, y, 0, (float)uOffset, (float)vOffset, width, height, 256, 256);
   }

   /** 1.20 blit(texture, x, y, blitOffset, u, v, width, height, textureWidth, textureHeight). */
   public void blit(
      ResourceLocation texture, int x, int y, int blitOffset, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight
   ) {
      RenderSystem.setShaderTexture(0, texture);
      GuiComponent.blit(this.pose, x, y, blitOffset, uOffset, vOffset, width, height, textureWidth, textureHeight);
   }

   /** 1.20 renderItem: 1.19.2's item renderer draws in the model-view stack, so the pose is applied there. */
   public void renderItem(ItemStack stack, int x, int y) {
      if (stack.isEmpty()) {
         return;
      }
      PoseStack modelView = RenderSystem.getModelViewStack();
      modelView.pushPose();
      modelView.mulPoseMatrix(this.pose.last().pose());
      RenderSystem.applyModelViewMatrix();
      this.minecraft.getItemRenderer().renderAndDecorateFakeItem(stack, x, y);
      modelView.popPose();
      RenderSystem.applyModelViewMatrix();
   }

   public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
      if (stack.isEmpty()) {
         return;
      }
      PoseStack modelView = RenderSystem.getModelViewStack();
      modelView.pushPose();
      modelView.mulPoseMatrix(this.pose.last().pose());
      RenderSystem.applyModelViewMatrix();
      this.minecraft.getItemRenderer().renderGuiItemDecorations(font, stack, x, y);
      modelView.popPose();
      RenderSystem.applyModelViewMatrix();
   }
}
