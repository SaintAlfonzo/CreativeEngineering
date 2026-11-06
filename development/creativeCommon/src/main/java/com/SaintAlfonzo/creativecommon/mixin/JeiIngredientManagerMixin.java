package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.compat.jei.JeiIngredientChanges;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.IngredientManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

/**
 * Records every ingredient added or removed after JEI built its list, with the plugin doing it
 * ({@link JeiIngredientChanges}), for the visibility classes of the pack order report.
 */
@Mixin(value = IngredientManager.class, remap = false)
public abstract class JeiIngredientManagerMixin {

    @Inject(method = "addIngredientsAtRuntime", at = @At("HEAD"))
    private <V> void creativecommon$recordAdd(IIngredientType<V> type, Collection<V> ingredients, CallbackInfo ci) {
        JeiIngredientChanges.record(true, (IIngredientManager) (Object) this, type, ingredients);
    }

    @Inject(method = "removeIngredientsAtRuntime", at = @At("HEAD"))
    private <V> void creativecommon$recordRemove(IIngredientType<V> type, Collection<V> ingredients, CallbackInfo ci) {
        JeiIngredientChanges.record(false, (IIngredientManager) (Object) this, type, ingredients);
    }
}
