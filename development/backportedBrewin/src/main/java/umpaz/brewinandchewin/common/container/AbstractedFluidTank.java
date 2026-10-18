package umpaz.brewinandchewin.common.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public interface AbstractedFluidTank {
   default long getFluidCapacity() {
      return this.getFluidCapacity(0);
   }

   long getFluidCapacity(int slot);

   AbstractedFluidStack getAbstractedFluid();

   void setAbstractedFluid(AbstractedFluidStack stack);

   AbstractedFluidStack fill(AbstractedFluidStack stack, boolean simulate);

   /** Port: how much of `stack` (in loader units) was, or would be, accepted. */
   long fillAmount(AbstractedFluidStack stack, boolean simulate);

   default AbstractedFluidStack drain(long maxDrain, FluidUnit unit, boolean simulate) {
      return this.drain(0, maxDrain, unit, simulate);
   }

   AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate);

   boolean isEmpty();

   default void readFromNbt(CompoundTag tag) {
   }

   default CompoundTag writeToNbt() {
      return new CompoundTag();
   }

   default ItemStack getContainer() {
      return ItemStack.EMPTY;
   }

   default boolean isFluidValid(AbstractedFluidStack stack) {
      return this.isFluidValid(0, stack);
   }

   default boolean isFluidValid(int slot, AbstractedFluidStack stack) {
      return false;
   }
}
