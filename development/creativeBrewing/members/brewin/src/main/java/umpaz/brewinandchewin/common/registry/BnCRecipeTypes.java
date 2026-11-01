package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;

public class BnCRecipeTypes {
   public static final RecipeType<KegFermentingRecipe> FERMENTING = registerRecipeType("fermenting");
   public static final RecipeType<KegPouringRecipe> KEG_POURING = registerRecipeType("keg_pouring");

   public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
      return new RecipeType<T>() {
         @Override
         public String toString() {
            return "brewinandchewin:" + identifier;
         }
      };
   }

   public static void registerAll(RegisterHelper<RecipeType<?>> helper) {
      helper.register(BrewinAndChewin.asResource("fermenting"), FERMENTING);
      helper.register(BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
   }
}
