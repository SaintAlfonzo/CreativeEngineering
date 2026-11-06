package com.SaintAlfonzo.creativecommon.mixin;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;

/**
 * The missing input slots behind a greyed-out JEI + button, for
 * {@code compat.jei.CraftableFirstRecipeLayoutList}'s most-complete-first ordering
 * (JEI 19 exposes the count as {@code IRecipeTransferError.getMissingCountHint()}; 11.8.1 doesn't).
 */
@Mixin(value = RecipeTransferErrorMissingSlots.class, remap = false)
public interface JeiMissingSlotsErrorAccessor {
    @Accessor("slots")
    Collection<IRecipeSlotView> creativecommon$getSlots();
}
