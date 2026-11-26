// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/BlockStateIconList.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.yuushya.modelling.blockentity.transformData.TransformBlockData;
import com.yuushya.modelling.gui.showblock.ShowBlockScreen;
import com.yuushya.modelling.registries.ItemRegistry;
import com.yuushya.modelling.utils.YuushyaDataTags;
import com.yuushya.modelling.utils.YuushyaUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockStateIconList extends ObjectSelectionList<BlockStateIconList.Entry> {
   protected final List<TransformBlockData> transformDataList;
   protected final List<BlockStateIconList.Entry> chosen = new ArrayList<>();
   protected final ShowBlockScreen screen;
   private final Map<Integer, MutableComponent> rememberDisplayName = new HashMap<>();
   private final Map<Integer, List<String>> rememberBlockStateProperties = new HashMap<>();
   private final Map<Integer, ItemStack> rememberItemStack = new HashMap<>();
   private final Map<Integer, Collection<Property<?>>> rememberProperties = new HashMap<>();
   private int itemHeight;
   private int itemWidth;

   public BlockStateIconList(
      Minecraft minecraft,
      int width,
      int height,
      int x,
      int y0,
      int itemWidth,
      int itemHeight,
      List<TransformBlockData> transformDataList,
      ShowBlockScreen showBlockScreen
   ) {
      super(minecraft, width, height, y0, y0 + height, itemHeight);
      this.setLeftPos(x);
      this.transformDataList = transformDataList;
      this.screen = showBlockScreen;
      this.centerListVertically = false;
      this.setRenderHeader(false, 0);
      this.itemWidth = itemWidth;
      this.itemHeight = itemHeight;
      this.updateRenderList();
   }

   public MutableComponent updateRenderDisplayName(BlockState blockState) {
      return this.rememberDisplayName.computeIfAbsent(Block.getId(blockState), id -> {
         BlockState blockState1 = Block.stateById(id);
         Item item = blockState1.getBlock().asItem();
         return item == Items.AIR ? blockState1.getBlock().getName() : (MutableComponent)item.getName(item.getDefaultInstance());
      });
   }

   public List<String> updateRenderBlockStateProperties(BlockState blockState) {
      return this.rememberBlockStateProperties
         .computeIfAbsent(
            Block.getId(blockState), id -> Block.stateById(id).getValues().entrySet().stream().map(YuushyaUtils.PROPERTY_ENTRY_TO_STRING_FUNCTION).toList()
         );
   }

   public ItemStack updateRenderItemstack(BlockState blockState) {
      return this.rememberItemStack.computeIfAbsent(Block.getId(blockState), id -> {
         ItemStack itemStack = ((Item)ItemRegistry.GET_BLOCKSTATE_ITEM.get()).getDefaultInstance();
         YuushyaDataTags.setBlockState(itemStack, Block.stateById(id));
         return itemStack;
      });
   }

   public Collection<Property<?>> updateRenderProperties(BlockState blockState) {
      return this.rememberProperties.computeIfAbsent(Block.getId(blockState), id -> Block.stateById(id).getBlock().getStateDefinition().getProperties());
   }

   public int getRowWidth() {
      return this.itemWidth;
   }

   protected int getScrollbarPosition() {
      return this.getLeft() + this.getWidth() - 4;
   }

   public void updateRenderList() {
      this.clearEntries();

      for (int i = 0; i < this.transformDataList.size(); i++) {
         this.addEntry(new BlockStateIconList.Entry(this, i));
      }

      this.notifyListUpdated();
   }

   public void addSlot() {
      if (this.getSelected() == null || this.transformDataList.get(((BlockStateIconList.Entry)this.getSelected()).slot).blockState.getBlock() != Blocks.AIR) {
         if (this.children().size() == this.transformDataList.size()) {
            BlockStateIconList.Entry entry = new BlockStateIconList.Entry(this, this.transformDataList.size());
            this.addEntry(entry);
            this.setSelected(entry);
         }
      }
   }

   private void notifyListUpdated() {
   }

   public void setSelectedSlot(int slot) {
      this.setSelected((BlockStateIconList.Entry)this.children().get(slot));
   }

   public void setSelected(@Nullable BlockStateIconList.Entry selected) {
      super.setSelected(selected);
      if (selected != null) {
         this.screen.setSlot(selected.slot);
      }
   }

   public int getChosenOne() {
      return !this.chosen.isEmpty() ? this.chosen.get(this.chosen.size() - 1).slot : -1;
   }

   public void setChosenCurrent() {
      BlockStateIconList.Entry selected = (BlockStateIconList.Entry)this.getSelected();
      if (selected != null) {
         selected.chosen = true;
         this.chosen.add(selected);
      }
   }

   public void clearChosen() {
      for (BlockStateIconList.Entry entry : this.chosen) {
         entry.chosen = false;
      }

      this.chosen.clear();
   }

   public static final class Entry extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<BlockStateIconList.Entry> {
      private final BlockStateIconList parent;
      private final int slot;
      private final Minecraft minecraft;
      private boolean chosen = false;

      public Entry(BlockStateIconList parent, int slot) {
         this.parent = parent;
         this.minecraft = parent.minecraft;
         this.slot = slot;
      }

      public TransformBlockData getTransformData() {
         return this.parent.transformDataList.size() > this.slot ? this.parent.transformDataList.get(this.slot) : new TransformBlockData();
      }

      public BlockState updateRenderState() {
         return this.parent.transformDataList.size() > this.slot ? this.parent.transformDataList.get(this.slot).blockState : Blocks.AIR.defaultBlockState();
      }

      public boolean updateRenderShown() {
         return this.parent.transformDataList.size() <= this.slot || this.parent.transformDataList.get(this.slot).isShown;
      }

      @NotNull
      public Component getNarration() {
         BlockState blockState = this.updateRenderState();
         Item item = blockState.getBlock().asItem();
         return item == Items.AIR ? blockState.getBlock().getName() : (MutableComponent)item.getName(item.getDefaultInstance());
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         BlockStateIconList.Entry preSelected = (BlockStateIconList.Entry)this.parent.getSelected();
         this.parent.setSelected(this);
         if (preSelected == this) {
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

      @Override
      public void render(
         PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick
      ) {
         GuiGraphics guiGraphics = new GuiGraphics(poseStack);
         BlockState blockState = this.updateRenderState();
         MutableComponent displayName = this.parent.updateRenderDisplayName(blockState);
         Font font = this.minecraft.font;
         int fontHeight = 9;
         guiGraphics.drawString(font, displayName, left + 3, top + 32, 16777215, false);
         if (this.updateRenderShown()) {
            guiGraphics.fill(left, top, left + 32 + 4, top + fontHeight + 32, -1601138544);
         }

         if (this.chosen) {
            guiGraphics.fill(left, top, left + 32 + 4, top + fontHeight + 32, 1608014895);
         }

         PoseStack pose = guiGraphics.pose();
         pose.pushPose();
         pose.translate((float)(left + 8), (float)(top + 24), 100.0F);
         pose.scale(16.0F, -16.0F, 16.0F);
         pose.mulPose(Vector3f.XP.rotationDegrees(30.0F));
         pose.mulPose(Vector3f.YP.rotationDegrees(45.0F));
         BakedModel blockModel = this.minecraft.getBlockRenderer().getBlockModel(blockState);
         this.minecraft
            .getBlockRenderer()
            .getModelRenderer()
            .renderModel(
               pose.last(),
               guiGraphics.bufferSource().getBuffer(RenderType.translucent()),
               blockState,
               blockModel,
               1.0F,
               1.0F,
               1.0F,
               15728880,
               OverlayTexture.NO_OVERLAY
            );
         // 1.19.2: flush here (1.20's GuiGraphics flushed its buffer source itself).
         guiGraphics.flush();
         pose.popPose();
      }
   }
}
