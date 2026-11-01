package umpaz.brewinandchewin.forge.container;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public class KegFluidItemStorageForge implements AbstractedFluidTank {
   private final IFluidHandlerItem storage;

   public KegFluidItemStorageForge(IFluidHandlerItem storage) {
      this.storage = storage;
   }

   public static KegFluidItemStorageForge of(ItemStack stack) {
      return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(KegFluidItemStorageForge::new).orElse(null);
   }

   @Override
   public long getFluidCapacity() {
      return this.storage.getTankCapacity(0);
   }

   @Override
   public long getFluidCapacity(int slot) {
      return this.storage.getTankCapacity(slot);
   }

   @Override
   public AbstractedFluidStack getAbstractedFluid() {
      return KegFluidTankForge.wrap(this.storage.getFluidInTank(0));
   }

   @Override
   public void setAbstractedFluid(AbstractedFluidStack stack) {
      this.storage.drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
      this.storage.fill(KegFluidTankForge.unwrap(stack), FluidAction.EXECUTE);
   }

   @Override
   public AbstractedFluidStack fill(AbstractedFluidStack stack, boolean simulate) {
      this.storage.fill(KegFluidTankForge.unwrap(stack), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
      return this.getAbstractedFluid();
   }

   @Override
   public long fillAmount(AbstractedFluidStack stack, boolean simulate) {
      return this.storage.fill(KegFluidTankForge.unwrap(stack), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
   }

   @Override
   public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
      // Port: upstream returns the stack left in the item after draining (it is used as "what came out" by its
      // callers only for full containers, where the two are the same). Return what was drained instead.
      FluidStack drained = this.storage.drain((int)unit.convertToLoader(maxDrain), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
      return KegFluidTankForge.wrap(drained);
   }

   @Override
   public ItemStack getContainer() {
      return this.storage.getContainer();
   }

   @Override
   public boolean isEmpty() {
      return this.storage.getFluidInTank(0).isEmpty();
   }

   @Override
   public boolean isFluidValid(int slot, AbstractedFluidStack stack) {
      FluidStack fluid = KegFluidTankForge.unwrap(stack);
      return this.storage instanceof FluidBucketWrapper bucketWrapper ? bucketWrapper.canFillFluidType(fluid) : this.storage.isFluidValid(slot, fluid);
   }
}
