package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IShapedRecipe;

/**
 * A crafting page of its own for one kind of craft (Canning, Using Cans: {@link CanPagesJEI}): the 3x3 grid exactly as
 * JEI's Crafting page draws it, so the + button fills a crafting table the same way.
 */
@ParametersAreNonnullByDefault
public class CanCraftingCategory implements IRecipeCategory<CraftingRecipe> {

    /** JEI's Crafting page background (CraftingRecipeCategory). */
    private static final ResourceLocation JEI_GUI = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private final RecipeType<CraftingRecipe> type;
    private final String titleKey;
    private final IDrawable background;
    private final IDrawable icon;
    private final ICraftingGridHelper grid;

    public CanCraftingCategory(IGuiHelper helper, RecipeType<CraftingRecipe> type, String titleKey, IDrawable icon) {
        this.type = type;
        this.titleKey = titleKey;
        this.icon = icon;
        background = helper.createDrawable(JEI_GUI, 0, 60, 116, 54);
        grid = helper.createCraftingGridHelper();
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(titleKey);
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
    public ResourceLocation getRegistryName(CraftingRecipe recipe) {
        return recipe.getId();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CraftingRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(Arrays.asList(ingredient.getItems()));
        }
        int width = 0;
        int height = 0;
        if (recipe instanceof IShapedRecipe<?> shaped) {
            width = shaped.getRecipeWidth();
            height = shaped.getRecipeHeight();
        }
        grid.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputs, width, height);
        grid.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(recipe.getResultItem()));
    }
}
