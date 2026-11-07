package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.jei.PackIngredientOrder;
import mezz.jei.gui.ingredients.IngredientFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pack ingredient order ({@link PackIngredientOrder}): remembers the new ingredient list and JEI's own order of it,
 * and re-sorts when a mod adds ingredients after JEI started (JEI gives a late element its created index as its
 * position, which puts it after everything else).
 */
@Mixin(value = IngredientFilter.class, remap = false)
public abstract class JeiIngredientFilterMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void creativecommon$remember(CallbackInfo ci) {
        PackIngredientOrder.onFilterCreated((IngredientFilter) (Object) this);
    }

    @Inject(method = "onIngredientsAdded", at = @At("RETURN"))
    private void creativecommon$resortAdded(CallbackInfo ci) {
        PackIngredientOrder.onIngredientsAdded((IngredientFilter) (Object) this);
    }
}
