package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.Container;

// Port: 1.21 RecipeInput -> 1.19.2 Container (forge/utility/KegRecipeWrapperForge extends Forge's RecipeWrapper).
public interface KegRecipeWrapper extends Container {
   AbstractedFluidStack getFluid();

   long getTankCapacity();
}
