package umpaz.brewinandchewin.forge.container;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

/**
 * Port (alcohol.md 8.7): the keg's tank as pipes, pumps and other blocks see it. Upstream hands out the plain tank, so a
 * pump could pull the base fluid out mid-batch and a pipe could push in any fluid. Here pipes only fill fluids the keg
 * uses (KegBlockEntity#isKegFluid) and, while {@code pipeDrainOnlyWhenIdle} is on, only drain while it isn't fermenting.
 * The keg's own GUI, the fluid slot and in-world pouring use the tank directly and aren't limited.
 */
public class KegPipeFluidHandlerForge implements IFluidHandler {
   private final KegFluidTankForge tank;
   private final KegBlockEntity keg;

   public KegPipeFluidHandlerForge(KegFluidTankForge tank, KegBlockEntity keg) {
      this.tank = tank;
      this.keg = keg;
   }

   @Override
   public int getTanks() {
      return this.tank.getTanks();
   }

   @NotNull
   @Override
   public FluidStack getFluidInTank(int tank) {
      return this.tank.getFluidInTank(tank);
   }

   @Override
   public int getTankCapacity(int tank) {
      return this.tank.getTankCapacity(tank);
   }

   @Override
   public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return this.tank.isFluidValid(tank, stack) && this.keg.isKegFluid(KegFluidTankForge.wrap(stack));
   }

   @Override
   public int fill(FluidStack resource, FluidAction action) {
      return resource.isEmpty() || !this.keg.isKegFluid(KegFluidTankForge.wrap(resource)) ? 0 : this.tank.fill(resource, action);
   }

   @NotNull
   @Override
   public FluidStack drain(FluidStack resource, FluidAction action) {
      return this.canDrain() ? this.tank.drain(resource, action) : FluidStack.EMPTY;
   }

   @NotNull
   @Override
   public FluidStack drain(int maxDrain, FluidAction action) {
      return this.canDrain() ? this.tank.drain(maxDrain, action) : FluidStack.EMPTY;
   }

   private boolean canDrain() {
      return !BnCConfiguration.COMMON_CONFIG.get().keg().pipeDrainOnlyWhenIdle() || !this.keg.isFermenting();
   }
}
