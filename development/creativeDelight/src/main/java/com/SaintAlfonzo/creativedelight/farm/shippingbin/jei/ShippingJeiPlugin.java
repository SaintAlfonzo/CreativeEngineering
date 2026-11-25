package com.SaintAlfonzo.creativedelight.farm.shippingbin.jei;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBinRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** JEI "Shipping": one row per {@code farmingforblockheads:shipping_bin} recipe, the item and its points per item. */
@JeiPlugin
public class ShippingJeiPlugin implements IModPlugin {
	public static final RecipeType<ShippingBinRecipe> TYPE = RecipeType.create(ShippingBin.NAMESPACE, "shipping_bin", ShippingBinRecipe.class);

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(com.SaintAlfonzo.creativedelight.CreativeDelight.MOD_ID, "shipping_bin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (Minecraft.getInstance().level == null) return;
		List<ShippingBinRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
				.getAllRecipesFor(ShippingBin.SHIPPING_BIN_RECIPE.get()).stream()
				.sorted(Comparator.comparing(r -> r.getId().toString())).toList();
		registration.addRecipes(TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ShippingBin.SHIPPING_BIN_ITEM.get()), TYPE);
	}

	private static final class Category implements IRecipeCategory<ShippingBinRecipe> {
		private final IDrawable background;
		private final IDrawable icon;
		private final IDrawable slot;

		Category(IGuiHelper helper) {
			background = helper.createBlankDrawable(150, 20);
			icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ShippingBin.SHIPPING_BIN_ITEM.get()));
			slot = helper.getSlotDrawable();
		}

		@Override
		public RecipeType<ShippingBinRecipe> getRecipeType() {
			return TYPE;
		}

		@Override
		public Component getTitle() {
			return Component.translatable("jei.farmingforblockheads.shipping");
		}

		@Override
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return icon;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, ShippingBinRecipe recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).addItemStacks(Arrays.asList(recipe.input().getItems()));
		}

		@Override
		public void draw(ShippingBinRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
			slot.draw(pose, 1, 1);
			Minecraft.getInstance().font.draw(pose, Component.translatable("jei.farmingforblockheads.shipping.value", recipe.value(),
					com.SaintAlfonzo.creativedelight.CDConfig.SHIPPING_BIN.capacity()), 24, 6, 0xFF404040);
		}
	}
}
