package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import umpaz.brewinandchewin.BrewinAndChewin;

/** Port-only: 1.20+ ItemStack helpers that 1.19.2 lacks. */
public final class BnCItemUtils {
   private BnCItemUtils() {
   }

   public static ItemStack copyWithCount(ItemStack stack, int count) {
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      }
      ItemStack copy = stack.copy();
      copy.setCount(count);
      return copy;
   }

   /** 1.20.5 ItemStack.isSameItem: same item, components ignored. */
   public static boolean isSameItem(ItemStack a, ItemStack b) {
      return a.is(b.getItem());
   }

   /**
    * What's left of a keg ingredient once used. As upstream (the crafting remainder), plus a glass bottle for potions:
    * Dread Nog takes a poison potion in place of the ominous bottle (alcohol.md 4.4), and vanilla potions have no
    * crafting remainder, so without this the bottle would vanish.
    */
   public static ItemStack getIngredientRemainder(ItemStack stack) {
      ItemStack remainder = BrewinAndChewin.getHelper().getCraftingRemainingItem(stack);
      if (remainder.isEmpty() && stack.getItem() instanceof PotionItem) {
         return new ItemStack(Items.GLASS_BOTTLE);
      }
      return remainder;
   }
}
