package umpaz.brewinandchewin.integration.jei;

import mezz.jei.api.recipe.RecipeType;

public class BnCJEIRecipeTypes {
   public static final RecipeType<KegFermentingPouringRecipe> FERMENTING = RecipeType.create("brewinandchewin", "fermenting", KegFermentingPouringRecipe.class);
   public static final RecipeType<CheeseAgingRecipe> AGING = RecipeType.create("brewinandchewin", "aging", CheeseAgingRecipe.class);
}
