package com.SaintAlfonzo.cdevcapture.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * JEI side of the driver: the {@code jei} wait (runtime available) and the {@code jei_recipes}, {@code jei_uses},
 * {@code jei_category} screens. Only called when JEI is loaded (CaptureDriver checks ModList).
 */
@JeiPlugin
public class CaptureJeiPlugin implements IModPlugin {

	private static volatile IJeiRuntime runtime;

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation("cdevcapture", "driver");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
	}

	public static boolean ready() {
		return runtime != null;
	}

	public static void showRecipes(ItemStack stack, boolean uses) {
		IJeiRuntime r = require();
		var ff = r.getJeiHelpers().getFocusFactory();
		if (uses) {
			// JEI's own "uses" view: input and catalyst focuses, so a stack that is only a catalyst still shows.
			r.getRecipesGui().show(List.of(
					ff.createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, stack),
					ff.createFocus(RecipeIngredientRole.CATALYST, VanillaTypes.ITEM_STACK, stack)));
		} else {
			r.getRecipesGui().show(ff.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
		}
	}

	public static void showCategory(String uid) {
		IJeiRuntime r = require();
		Optional<RecipeType<?>> type = r.getRecipeManager().getRecipeType(new ResourceLocation(uid));
		if (type.isEmpty()) throw new IllegalArgumentException("no JEI category " + uid);
		r.getRecipesGui().showTypes(List.of(type.get()));
	}

	private static IJeiRuntime require() {
		IJeiRuntime r = runtime;
		if (r == null) throw new IllegalStateException("JEI runtime not available");
		return r;
	}
}
