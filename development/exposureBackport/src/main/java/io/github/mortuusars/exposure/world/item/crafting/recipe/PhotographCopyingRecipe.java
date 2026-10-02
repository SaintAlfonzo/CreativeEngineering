package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class PhotographCopyingRecipe extends ComponentTransferringRecipe {
   public PhotographCopyingRecipe(ResourceLocation id, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
      super(id, sourceIngredient, ingredients, result);
   }

   @NotNull
   @Override
   public RecipeSerializer<?> getSerializer() {
      return Exposure.RecipeSerializers.PHOTOGRAPH_COPYING.get();
   }

   @NotNull
   @Override
   public ItemStack transferComponents(ItemStack stack, ItemStack recipeResultStack) {
      int generation = (Integer)Exposure.DataComponents.PHOTOGRAPH_GENERATION.getOrDefault(stack, 0);
      if (generation < 2) {
         ItemStack result = super.transferComponents(stack, recipeResultStack);
         Exposure.DataComponents.PHOTOGRAPH_GENERATION.set(result, generation + 1);
         return result;
      } else {
         return ItemStack.EMPTY;
      }
   }

   @NotNull
   public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer input) {
      NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

      for (int i = 0; i < remainingItems.size(); i++) {
         ItemStack stack = input.getItem(i);
         if (stack.getItem() instanceof PhotographItem) {
            ItemStack remainingPhotographStack = stack.copy();
            remainingPhotographStack.setCount(1);
            remainingItems.set(i, remainingPhotographStack);
         }
      }

      return remainingItems;
   }
}
