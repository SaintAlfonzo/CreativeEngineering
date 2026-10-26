package com.SaintAlfonzo.creativedelight.compat.farmersrespite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import umpaz.farmersrespite.common.crafting.KettleRecipe;
import umpaz.farmersrespite.common.registry.FRRecipeTypes;

/**
 * Which drink bottles the Kettle tank takes and gives (farmers_respite.md K3, K4, K7, K10). Nothing is listed here:
 * <ul>
 *   <li>a bottle <-> fluid pair is a Create emptying recipe (drink -> glass bottle + 250 mB) and a Create filling
 *       recipe (glass bottle + 250 mB -> drink), read by {@link CreateDrinkRecipes}; so the K10 recipes are the one
 *       place a drink is wired to its fluid. A water bottle is always water;</li>
 *   <li>the tank holds water and every fluid that is the base of some Kettle recipe ({@link KettleBases}).</li>
 * </ul>
 * Rebuilt for each recipe manager (a server reload makes a new one; the client's is reset by {@link #reset()}).
 */
public final class KettleDrinks {

	private static RecipeManager builtFor;
	private static Map<Item, FluidStack> fluidOfBottle = Map.of();
	private static Map<Fluid, ItemStack> bottleOfFluid = Map.of();
	private static Set<Fluid> tankFluids = Set.of();

	private KettleDrinks() {}

	public static synchronized void reset() {
		builtFor = null;
	}

	/** The fluid one bottle of this drink pours (amount {@link KettleTank#UNIT}), or EMPTY if it is not a drink bottle. */
	public static FluidStack fluidOf(Level level, ItemStack stack) {
		if (stack.isEmpty()) return FluidStack.EMPTY;
		if (stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER) {
			return new FluidStack(Fluids.WATER, KettleTank.UNIT);
		}
		if (stack.hasTag() && !stack.is(Items.POTION)) {
			// a drink with its own data (a named or enchanted bottle) is not poured, so nothing is lost
			return FluidStack.EMPTY;
		}
		ensure(level);
		FluidStack fs = fluidOfBottle.get(stack.getItem());
		return fs == null ? FluidStack.EMPTY : fs.copy();
	}

	/** One glass bottle filled from this fluid, or EMPTY if no drink bottle holds it. */
	public static ItemStack bottleOf(Level level, FluidStack fluid) {
		if (fluid.isEmpty()) return ItemStack.EMPTY;
		if (fluid.getFluid().isSame(Fluids.WATER) && !fluid.hasTag()) {
			return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
		}
		ensure(level);
		ItemStack out = bottleOfFluid.get(fluid.getFluid());
		return out == null || fluid.hasTag() ? ItemStack.EMPTY : out.copy();
	}

	/** Whether the tank may hold this fluid at all (K3). */
	public static boolean tankHolds(Level level, FluidStack fluid) {
		if (fluid.isEmpty()) return false;
		if (fluid.getFluid().isSame(Fluids.WATER)) return true;
		ensure(level);
		return tankFluids.contains(fluid.getFluid());
	}

	/** A drink bottle the tank could take (the fill box accepts it). */
	public static boolean isPourable(Level level, ItemStack stack) {
		return tankHolds(level, fluidOf(level, stack));
	}

	private static synchronized void ensure(Level level) {
		if (level == null) return;
		RecipeManager rm = level.getRecipeManager();
		if (rm == builtFor) return;
		Map<Item, FluidStack> byBottle = new HashMap<>();
		Map<Fluid, ItemStack> byFluid = new HashMap<>();
		if (ModList.get().isLoaded("create")) {
			CreateDrinkRecipes.read(rm, byBottle, byFluid);
		}
		Set<Fluid> holds = new HashSet<>();
		for (Recipe<?> r : rm.getAllRecipesFor(FRRecipeTypes.BREWING.get())) {
			if (r instanceof KettleRecipe kr) {
				FluidStack base = KettleBases.get(kr);
				if (!base.isEmpty()) holds.add(base.getFluid());
			}
		}
		fluidOfBottle = byBottle;
		bottleOfFluid = byFluid;
		tankFluids = holds;
		builtFor = rm;
	}
}
