package com.SaintAlfonzo.creativeengineering.mixin;

import blusunrize.immersiveengineering.common.util.compat.jei.bottlingmachine.BottlingMachineRecipeCategory;
import com.SaintAlfonzo.creativeengineering.compat.PotionFluidTooltip;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The pack's per-potion bottling recipes ({@code kubejs/.../engineering/potion_bottling.js}) feed
 * Create's {@code create:potion} NBT fluid into the IE Bottling Machine. IE's JEI category shows
 * only the generic fluid name + amount on that input slot, so append the potion display name and
 * per-effect lines ({@link PotionFluidTooltip}).
 *
 * <p>{@code BottlingMachineRecipeCategory.setRecipe} has a single
 * {@code addTooltipCallback(JEIHelper.fluidTooltipCallback)} call and it is on the fluid input
 * slot — redirect it.
 */
@Mixin(value = BottlingMachineRecipeCategory.class, remap = false)
public class BottlingMachineRecipeCategoryMixin {

    @Redirect(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;"
                   + "Lblusunrize/immersiveengineering/api/crafting/BottlingMachineRecipe;"
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
