package net.ribs.vintagedelight.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.block.entity.FermentingJarBlockEntity;
import net.ribs.vintagedelight.item.ModTags;

public class FermentingJarMenu extends AbstractContainerMenu {
   public final FermentingJarBlockEntity blockEntity;
   private final Level level;
   private final ContainerData data;
   private static final int HOTBAR_SLOT_COUNT = 9;
   private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
   private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
   private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
   private static final int VANILLA_SLOT_COUNT = 36;
   private static final int VANILLA_FIRST_SLOT_INDEX = 0;
   private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 36;
   private static final int TE_INVENTORY_SLOT_COUNT = 9;

   public FermentingJarMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
      this(pContainerId, inv, inv.player.getLevel().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
   }

   public FermentingJarMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
      super((MenuType)ModMenuTypes.FERMENTING_MENU.get(), pContainerId);
      checkContainerSize(inv, 7);
      this.blockEntity = (FermentingJarBlockEntity)entity;
      this.level = inv.player.getLevel();
      this.data = data;
      this.addPlayerInventory(inv);
      this.addPlayerHotbar(inv);
      {
         IItemHandler iItemHandler = this.blockEntity.getInventory();
         int inputBaseX = 26;
         int yOffset = 6;
         int firstRowY = 21 + yOffset;
         int secondRowY = firstRowY + 18;
         int inputSpacing = 18;
         int outputOffsetX = 90;
         int secondaryOutputX = outputOffsetX + inputSpacing;

         for (int i = 0; i < 6; i++) {
            int row = i / 3;
            int col = i % 3;
            int x = inputBaseX + col * inputSpacing;
            int y = row == 0 ? firstRowY : secondRowY;
            this.addSlot(new SlotItemHandler(iItemHandler, i, x, y));
         }

         int containerSlotX = inputBaseX + 3 * inputSpacing + 2;
         this.addSlot(new SlotItemHandler(iItemHandler, 6, containerSlotX, firstRowY) {
            public boolean mayPlace(ItemStack stack) {
               return stack.is(ModTags.CONTAINER_ITEMS);
            }
         });
         this.addSlot(new SlotItemHandler(iItemHandler, 7, inputBaseX + outputOffsetX, secondRowY));
         this.addSlot(new SlotItemHandler(iItemHandler, 8, inputBaseX + secondaryOutputX, secondRowY));
      }
      this.addDataSlots(data);
   }

   public boolean isCrafting() {
      return this.data.get(0) > 0;
   }

   public int getScaledProgress() {
      int progress = this.data.get(0);
      int maxProgress = this.data.get(1);
      int arrowWidth = 25;
      return maxProgress > 0 ? progress * arrowWidth / maxProgress : 0;
   }

   public ItemStack quickMoveStack(Player playerIn, int index) {
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         ItemStack originalStack = stack.copy();
         if (index < 36) {
            if (stack.is(ModTags.CONTAINER_ITEMS)) {
               if (!this.moveItemStackTo(stack, 42, 43, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(stack, 36, 42, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 36 && index < 45 && !this.moveItemStackTo(stack, 0, 36, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() != originalStack.getCount()) {
            slot.onTake(playerIn, stack);
         }

         return originalStack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean stillValid(Player pPlayer) {
      return stillValid(ContainerLevelAccess.create(this.level, this.blockEntity.getBlockPos()), pPlayer, (Block)ModBlocks.FERMENTING_JAR.get());
   }

   private void addPlayerInventory(Inventory playerInventory) {
      for (int i = 0; i < 3; i++) {
         for (int l = 0; l < 9; l++) {
            this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
         }
      }
   }

   private void addPlayerHotbar(Inventory playerInventory) {
      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }
   }
}
