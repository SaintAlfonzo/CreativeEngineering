package umpaz.brewinandchewin.client.recipebook;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import umpaz.brewinandchewin.common.registry.BnCItems;

// Port: Forge 1.19.2's RecipeBookCategories.create (client-only extensible enum); icons as upstream.
public class BnCRecipeBookCategories {
   public static final RecipeBookCategories FERMENTING_SEARCH = RecipeBookCategories.create("BREWINANDCHEWIN_FERMENTING_SEARCH", new ItemStack(Items.COMPASS));
   public static final RecipeBookCategories FERMENTING_DRINKS = RecipeBookCategories.create("BREWINANDCHEWIN_FERMENTING_DRINKS", new ItemStack(BnCItems.BEER));
   public static final RecipeBookCategories FERMENTING_MEALS = RecipeBookCategories.create(
      "BREWINANDCHEWIN_FERMENTING_MEALS", new ItemStack(BnCItems.UNRIPE_FLAXEN_CHEESE_WHEEL)
   );
   public static final RecipeBookCategories FERMENTING_MISC = RecipeBookCategories.create("BREWINANDCHEWIN_FERMENTING_MISC", new ItemStack(BnCItems.KIMCHI));
}
