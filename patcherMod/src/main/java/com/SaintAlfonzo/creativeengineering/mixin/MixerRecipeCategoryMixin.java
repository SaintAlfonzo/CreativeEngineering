package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.common.util.compat.jei.mixer.MixerRecipeCategory;
import com.SaintAlfonzo.creativeengineering.compat.PotionFluidTooltip;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * IE's Mixer JEI category renders potion fluids with only IE's generic name + amount tooltip.
 * This pack routes every IE-Mixer potion recipe through Create's {@code create:potion} NBT fluid
 * (see {@code PotionRecipeGeneratorsMixin}), so append the potion display name and per-effect
 * duration / amplifier lines whenever a fluid slot's stack carries a {@code Potion} tag
 * ({@link PotionFluidTooltip}).
 *
 * <p>Wraps both {@code addTooltipCallback(JEIHelper.fluidTooltipCallback)} calls in
 * {@code setRecipe} (input + output fluid slot); the item-reagent slot has no such call.
 */
@Mixin(value = MixerRecipeCategory.class, remap = false)
public class MixerRecipeCategoryMixin {

    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;"
                   + "Lblusunrize/immersiveengineering/api/crafting/MixerRecipe;"
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
