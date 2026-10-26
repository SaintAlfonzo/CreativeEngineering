package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleBases;
import com.SaintAlfonzo.creativedelight.compat.farmersrespite.KettleTank;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import umpaz.farmersrespite.integration.jei.category.BrewingRecipeCategory;

/**
 * Farmer's Respite's JEI Kettle page (farmers_respite.md K8): the recipe's base fluid sits in the water bar as a
 * fluid input, one unit of a four-unit bar, so JEI finds a brew from its tea as well as from its items. 2.0's
 * fixed water bar picture is no longer drawn. Applies only when JEI loads the category.
 */
@Mixin(value = BrewingRecipeCategory.class, remap = false)
public abstract class KettleJeiBaseMixin {

	@Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lumpaz/farmersrespite/common/crafting/KettleRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
			at = @At("TAIL"), require = 1)
	private void ce$baseSlot(IRecipeLayoutBuilder builder, KettleRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
		FluidStack base = KettleBases.get(recipe);
		if (base.isEmpty()) return;
		builder.addSlot(RecipeIngredientRole.INPUT, 5, 1)
				.setFluidRenderer(KettleTank.CAPACITY, false, 5, 32)
				.addIngredient(mezz.jei.api.forge.ForgeTypes.FLUID_STACK, base.copy());
	}

	@Redirect(method = "draw(Lumpaz/farmersrespite/common/crafting/KettleRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lcom/mojang/blaze3d/vertex/PoseStack;DD)V",
			require = 1, at = @At(value = "INVOKE", target = "Lumpaz/farmersrespite/common/crafting/KettleRecipe;getNeedWater()Z"))
	private boolean ce$noWaterPicture(KettleRecipe recipe) {
		return false;
	}
}
