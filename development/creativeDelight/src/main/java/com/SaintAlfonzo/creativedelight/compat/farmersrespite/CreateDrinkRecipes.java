package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import java.util.List;
import java.util.Map;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Reads the glass bottle <-> 250 mB drink pairs from Create's filling and emptying recipes for {@link KettleDrinks}.
 * Only single-item, untagged pairs count: a filling recipe whose item input is exactly a glass bottle and whose fluid
 * input is 250 mB, an emptying recipe from one drink item to a glass bottle plus 250 mB of an untagged fluid. When two
 * drinks fill from one fluid the first recipe read wins (the pack keeps one per fluid).
 */
final class CreateDrinkRecipes {

	private CreateDrinkRecipes() {}

	static void read(RecipeManager rm, Map<Item, FluidStack> byBottle, Map<Fluid, ItemStack> byFluid) {
		for (Recipe<?> r : rm.getRecipes()) {
			if (r instanceof EmptyingRecipe er && r.getType() == AllRecipeTypes.EMPTYING.getType()) {
				readEmptying(er, byBottle);
			} else if (r instanceof FillingRecipe fr && r.getType() == AllRecipeTypes.FILLING.getType()) {
				readFilling(fr, byFluid);
			}
		}
	}

	private static void readEmptying(EmptyingRecipe er, Map<Item, FluidStack> byBottle) {
		if (er.getIngredients().isEmpty() || er.getFluidResults().isEmpty()) return;
		FluidStack out = er.getFluidResults().get(0);
		if (out.isEmpty() || out.hasTag() || out.getAmount() != KettleTank.UNIT) return;
		List<ItemStack> rest = er.getRollableResultsAsItemStacks();
		if (rest.size() != 1 || !rest.get(0).is(Items.GLASS_BOTTLE)) return;
		ItemStack[] drinks = er.getIngredients().get(0).getItems();
		if (drinks.length != 1) return;
		byBottle.putIfAbsent(drinks[0].getItem(), new FluidStack(out.getFluid(), KettleTank.UNIT));
	}

	private static void readFilling(FillingRecipe fr, Map<Fluid, ItemStack> byFluid) {
		if (fr.getIngredients().isEmpty() || fr.getFluidIngredients().isEmpty()) return;
		Ingredient bottle = fr.getIngredients().get(0);
		ItemStack[] items = bottle.getItems();
		if (items.length != 1 || !items[0].is(Items.GLASS_BOTTLE)) return;
		FluidIngredient fluid = fr.getFluidIngredients().get(0);
		if (fluid.getRequiredAmount() != KettleTank.UNIT) return;
		ItemStack drink = fr.getResultItem();
		if (drink.isEmpty() || drink.getCount() != 1) return;
		for (FluidStack fs : fluid.getMatchingFluidStacks()) {
			if (!fs.hasTag()) byFluid.putIfAbsent(fs.getFluid(), drink.copy());
		}
	}
}
