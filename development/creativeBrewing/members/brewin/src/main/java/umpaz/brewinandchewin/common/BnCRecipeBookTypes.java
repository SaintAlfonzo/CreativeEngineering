package umpaz.brewinandchewin.common;

import net.minecraft.world.inventory.RecipeBookType;

/**
 * Port: Forge's extensible enum. Created from the mod constructor on both sides, so the client and the dedicated server
 * agree on RecipeBookType.values() (a mismatch kicks players on join, see the RecipeBookType desync note).
 */
public class BnCRecipeBookTypes {
   public static final RecipeBookType FERMENTING = RecipeBookType.create("BREWINANDCHEWIN_FERMENTING");

   public static void init() {
   }
}
