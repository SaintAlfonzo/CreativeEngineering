package io.github.mortuusars.exposure.world.item.crafting.recipe;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Shapeless recipe whose result takes all data (1.21: components; here the NBT) of the source ingredient.
 * 1.19.2: recipes carry their id; there is no crafting book category, so the constructor's first argument (upstream's
 * {@code CraftingBookCategory}) is the id.
 */
public class ComponentTransferringRecipe extends CustomRecipe {
   private final Ingredient sourceIngredient;
   private final NonNullList<Ingredient> ingredients;
   private final ItemStack result;

   public ComponentTransferringRecipe(ResourceLocation id, Ingredient sourceIngredient, NonNullList<Ingredient> ingredients, ItemStack result) {
      super(id);
      this.sourceIngredient = sourceIngredient;
      this.ingredients = ingredients;
      this.result = result;
   }

   @NotNull
   public RecipeSerializer<?> getSerializer() {
      return Exposure.RecipeSerializers.COMPONENT_TRANSFERRING.get();
   }

   @NotNull
   public Ingredient getSourceIngredient() {
      return this.sourceIngredient;
   }

   @NotNull
   public NonNullList<Ingredient> getIngredients() {
      return this.ingredients;
   }

   @NotNull
   public ItemStack getResultItem() {
      return this.getResult();
   }

   @NotNull
   public ItemStack getResult() {
      return this.result;
   }

   public boolean matches(@NotNull CraftingContainer input, @NotNull Level level) {
      if (!this.getSourceIngredient().isEmpty() && !this.ingredients.isEmpty()) {
         List<Ingredient> unmatchedIngredients = new ArrayList<>(this.ingredients);
         unmatchedIngredients.add(0, this.getSourceIngredient());
         int itemsInCraftingGrid = 0;

         for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
               itemsInCraftingGrid++;
            }

            if (itemsInCraftingGrid > this.ingredients.size() + 1) {
               return false;
            }

            if (!unmatchedIngredients.isEmpty()) {
               for (int j = 0; j < unmatchedIngredients.size(); j++) {
                  if (unmatchedIngredients.get(j).test(stack)) {
                     unmatchedIngredients.remove(j);
                     break;
                  }
               }
            }
         }

         return unmatchedIngredients.isEmpty() && itemsInCraftingGrid == this.ingredients.size() + 1;
      } else {
         return false;
      }
   }

   @NotNull
   public ItemStack assemble(@NotNull CraftingContainer input) {
      for (int index = 0; index < input.getContainerSize(); index++) {
         ItemStack itemStack = input.getItem(index);
         if (this.getSourceIngredient().test(itemStack)) {
            return this.transferComponents(itemStack, this.getResultItem().copy());
         }
      }

      return this.getResultItem();
   }

   @NotNull
   public ItemStack transferComponents(ItemStack transferIngredientStack, ItemStack recipeResultStack) {
      return DataComponentType.applyComponents(transferIngredientStack, recipeResultStack);
   }

   public boolean canCraftInDimensions(int width, int height) {
      return this.ingredients.size() <= width * height;
   }
}
