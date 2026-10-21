package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

/**
 * Hopper and pipe access. Every face puts items in (TODO alcohol/brewinport #7): ingredients (0-3), containers to pour
 * into (4) and filled containers for the tank (6); which slot an item may enter is the keg inventory's own isItemValid.
 * On the sides and bottom a filled container skips the ingredient slots, so it lands in the fluid slot; from the top it
 * may still go in as an ingredient. The top takes ingredients back out; the other faces take the results (5), the fluid
 * slot's empties (7) and the ingredient leftovers (8).
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
      return this.canInsert(slot) || this.canExtract(slot) ? this.itemHandler.getStackInSlot(slot) : ItemStack.EMPTY;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.canInsert(slot)) {
         this.itemHandler.setStackInSlot(slot, stack);
      }
   }

   @NotNull
   @Override
   public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      if (!this.canInsert(slot)) {
         return stack;
      }
      if (slot < 4 && !this.isTop() && this.itemHandler.isItemValid(KegBlockEntity.FLUID_IN_SLOT, stack)) {
         return stack;
      }
      return this.itemHandler.insertItem(slot, stack, simulate);
   }

   @NotNull
   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate) {
      return this.canExtract(slot) ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
   }

   private boolean isTop() {
      return this.side != null && this.side.equals(Direction.UP);
   }

   private boolean canInsert(int slot) {
      return this.side != null && (slot < 4 || slot == KegBlockEntity.CONTAINER_SLOT || slot == KegBlockEntity.FLUID_IN_SLOT);
   }

   private boolean canExtract(int slot) {
      if (this.side == null) {
         return false;
      }
      return this.isTop()
         ? slot < 4
         : slot == KegBlockEntity.OUTPUT_SLOT || slot == KegBlockEntity.FLUID_OUT_SLOT || slot == KegBlockEntity.REMAINDER_SLOT;
   }

   @Override
   public int getSlotLimit(int slot) {
      return this.itemHandler.getSlotLimit(slot);
   }
}
