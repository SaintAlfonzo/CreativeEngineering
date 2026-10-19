package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

/**
 * Hopper and pipe access: the top reaches the ingredient slots (0-3); every other side puts containers in and takes
 * results out. Port (alcohol.md 8.4): the sides also feed the fluid slot (6) and drain its empty containers (7). Which of
 * slots 4 and 6 an item may enter is the keg inventory's own isItemValid, so filled containers land in 6.
 */
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
      if (this.side != null && !this.side.equals(Direction.UP) && isSideOutput(slot)) {
         return this.itemHandler.getStackInSlot(slot);
      } else {
         return this.side != null && this.side.equals(Direction.UP) && slot < 4 ? this.itemHandler.getStackInSlot(slot) : ItemStack.EMPTY;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.side != null && !this.side.equals(Direction.UP) && isSideInput(slot)) {
         this.itemHandler.setStackInSlot(slot, stack);
      } else if (this.side != null && this.side.equals(Direction.UP) && slot < 4) {
         this.itemHandler.setStackInSlot(slot, stack);
      }
   }

   @NotNull
   @Override
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      if (this.side != null && !this.side.equals(Direction.UP)) {
         return isSideInput(slot) ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
      } else {
         return slot < 4 ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
      }
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      if (this.side != null && !this.side.equals(Direction.UP)) {
         return isSideOutput(slot) ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      } else {
         return slot < 4 ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      }
   }

   private static boolean isSideInput(int slot) {
      return slot == KegBlockEntity.CONTAINER_SLOT || slot == KegBlockEntity.FLUID_IN_SLOT;
   }

   private static boolean isSideOutput(int slot) {
      return slot == KegBlockEntity.OUTPUT_SLOT || slot == KegBlockEntity.FLUID_OUT_SLOT;
   }

   @Override
   public int getSlotLimit(int slot) {
      return this.itemHandler.getSlotLimit(slot);
   }
}
