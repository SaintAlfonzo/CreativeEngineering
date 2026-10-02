package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PhotographAgingRecipe extends ComponentTransferringRecipe {
   public PhotographAgingRecipe(ResourceLocation id, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
      super(id, sourceIngredient, ingredients, result);
   }

   @NotNull
   @Override
   public RecipeSerializer<?> getSerializer() {
      return Exposure.RecipeSerializers.PHOTOGRAPH_AGING.get();
   }

   /**
    * Upstream damages the brush (1.20+) instead of using it up. 1.19.2 has no brush: any damageable non-source
    * ingredient (a tool someone adds to the recipe) is damaged the same way.
    */
   @NotNull
   public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer input) {
      NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

      for (int i = 0; i < input.getContainerSize(); i++) {
         ItemStack stack = input.getItem(i);
         if (stack.isDamageableItem() && !this.getSourceIngredient().test(stack)) {
            stack = stack.copy();
            int damage = stack.getDamageValue() + 1;
            stack.setDamageValue(damage);
            if (damage >= stack.getMaxDamage()) {
               stack.shrink(1);
            }

            remainingItems.set(i, stack);
         }
      }

      return remainingItems;
   }
}
