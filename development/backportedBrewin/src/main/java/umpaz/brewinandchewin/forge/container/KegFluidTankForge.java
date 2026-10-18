package umpaz.brewinandchewin.forge.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public class KegFluidTankForge extends FluidTank implements AbstractedFluidTank {
   public KegFluidTankForge(int capacity) {
      super(capacity);
   }

   @Override
   public long getFluidCapacity(int slot) {
      return this.getCapacity();
   }

   @Override
   public AbstractedFluidStack getAbstractedFluid() {
      return wrap(this.fluid);
   }

   @Override
   public void setAbstractedFluid(AbstractedFluidStack stack) {
      this.setFluid(unwrap(stack));
   }

   @Override
   public AbstractedFluidStack fill(AbstractedFluidStack fluidStack, boolean simulate) {
      this.fill(unwrap(fluidStack), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
      return this.getAbstractedFluid();
   }

   @Override
   public long fillAmount(AbstractedFluidStack fluidStack, boolean simulate) {
      return this.fill(unwrap(fluidStack), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
   }

   @Override
   public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
      int newAmount = (int)unit.convertToLoader(maxDrain);
      FluidStack fluid = this.drain(newAmount, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
      return wrap(fluid);
   }

   @Override
   public void readFromNbt(CompoundTag tag) {
      this.readFromNBT(tag);
   }

   @Override
   public CompoundTag writeToNbt() {
      return this.writeToNBT(new CompoundTag());
   }

   public static AbstractedFluidStack wrap(FluidStack fluid) {
      if (fluid.isEmpty()) {
         return AbstractedFluidStack.EMPTY;
      }
      return new AbstractedFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getTag(), FluidUnit.MILLIBUCKET, fluid);
   }

   public static FluidStack unwrap(AbstractedFluidStack stack) {
      if (stack.isEmpty()) {
         return FluidStack.EMPTY;
      }
      Object loader = stack.loaderSpecific();
      if (loader instanceof FluidStack fluidStack && fluidStack.getAmount() == (int)stack.unit().convertToLoader(stack.amount())) {
         return fluidStack;
      }
      return new FluidStack(stack.fluid(), (int)stack.unit().convertToLoader(stack.amount()), stack.tag());
   }
}
