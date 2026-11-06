package com.SaintAlfonzo.creativecommon.mixin;

import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.search.IElementSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * The full element list and the ingredient manager behind JEI's ingredient list, and its change notification,
 * for {@code compat.jei.PackIngredientOrder}'s re-sort.
 */
@Mixin(value = IngredientFilter.class, remap = false)
public interface JeiIngredientFilterAccessor {
    @Accessor("elementSearch")
    IElementSearch creativecommon$getElementSearch();

    @Accessor("ingredientManager")
    IIngredientManager creativecommon$getIngredientManager();

    @Invoker("notifyListenersOfChange")
    void creativecommon$notifyListenersOfChange();
}
