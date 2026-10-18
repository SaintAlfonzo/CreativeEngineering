package umpaz.brewinandchewin.integration.jei;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An unripe wheel ripening into the ripe one (JEI display only; upstream wraps this in a dummy Recipe). */
public record CheeseAgingRecipe(Item before, Item after) {
   public ItemStack input() {
      return new ItemStack(this.before);
   }

   public ItemStack output() {
      return new ItemStack(this.after);
   }
}
