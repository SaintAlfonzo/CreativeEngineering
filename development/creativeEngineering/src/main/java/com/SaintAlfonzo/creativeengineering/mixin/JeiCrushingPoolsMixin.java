package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.compat.ResultPoolsJei;
import com.SaintAlfonzo.creativeengineering.fan.duck.ProcessingRecipeExt;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CrushingCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.foundation.ponder.ui.LayoutHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create's JEI crushing page for a recipe with result pools (plan_flesh_recipes.md 5.3): one slot per pool, then
 * the outputs in no pool, centred in a row as Create lays them out (wrapping past 9). Recipes without pools keep
 * Create's layout. See {@link ResultPoolsJei}.
 */
@Mixin(value = CrushingCategory.class, remap = false)
public class JeiCrushingPoolsMixin {

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/kinetics/crusher/AbstractCrushingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void creativeengineering$poolSlots(IRecipeLayoutBuilder builder, AbstractCrushingRecipe recipe,
                                               IFocusGroup focuses, CallbackInfo ci) {
        int[][] pools = ((ProcessingRecipeExt) recipe).creativeengineering$getResultPools();
        if (pools == null) return;
        ci.cancel();

        builder.addSlot(RecipeIngredientRole.INPUT, 51, 3)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ResultPoolsJei.Slot> slots = ResultPoolsJei.slots(recipe.getRollableResults(), pools);
        int perRow = Math.min(slots.size(), 9);
        int rows = (slots.size() + perRow - 1) / perRow;
        int xOffset = ((IRecipeCategory<?>) (Object) this).getBackground().getWidth() / 2;
        LayoutHelper layout = LayoutHelper.centeredHorizontal(perRow, rows, 18, 18, 1);
        for (ResultPoolsJei.Slot slot : slots) {
            ResultPoolsJei.fill(builder.addSlot(RecipeIngredientRole.OUTPUT, xOffset + layout.getX(), 86 + layout.getY()), slot);
            layout.next();
        }
    }
}
