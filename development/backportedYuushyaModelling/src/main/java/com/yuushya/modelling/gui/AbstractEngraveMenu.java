// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/AbstractEngraveMenu.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui;

import com.google.common.collect.Lists;
import com.yuushya.modelling.blockentity.itemblock.ItemBlock;
import com.yuushya.modelling.gui.engrave.EngraveBlockResult;
import com.yuushya.modelling.gui.engrave.EngraveItemResult;
import com.yuushya.modelling.gui.engrave.IEngraveResult;
import com.yuushya.modelling.network.TransformDataListPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractEngraveMenu extends AbstractContainerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int RESULT_SLOT = 1;
   private static final int INV_SLOT_START = 2;
   private static final int INV_SLOT_END = 29;
   private static final int USE_ROW_SLOT_START = 29;
   private static final int USE_ROW_SLOT_END = 38;
   final Slot inputSlot;
   final Slot resultSlot;
   final ResultContainer resultContainer = new ResultContainer();
   private final ContainerLevelAccess access;
   private final DataSlot selectedRecipeIndex = DataSlot.standalone();
   private final Level level;
   long lastSoundTime;
   Runnable slotUpdateListener = () -> {
   };
   private List<IEngraveResult> recipes = Lists.newArrayList();
   private ItemStack input = ItemStack.EMPTY;
   private AbstractEngraveMenu.BLOCK_TYPE useItemRecipes = AbstractEngraveMenu.BLOCK_TYPE.BLOCK;
   public final Container container = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         AbstractEngraveMenu.this.slotsChanged(this);
         AbstractEngraveMenu.this.slotUpdateListener.run();
      }
   };

   public AbstractEngraveMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
      this(menuType, containerId, playerInventory, ContainerLevelAccess.NULL);
   }

   public AbstractEngraveMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, final ContainerLevelAccess access) {
      super(menuType, containerId);
      this.access = access;
      this.level = playerInventory.player.getLevel();
      this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
         public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
         }

         public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            stack.onCraftedBy(player.getLevel(), player, stack.getCount());
            ItemStack itemStack = AbstractEngraveMenu.this.inputSlot.remove(1);
            if (!itemStack.isEmpty()) {
               AbstractEngraveMenu.this.setupResultSlotServer(stack);
            }

            access.execute((level, blockPos) -> {
               long l = level.getGameTime();
               if (AbstractEngraveMenu.this.lastSoundTime != l) {
                  level.playSound(null, blockPos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  AbstractEngraveMenu.this.lastSoundTime = l;
               }
            });
            super.onTake(player, stack);
         }

         private List<ItemStack> getRelevantItems() {
            return List.of(AbstractEngraveMenu.this.inputSlot.getItem());
         }
      });

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int var7 = 0; var7 < 9; var7++) {
         this.addSlot(new Slot(playerInventory, var7, 8 + var7 * 18, 142));
      }

      this.addDataSlot(this.selectedRecipeIndex);
   }

   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   public int getNumRecipes() {
      return this.recipes.size();
   }

   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && !this.recipes.isEmpty();
   }

   public boolean stillValid(@NotNull Player player) {
      return !this.container.getItem(0).isEmpty();
   }

   public boolean clickMenuButton(@NotNull Player player, int id) {
      if (this.isValidRecipeIndex(id)) {
         this.selectedRecipeIndex.set(id);
         this.setupResultSlot();
      }

      return true;
   }

   private boolean isValidRecipeIndex(int recipeIndex) {
      return recipeIndex >= 0 && recipeIndex < this.recipes.size();
   }

   public void slotsChanged(@NotNull Container container) {
      ItemStack itemStack = this.inputSlot.getItem();
      if (!itemStack.is(this.input.getItem())) {
         this.input = itemStack.copy();
         this.setupRecipeList(container, itemStack);
      }
   }

   private void setupRecipeList(Container container, ItemStack stack) {
      this.recipes.clear();
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
         if (blockItem.getBlock() instanceof ItemBlock) {
            this.useItemRecipes = AbstractEngraveMenu.BLOCK_TYPE.BLOCK;
            this.recipes = new ArrayList<>(this.getItemBlockRecipes());
         } else {
            // PHASE-B: a TextBlock input sets BLOCK_TYPE.TEXT and lists getTextBlockRecipes() (EngraveTextResult).
            this.useItemRecipes = AbstractEngraveMenu.BLOCK_TYPE.ITEM;
            this.recipes = new ArrayList<>(this.getShowBlockRecipes());
         }
      }
   }

   protected abstract List<EngraveBlockResult> getShowBlockRecipes();

   protected abstract List<EngraveItemResult> getItemBlockRecipes();

   // PHASE-B: protected abstract List<EngraveTextResult> getTextBlockRecipes();

   public void setupResultSlotServer(ItemStack resultItemStack) {
      ItemStack itemStack = resultItemStack.copy();
      // 1.19.2 has no feature flags (1.20 checked isItemEnabled here): every item is enabled.
      this.resultSlot.set(itemStack);

      this.broadcastChanges();
   }

   void setupResultSlot() {
      if (this.level.isClientSide) {
         if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            IEngraveResult recipeHolder = this.recipes.get(this.selectedRecipeIndex.get());
            ItemStack itemStack = recipeHolder.getResultItem().copy();
            // 1.19.2 has no feature flags (1.20 checked isItemEnabled here).
            TransformDataListPacket.sendToServerSide(recipeHolder);
            this.resultSlot.set(itemStack);
         } else {
            this.resultSlot.set(ItemStack.EMPTY);
         }

         this.broadcastChanges();
      }
   }

   public void registerUpdateListener(Runnable listener) {
      this.slotUpdateListener = listener;
   }

   public AbstractEngraveMenu.BLOCK_TYPE getBlockType() {
      return this.useItemRecipes;
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
   }

   @NotNull
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemStack2 = slot.getItem();
         Item item = itemStack2.getItem();
         itemStack = itemStack2.copy();
         if (index == 1) {
            item.onCraftedBy(itemStack2, player.getLevel(), player);
            if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemStack2, itemStack);
         } else if (index == 0) {
            if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemStack2);
         this.broadcastChanges();
      }

      return itemStack;
   }

   public void removed(@NotNull Player player) {
      super.removed(player);
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute((level, blockPos) -> this.clearContainer(player, this.container));
   }

   public List<IEngraveResult> getRecipes() {
      return this.recipes;
   }

   public static enum BLOCK_TYPE {
      BLOCK,
      ITEM,
      TEXT;
   }
}
