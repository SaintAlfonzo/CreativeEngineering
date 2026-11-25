package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * {@code farmingforblockheads:shipping_bin}: what one item is worth in the bin.
 * <pre>{"type": "farmingforblockheads:shipping_bin", "input": {"item": "minecraft:wheat"}, "value": 4}</pre>
 * Written by {@code global.NCH.shippingBin(input, value, e)}. Items with no row are never sold. {@link #matches} looks at
 * slot 0 of the container (the bin asks per stack, {@link #valueOf}).
 */
public class ShippingBinRecipe implements Recipe<Container> {
	private final ResourceLocation id;
	private final Ingredient input;
	private final int value;

	public ShippingBinRecipe(ResourceLocation id, Ingredient input, int value) {
		this.id = id;
		this.input = input;
		this.value = value;
	}

	public Ingredient input() {
		return input;
	}

	public int value() {
		return value;
	}

	/** Points one of this item is worth, 0 for no row (the first matching row wins, in recipe id order). */
	public static int valueOf(Level level, ItemStack stack) {
		ShippingBinRecipe recipe = find(level, stack);
		return recipe == null ? 0 : recipe.value;
	}

	@Nullable
	public static ShippingBinRecipe find(Level level, ItemStack stack) {
		if (stack.isEmpty()) return null;
		List<ShippingBinRecipe> all = level.getRecipeManager().getAllRecipesFor(ShippingBin.SHIPPING_BIN_RECIPE.get());
		ShippingBinRecipe best = null;
		for (ShippingBinRecipe recipe : all) {
			if (recipe.input.test(stack) && (best == null || recipe.id.compareTo(best.id) < 0)) best = recipe;
		}
		return best;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return input.test(container.getItem(0));
	}

	@Override
	public ItemStack assemble(Container container) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	@Override
	public boolean isSpecial() {
		return true; // keeps it out of the recipe book
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ShippingBin.SHIPPING_BIN_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ShippingBin.SHIPPING_BIN_RECIPE.get();
	}

	public static class Serializer implements RecipeSerializer<ShippingBinRecipe> {
		@Override
		public ShippingBinRecipe fromJson(ResourceLocation id, JsonObject json) {
			Ingredient input = Ingredient.fromJson(GsonHelper.isArrayNode(json, "input")
					? GsonHelper.getAsJsonArray(json, "input") : GsonHelper.getAsJsonObject(json, "input"));
			int value = GsonHelper.getAsInt(json, "value");
			if (value < 0) throw new IllegalArgumentException("shipping_bin recipe " + id + ": value must be 0 or more, got " + value);
			return new ShippingBinRecipe(id, input, value);
		}

		@Override
		public ShippingBinRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			return new ShippingBinRecipe(id, Ingredient.fromNetwork(buf), buf.readVarInt());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, ShippingBinRecipe recipe) {
			recipe.input.toNetwork(buf);
			buf.writeVarInt(recipe.value);
		}
	}
}
