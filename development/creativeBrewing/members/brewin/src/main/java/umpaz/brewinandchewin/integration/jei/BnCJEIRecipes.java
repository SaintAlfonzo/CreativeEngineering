package umpaz.brewinandchewin.integration.jei;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;

public class BnCJEIRecipes {
   private final RecipeManager recipeManager;

   public BnCJEIRecipes() {
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         throw new NullPointerException("minecraft world must not be null.");
      }
      this.recipeManager = level.getRecipeManager();
   }

   public List<KegFermentingPouringRecipe> getKegRecipes() {
      List<KegPouringRecipe> pours = this.recipeManager.getAllRecipesFor(BnCRecipeTypes.KEG_POURING);
      List<KegFermentingPouringRecipe> kegRecipes = new ArrayList<>();

      for (KegFermentingRecipe fermentingRecipe : this.recipeManager.getAllRecipesFor(BnCRecipeTypes.FERMENTING)) {
         if (fermentingRecipe.getResult().left().isPresent()) {
            for (KegPouringRecipe pouringRecipe : pours) {
               if (pouringRecipe.getRawFluid().matches(fermentingRecipe.getResult().left().get())) {
                  kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe, pouringRecipe));
               }
            }
         } else {
            kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe, null));
         }
      }

      return kegRecipes;
   }

   public List<CheeseAgingRecipe> getCheeseRecipes() {
      return List.of(
         new CheeseAgingRecipe(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL.asItem(), BnCBlocks.FLAXEN_CHEESE_WHEEL.asItem()),
         new CheeseAgingRecipe(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL.asItem(), BnCBlocks.SCARLET_CHEESE_WHEEL.asItem())
      );
   }
}
