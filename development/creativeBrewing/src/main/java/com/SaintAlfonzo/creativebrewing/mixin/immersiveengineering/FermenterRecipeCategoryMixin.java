package com.SaintAlfonzo.creativebrewing.mixin.immersiveengineering;

import blusunrize.immersiveengineering.common.util.compat.jei.fermenter.FermenterRecipeCategory;
import com.SaintAlfonzo.creativecommon.compat.PotionFluidTooltip;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Consistency with the other IE fluid categories ({@code MixerRecipeCategoryMixin},
 * {@code BottlingMachineRecipeCategoryMixin}, {@code RefineryRecipeCategoryMixin}): append the
 * potion display name + per-effect lines to any fluid slot carrying a {@code Potion} tag
 * ({@link PotionFluidTooltip}). Inert in this pack (the Fermenter only outputs ethanol), but
 * future-proof and harmless — the callback is a no-op for non-potion fluids.
 *
 * <p>{@code FermenterRecipeCategory.setRecipe} has a single {@code addTooltipCallback} call, on
 * the fluid output slot.
 */
@Mixin(value = FermenterRecipeCategory.class, remap = false)
public class FermenterRecipeCategoryMixin {

    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;"
                   + "Lblusunrize/immersiveengineering/api/crafting/FermenterRecipe;"
                   + "Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
                           + "addTooltipCallback(Lmezz/jei/api/gui/ingredient/IRecipeSlotTooltipCallback;)"
                           + "Lmezz/jei/api/gui/builder/IRecipeSlotBuilder;"
            )
    )
    private IRecipeSlotBuilder creativeengineering$potionEffectTooltip(
            IRecipeSlotBuilder slot, IRecipeSlotTooltipCallback ieCallback) {

        return slot.addTooltipCallback(ieCallback).addTooltipCallback(PotionFluidTooltip.POTION_EFFECTS);
    }
}
