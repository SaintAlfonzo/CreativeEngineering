package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class FilmDevelopingRecipe extends ComponentTransferringRecipe {
   public FilmDevelopingRecipe(ResourceLocation id, Ingredient filmIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
      super(id, filmIngredient, ingredients, result);
   }

   @NotNull
   @Override
   public RecipeSerializer<?> getSerializer() {
      return Exposure.RecipeSerializers.FILM_DEVELOPING.get();
   }

   @NotNull
   public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer input) {
      NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);

      for (int i = 0; i < input.getContainerSize(); i++) {
         ItemStack item = input.getItem(i);
         if (item.getItem() instanceof PotionItem && remainingItems.get(i).isEmpty()) {
            remainingItems.set(i, new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return remainingItems;
   }
}
