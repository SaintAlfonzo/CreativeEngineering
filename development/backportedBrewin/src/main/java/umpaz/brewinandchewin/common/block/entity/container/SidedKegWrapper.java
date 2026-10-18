package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

public abstract class SidedKegWrapper implements AbstractedItemHandler {
   protected final AbstractedItemHandler itemHandler;
   protected final Direction side;

   public SidedKegWrapper(AbstractedItemHandler itemHandler, @Nullable Direction side) {
      this.itemHandler = itemHandler;
      this.side = side;
   }

   @Override
   public boolean isItemValid(int slot, ItemStack stack) {
      return this.itemHandler.isItemValid(slot, stack);
   }

   @Override
   public int getSlotCount() {
      return this.itemHandler.getSlotCount();
   }

   @NotNull
   @Override
   public ItemStack getStackInSlot(int slot) {
      if (this.side != null && !this.side.equals(Direction.UP) && slot == 5) {
         return this.itemHandler.getStackInSlot(slot);
      } else {
         return this.side != null && this.side.equals(Direction.UP) && slot < 4 ? this.itemHandler.getStackInSlot(slot) : ItemStack.EMPTY;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.side != null && !this.side.equals(Direction.UP) && slot == 4) {
         this.itemHandler.setStackInSlot(slot, stack);
      } else if (this.side != null && this.side.equals(Direction.UP) && slot < 4) {
         this.itemHandler.setStackInSlot(slot, stack);
      }
   }

   @NotNull
   @Override
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      if (this.side != null && !this.side.equals(Direction.UP)) {
         return slot == 4 ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
      } else {
         return slot < 4 ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
      }
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (this.side != null && !this.side.equals(Direction.UP)) {
         return slot == 5 ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      } else {
         return slot < 4 ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      }
   }

   @Override
   public int getSlotLimit(int slot) {
      return this.itemHandler.getSlotLimit(slot);
   }
}
