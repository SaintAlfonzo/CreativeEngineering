package com.SaintAlfonzo.creativecompat.compat;

import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;

/**
 * Shared JEI slot tooltip (Common; Creative Brewing's IE category mixins and RowPagesCategory attach it): when a fluid slot displays a Create {@code create:potion} stack (i.e.
 * it carries a {@code Potion} NBT tag), replace the generic first line with the potion's display
 * name and append the per-effect duration / amplifier lines — exactly what Create's own JEI
 * categories do via {@code PotionFluidHandler.addPotionTooltip}.
 *
 * <p>Attached (after IE's own fluid tooltip callback) by the IE Mixer / Bottling Machine /
 * Refinery / Squeezer / Fermenter category mixins. A pure no-op for any fluid without a
 * {@code Potion} tag, so it is safe to attach to every fluid slot regardless of what the recipe
 * actually uses.
 */
public final class PotionFluidTooltip {

    private PotionFluidTooltip() {}

    public static final IRecipeSlotTooltipCallback POTION_EFFECTS = (view, tooltip) ->
            view.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs -> {
                if (!fs.hasTag() || fs.getTag() == null || !fs.getTag().contains("Potion")) {
                    return;
                }
                if (!tooltip.isEmpty()) {
                    tooltip.set(0, fs.getDisplayName());
                }
                PotionFluidHandler.addPotionTooltip(fs, tooltip, 1.0F);
            });
}
