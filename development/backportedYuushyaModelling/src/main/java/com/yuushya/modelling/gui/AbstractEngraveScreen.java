// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/AbstractEngraveScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui;

import com.yuushya.modelling.gui.engrave.IEngraveResult;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractEngraveScreen<T extends AbstractEngraveMenu> extends AbstractContainerScreen<T> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int RECIPES_COLUMNS = 4;
   private static final int RECIPES_ROWS = 3;
   private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
   private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
   private static final int SCROLLER_FULL_HEIGHT = 54;
   private static final int RECIPES_X = 52;
   private static final int RECIPES_Y = 14;
   private float scrollOffs;
   private boolean scrolling;
   private int startIndex;
   private boolean displayRecipes;

   public AbstractEngraveScreen(T menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      menu.registerUpdateListener(this::containerChanged);
      this.titleLabelY--;
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      // 1.19.2: the background is drawn by the screen itself (2.2.0 form).
      this.renderBackground(poseStack);
      super.render(poseStack, mouseX, mouseY, partialTick);
      this.renderTooltip(poseStack, mouseX, mouseY);
   }

   // 1.19.2 stonecutter.png form (2.2.0): the 1.20.2 GUI sprites do not exist, the scroller and recipe buttons are
   // regions of the background texture.
   @Override
   protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, BG_LOCATION);
      int i = this.leftPos;
      int j = this.topPos;
      this.blit(poseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      int k = (int)(41.0F * this.scrollOffs);
      this.blit(poseStack, i + 119, j + 15 + k, 176 + (this.isScrollBarActive() ? 0 : 12), 0, 12, 15);
      int l = this.leftPos + 52;
      int m = this.topPos + 14;
      int n = this.startIndex + 12;
      this.renderButtons(poseStack, mouseX, mouseY, l, m, n);
      this.renderRecipes(new GuiGraphics(poseStack), l, m, n);
   }

   @Override
   protected void renderTooltip(PoseStack poseStack, int x, int y) {
      super.renderTooltip(poseStack, x, y);
      if (this.displayRecipes) {
         int i = this.leftPos + 52;
         int j = this.topPos + 14;
         int k = this.startIndex + 12;
         List<IEngraveResult> list = ((AbstractEngraveMenu)this.menu).getRecipes();

         for (int l = this.startIndex; l < k && l < ((AbstractEngraveMenu)this.menu).getNumRecipes(); l++) {
            int m = l - this.startIndex;
            int n = i + m % 4 * 16;
            int o = j + m / 4 * 18 + 2;
            if (x >= n && x < n + 16 && y >= o && y < o + 18) {
               this.renderTooltip(poseStack, list.get(l).getResultItem(), x, y);
            }
         }
      }
   }

   private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, int x, int y, int lastVisibleElementIndex) {
      for (int i = this.startIndex; i < lastVisibleElementIndex && i < ((AbstractEngraveMenu)this.menu).getNumRecipes(); i++) {
         int j = i - this.startIndex;
         int k = x + j % 4 * 16;
         int l = j / 4;
         int m = y + l * 18 + 2;
         int n = this.imageHeight;
         if (i == ((AbstractEngraveMenu)this.menu).getSelectedRecipeIndex()) {
            n += 18;
         } else if (mouseX >= k && mouseY >= m && mouseX < k + 16 && mouseY < m + 18) {
            n += 36;
         }

         this.blit(poseStack, k, m - 1, 0, n, 16, 18);
      }
   }

   private void renderRecipes(GuiGraphics guiGraphics, int x, int y, int startIndex) {
      List<IEngraveResult> list = ((AbstractEngraveMenu)this.menu).getRecipes();

      for (int i = this.startIndex; i < startIndex && i < ((AbstractEngraveMenu)this.menu).getNumRecipes(); i++) {
         int j = i - this.startIndex;
         int k = x + j % 4 * 16;
         int l = j / 4;
         int m = y + l * 18 + 2;
         guiGraphics.renderItem(list.get(i).getResultItem(), k, m);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.scrolling = false;
      if (this.displayRecipes) {
         int i = this.leftPos + 52;
         int j = this.topPos + 14;
         int k = this.startIndex + 12;

         for (int l = this.startIndex; l < k; l++) {
            int m = l - this.startIndex;
            double d = mouseX - (double)(i + m % 4 * 16);
            double e = mouseY - (double)(j + m / 4 * 18);
            if (d >= 0.0 && e >= 0.0 && d < 16.0 && e < 18.0 && ((AbstractEngraveMenu)this.menu).clickMenuButton(this.minecraft.player, l)) {
               Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
               this.minecraft.gameMode.handleInventoryButtonClick(((AbstractEngraveMenu)this.menu).containerId, l);
               return true;
            }
         }

         i = this.leftPos + 119;
         j = this.topPos + 9;
         if (mouseX >= (double)i && mouseX < (double)(i + 12) && mouseY >= (double)j && mouseY < (double)(j + 54)) {
            this.scrolling = true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.scrolling && this.isScrollBarActive()) {
         int i = this.topPos + 14;
         int j = i + 54;
         this.scrollOffs = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
      if (this.isScrollBarActive()) {
         int i = this.getOffscreenRows();
         float f = (float)scrollY / (float)i;
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.startIndex = (int)((double)(this.scrollOffs * (float)i) + 0.5) * 4;
      }

      return true;
   }

   private boolean isScrollBarActive() {
      return this.displayRecipes && ((AbstractEngraveMenu)this.menu).getNumRecipes() > 12;
   }

   protected int getOffscreenRows() {
      return (((AbstractEngraveMenu)this.menu).getNumRecipes() + 4 - 1) / 4 - 3;
   }

   private void containerChanged() {
      this.displayRecipes = ((AbstractEngraveMenu)this.menu).hasInputItem();
      if (!this.displayRecipes) {
         this.scrollOffs = 0.0F;
         this.startIndex = 0;
      }
   }
}
