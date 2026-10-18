package com.SaintAlfonzo.creativeengineering.oven.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.SaintAlfonzo.creativeengineering.oven.BakingRecipe;
import com.SaintAlfonzo.creativeengineering.oven.OvenRegistry;
import com.SaintAlfonzo.creativeengineering.oven.client.OvenScreen;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * FD's CookingRecipeCategory layout (drawn from FD's cooking_pot.png) shifted right to make room for the tool slot on
 * the left. The tool is a CATALYST slot, so JEI's "+" transfer only moves the six ingredients.
 */
public class BakingRecipeCategory implements IRecipeCategory<BakingRecipe> {

    public static final RecipeType<BakingRecipe> TYPE = RecipeType.create(OvenRegistry.BAKING_ID.getNamespace(), OvenRegistry.BAKING_ID.getPath(), BakingRecipe.class);

    // The FD panel (texture 29,16 116x56) starts this far right; the tool slot sits in the gap.
    private static final int SHIFT = 22;

    private final IDrawable background;
    private final IDrawable panel;
    private final IDrawable slotFrame;
    private final IDrawable icon;
    private final IDrawable heatIndicator;
    private final IDrawable timeIcon;
    private final IDrawable expIcon;
    private final IDrawableAnimated arrow;

    public BakingRecipeCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(116 + SHIFT, 56);
        panel = helper.createDrawable(OvenScreen.BACKGROUND, 29, 16, 116, 56);
        slotFrame = helper.createDrawable(OvenScreen.BACKGROUND, 91, 54, 18, 18);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(OvenRegistry.OVEN_ITEM.get()));
        heatIndicator = helper.createDrawable(OvenScreen.BACKGROUND, 176, 0, 17, 15);
        timeIcon = helper.createDrawable(OvenScreen.BACKGROUND, 176, 32, 8, 11);
        expIcon = helper.createDrawable(OvenScreen.BACKGROUND, 176, 43, 9, 9);
        arrow = helper.drawableBuilder(OvenScreen.BACKGROUND, 176, 15, 24, 17)
                .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<BakingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.creativeengineering.baking");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BakingRecipe recipe, IFocusGroup focuses) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 3; ++column) {
                int index = row * 3 + column;
                if (index < ingredients.size()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, SHIFT + column * 18 + 1, row * 18 + 1)
                            .addItemStacks(Arrays.asList(ingredients.get(index).getItems()));
                }
            }
        }
        if (recipe.hasTool()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 1, 10)
                    .addItemStacks(Arrays.asList(recipe.getTool().getItems()));
        }
        ItemStack result = recipe.getResultItem();
        builder.addSlot(RecipeIngredientRole.OUTPUT, SHIFT + 95, 10).addItemStack(result);
        ItemStack container = recipe.getOutputContainer();
        if (!container.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, SHIFT + 63, 39).addItemStack(container);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, SHIFT + 95, 39).addItemStack(result);
    }

    @Override
    public void draw(BakingRecipe recipe, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        panel.draw(poseStack, SHIFT, 0);
        if (recipe.hasTool()) {
            slotFrame.draw(poseStack, 0, 9);
        }
        arrow.draw(poseStack, SHIFT + 60, 9);
        heatIndicator.draw(poseStack, SHIFT + 18, 39);
        timeIcon.draw(poseStack, SHIFT + 64, 2);
        if (recipe.getExperience() > 0) {
            expIcon.draw(poseStack, SHIFT + 63, 21);
        }
    }

    @Override
    public List<Component> getTooltipStrings(BakingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= SHIFT + 61 && mouseX < SHIFT + 83 && mouseY >= 2 && mouseY < 30) {
            List<Component> lines = new ArrayList<>();
            if (recipe.getCookTime() > 0) {
                lines.add(Component.translatable("gui.jei.category.smelting.time.seconds", recipe.getCookTime() / 20));
            }
            if (recipe.getExperience() > 0) {
                lines.add(Component.translatable("gui.jei.category.smelting.experience", recipe.getExperience()));
            }
            return lines;
        }
        if (mouseX >= SHIFT + 18 && mouseX < SHIFT + 35 && mouseY >= 39 && mouseY < 54) {
            return List.of(Component.translatable("jei.creativeengineering.baking.heat"));
        }
        return Collections.emptyList();
    }
}
