package com.SaintAlfonzo.creativebrewing.mixin.immersiveengineering;

import blusunrize.immersiveengineering.common.util.compat.jei.refinery.RefineryRecipeCategory;
import com.SaintAlfonzo.creativecommon.compat.PotionFluidTooltip;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * IE's Refinery JEI category renders fluids with only IE's generic name + amount tooltip. The
 * pack's "Lingerification" recipes ({@code compat/LingerificationCategory}, which extends this
 * category) carry {@code create:potion} NBT fluids, so append the potion display name and
 * per-effect lines to fluid slots carrying a {@code Potion} tag ({@link PotionFluidTooltip}).
 * Harmless on IE's own Refinery page (biodiesel / resin carry no {@code Potion} tag).
 *
 * <p>The <b>output</b> slot ends in {@code addTooltipCallback(JEIHelper.fluidTooltipCallback)} —
 * wrapped by {@link #creativeengineering$outputPotionTooltip}. The two <b>input</b> slots
 * ({@code input0}, {@code input1}) have no {@code addTooltipCallback} call at all — instead they
 * end in {@code addIngredients(FLUID, getMatchingFluidStacks())}, so
 * {@link #creativeengineering$inputPotionTooltip} redirects that (the catalyst slot uses
 * {@code addItemStacks} and the output uses the singular {@code addIngredient}, so neither is hit).
 */
@Mixin(value = RefineryRecipeCategory.class, remap = false)
public class RefineryRecipeCategoryMixin {

    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;"
                   + "Lblusunrize/immersiveengineering/api/crafting/RefineryRecipe;"
                   + "Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
                           + "addTooltipCallback(Lmezz/jei/api/gui/ingredient/IRecipeSlotTooltipCallback;)"
                           + "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
            )
    )
    private IRecipeSlotBuilder creativeengineering$outputPotionTooltip(
            IRecipeSlotBuilder slot, IRecipeSlotTooltipCallback ieCallback) {

        return slot.addTooltipCallback(ieCallback).addTooltipCallback(PotionFluidTooltip.POTION_EFFECTS);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;"
                   + "Lblusunrize/immersiveengineering/api/crafting/RefineryRecipe;"
                   + "Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
                           + "addIngredients(Lmezz/jei/api/ingredients/IIngredientType;Ljava/util/List;)"
                           + "Lmezz/jei/api/gui/builder/IIngredientAcceptor;"
            )
    )
    private IIngredientAcceptor creativeengineering$inputPotionTooltip(
            IRecipeSlotBuilder slot, IIngredientType type, List ingredients) {

        return slot.addIngredients(type, ingredients)
                .addTooltipCallback(PotionFluidTooltip.POTION_EFFECTS);
    }
}
