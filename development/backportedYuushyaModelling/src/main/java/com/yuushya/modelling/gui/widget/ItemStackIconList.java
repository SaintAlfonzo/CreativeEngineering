// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ItemStackIconList.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.yuushya.modelling.blockentity.transformData.ItemTransformType;
import com.yuushya.modelling.blockentity.transformData.TransformItemData;
import com.yuushya.modelling.gui.itemblock.ItemBlockScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class ItemStackIconList extends ObjectSelectionList<ItemStackIconList.Entry> {
   protected final List<TransformItemData> transformDataList;
   protected final List<ItemStackIconList.Entry> chosen = new ArrayList<>();
   protected final ItemBlockScreen screen;
   private int itemHeight;
   private int itemWidth;

   public ItemStackIconList(
      Minecraft minecraft,
      int width,
      int height,
      int x,
      int y0,
      int itemWidth,
      int itemHeight,
      List<TransformItemData> transformDataList,
      ItemBlockScreen itemBlockScreen
   ) {
      super(minecraft, width, height, y0, y0 + height, itemHeight);
      this.setLeftPos(x);
      this.transformDataList = transformDataList;
      this.screen = itemBlockScreen;
      this.centerListVertically = false;
      this.setRenderHeader(false, 0);
      this.itemWidth = itemWidth;
      this.itemHeight = itemHeight;
      this.updateRenderList();
   }

   public void updateRenderList() {
      this.clearEntries();

      for (int i = 0; i < this.transformDataList.size(); i++) {
         ItemStackIconList.Entry entry = new ItemStackIconList.Entry(this, i);
         this.addEntry(entry);
      }
   }

   public void addSlot() {
      if (this.getSelected() == null || !this.transformDataList.get(((ItemStackIconList.Entry)this.getSelected()).slot).itemStack.isEmpty()) {
         if (this.children().size() == this.transformDataList.size()) {
            ItemStackIconList.Entry entry = new ItemStackIconList.Entry(this, this.transformDataList.size());
            this.addEntry(entry);
            this.setSelected(entry);
         }
      }
   }

   public int getChosenOne() {
      return !this.chosen.isEmpty() ? this.chosen.get(this.chosen.size() - 1).slot : -1;
   }

   public void setSelected(@Nullable ItemStackIconList.Entry selected) {
      super.setSelected(selected);
      if (selected != null) {
         this.screen.setSlot(selected.slot);
      }
   }

   public void setSelectedSlot(int slot) {
      this.setSelected((ItemStackIconList.Entry)this.children().get(slot));
   }

   public int getRowWidth() {
      return this.itemWidth;
   }

   protected int getScrollbarPosition() {
      return this.getLeft() + this.getWidth() - 4;
   }

   public class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<ItemStackIconList.Entry> {
      private final ItemStackIconList parent;
      private final int slot;
      private final Minecraft minecraft;
      private boolean chosen = false;
      private boolean animating = false;
      private long animStartMs = 0L;
      private static final long PHASE_MS = 80L;
      private static final long TOTAL_MS = 160L;
      private float cachedScaleX = 1.0F;
      private float cachedScaleY = 1.0F;
      private float cachedScaleZ = 1.0F;

      public Entry(ItemStackIconList parent, int slot) {
         this.parent = parent;
         this.minecraft = parent.minecraft;
         this.slot = slot;
      }

      public TransformItemData getTransformData() {
         return this.parent.transformDataList.size() > this.slot ? this.parent.transformDataList.get(this.slot) : new TransformItemData();
      }

      public ItemStack updateRenderState() {
         return this.parent.transformDataList.size() > this.slot ? this.parent.transformDataList.get(this.slot).itemStack : Items.AIR.getDefaultInstance();
      }

      public boolean updateRenderShown() {
         return this.parent.transformDataList.size() <= this.slot || this.parent.transformDataList.get(this.slot).isShown;
      }

      private void startPopupAnimation() {
         TransformItemData td = this.getTransformData();
         this.cachedScaleX = td.scales.x();
         this.cachedScaleY = td.scales.y();
         this.cachedScaleZ = td.scales.z();
         this.animStartMs = System.currentTimeMillis();
         this.animating = true;
      }

      private void applyAnimationIfNeeded() {
         if (this.animating) {
            long elapsed = System.currentTimeMillis() - this.animStartMs;
            if (elapsed >= 160L) {
               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_X, (double)this.cachedScaleX);
               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_Y, (double)this.cachedScaleY);
               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_Z, (double)this.cachedScaleZ);
               this.animating = false;
            } else {
               float scaleFactor;
               if (elapsed < 80L) {
                  float p = (float)elapsed / 80.0F;
                  scaleFactor = 1.0F + 0.1F * p;
               } else {
                  float p = (float)(elapsed - 80L) / 80.0F;
                  scaleFactor = 1.1F - 0.1F * p;
               }

               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_X, (double)(this.cachedScaleX * scaleFactor));
               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_Y, (double)(this.cachedScaleY * scaleFactor));
               ItemStackIconList.this.screen.updateTransformDataClient(ItemTransformType.SCALE_Z, (double)(this.cachedScaleZ * scaleFactor));
            }
         }
      }

      @Override
      public void render(
         PoseStack poseStack, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isMouseOver, float partialTick
      ) {
         GuiGraphics guiGraphics = new GuiGraphics(poseStack);
         this.applyAnimationIfNeeded();
         ItemStack itemStack = this.updateRenderState();
         if (!itemStack.isEmpty()) {
            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate((float)x + (float)itemWidth / 2.0F, (float)y + (float)itemHeight / 2.0F, 100.0F);
            pose.scale(24.0F, -24.0F, 24.0F);
            boolean flatItem = !bakedModel.usesBlockLight();
            if (flatItem) {
               pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            }

            // 1.19.2: GUI item lighting and the batch flush are the caller's job (1.20's GuiGraphics did both).
            if (flatItem) {
               Lighting.setupForFlatItems();
            }

            Minecraft.getInstance()
               .getItemRenderer()
               .render(itemStack, TransformType.GUI, false, pose, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
            guiGraphics.flush();
            if (flatItem) {
               Lighting.setupFor3DItems();
            }

            pose.popPose();
         }

         Font font = Minecraft.getInstance().font;
         String indexStr = String.valueOf(this.slot);
         int textX = x + itemWidth - font.width(indexStr) - 2;
         int textY = y + 2;
         guiGraphics.drawString(font, indexStr, textX, textY, 16777215, true);
         if (isMouseOver || this == ItemStackIconList.this.getSelected()) {
            guiGraphics.fill(x - 1, y - 1, x + itemWidth + 1, y + itemHeight + 1, -2130706433);
         }
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         ItemStackIconList.Entry previousSelected = (ItemStackIconList.Entry)this.parent.getSelected();
         if (previousSelected != this) {
            this.startPopupAnimation();
         }

         this.parent.setSelected(this);
         if (previousSelected == this) {
            if (this.chosen) {
               this.chosen = false;
               this.parent.chosen.remove(this);
            } else {
               this.chosen = true;
               this.parent.chosen.add(this);
            }
         }

         return true;
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", new Object[]{this.slot});
      }
   }
}
