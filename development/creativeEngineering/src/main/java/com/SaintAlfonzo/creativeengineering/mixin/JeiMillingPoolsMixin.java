package com.SaintAlfonzo.creativeengineering.mixin;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.compat.ResultPoolsJei;
import com.SaintAlfonzo.creativeengineering.fan.duck.ProcessingRecipeExt;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.MillingCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create's JEI milling page for a recipe with result pools (plan_flesh_recipes.md 5.3): one slot per pool, then
 * the outputs in no pool, in Create's two-column grid growing upwards. Recipes without pools keep Create's layout.
 * See {@link ResultPoolsJei}.
 */
@Mixin(value = MillingCategory.class, remap = false)
public class JeiMillingPoolsMixin {

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/kinetics/crusher/AbstractCrushingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void creativeengineering$poolSlots(IRecipeLayoutBuilder builder, AbstractCrushingRecipe recipe,
                                               IFocusGroup focuses, CallbackInfo ci) {
        int[][] pools = ((ProcessingRecipeExt) recipe).creativeengineering$getResultPools();
        if (pools == null) return;
        ci.cancel();

        builder.addSlot(RecipeIngredientRole.INPUT, 15, 9)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().get(0));

        List<ResultPoolsJei.Slot> slots = ResultPoolsJei.slots(recipe.getRollableResults(), pools);
        boolean single = slots.size() == 1;
        for (int n = 0; n < slots.size(); n++) {
            int x = (single ? 139 : 133) + (n % 2 == 0 ? 0 : 19);
            int y = 27 - (n / 2) * 19;
            ResultPoolsJei.fill(builder.addSlot(RecipeIngredientRole.OUTPUT, x, y), slots.get(n));
        }
    }
}
