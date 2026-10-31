package com.SaintAlfonzo.creativeengineering.fan.duck;

/**
 * Duck interface mixed into {@code com.simibubi.create.content.processing.recipe.ProcessingRecipe}
 * to carry the optional {@code "exclusive_results"} flag parsed from the recipe JSON.
 *
 * <p>When set, the recipe's chance outputs are one weighted pick per processed item instead of
 * an independent roll per output (see {@link com.SaintAlfonzo.creativeengineering.fan.ExclusiveResults}).
 */
public interface ProcessingRecipeExt {

    boolean creativeengineering$isExclusiveResults();

    void creativeengineering$setExclusiveResults(boolean exclusive);
}
