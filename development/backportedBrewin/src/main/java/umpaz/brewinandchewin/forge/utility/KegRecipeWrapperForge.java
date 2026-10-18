package umpaz.brewinandchewin.forge.utility;

import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

public class KegRecipeWrapperForge extends RecipeWrapper implements KegRecipeWrapper {
   private final AbstractedFluidTank tank;

   public KegRecipeWrapperForge(IItemHandlerModifiable itemHandler, AbstractedFluidTank fluidHandler) {
      super(itemHandler);
      this.tank = fluidHandler;
   }

   @Override
   public AbstractedFluidStack getFluid() {
      return this.tank.getAbstractedFluid();
   }

   @Override
   public long getTankCapacity() {
      return this.tank.getFluidCapacity();
   }

   // Upstream KegRecipeWrapper.isEmpty (sic): an empty tank never counts as an empty input.
   @Override
   public boolean isEmpty() {
      return this.getFluid().isEmpty() ? false : super.isEmpty();
   }
}
