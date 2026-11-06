package com.SaintAlfonzo.creativecommon.compat.jei.cycling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws a {@link CyclingCraftingDisplay} on JEI's crafting page. All nine inputs and the output are
 * focus-linked, so they cycle in step and a lookup (say, uses of red dye) narrows every slot to the
 * variants that contain the focused stack.
 */
@ParametersAreNonnullByDefault
public class CyclingCraftingExtension implements ICraftingCategoryExtension {

    private final CyclingCraftingDisplay recipe;

    public CyclingCraftingExtension(CyclingCraftingDisplay recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper grid, IFocusGroup focuses) {
        List<IRecipeSlotBuilder> inputs = grid.createAndSetInputs(builder, recipe.slots(), 3, 3);
        IRecipeSlotBuilder output = grid.createAndSetOutputs(builder, recipe.outputs());
        if (recipe.shapeless()) {
            builder.setShapeless();
        }
        List<IIngredientAcceptor<?>> linked = new ArrayList<>(inputs);
        linked.add(output);
        builder.createFocusLink(linked.toArray(new IIngredientAcceptor<?>[0]));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getId();
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }
}
