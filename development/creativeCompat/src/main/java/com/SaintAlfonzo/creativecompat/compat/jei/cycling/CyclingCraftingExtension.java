package com.SaintAlfonzo.creativecompat.compat.jei.cycling;

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
        // JEI links only slots that hold the same number of stacks: the filled cells (every variant has a stack there)
        // and the output. Blank cells hold none, and a cell that is blank in some variants is kept off the link by the
        // page split in CraftingProbe.page, so this check is a guard against a page that still mixes them.
        int variants = recipe.outputs().size();
        List<IIngredientAcceptor<?>> linked = new ArrayList<>(inputs.size() + 1);
        for (int i = 0; i < inputs.size() && i < recipe.slots().size(); i++) {
            long filled = recipe.slots().get(i).stream().filter(s -> !s.isEmpty()).count();
            if (filled == variants) {
                linked.add(inputs.get(i));
            }
        }
        linked.add(output);
        if (linked.size() > 1) {
            builder.createFocusLink(linked.toArray(new IIngredientAcceptor<?>[0]));
        }
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
