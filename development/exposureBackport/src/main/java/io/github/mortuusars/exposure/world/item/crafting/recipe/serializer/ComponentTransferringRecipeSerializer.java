package io.github.mortuusars.exposure.world.item.crafting.recipe.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.mortuusars.exposure.backport.util.BackportRecipes;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * 1.19.2 JSON/network serializer with upstream's recipe format:
 * {@code {"<sourceName>": ingredient, "ingredients": [ingredient, ...], "result": stack}}. A result may use the
 * 1.21 {@code "id"} key and an ingredient the 1.21 single-string {@code "items"} (see {@link BackportRecipes}).
 * Upstream's optional {@code "category"} is ignored (no crafting book categories in 1.19.2).
 */
public class ComponentTransferringRecipeSerializer<T extends ComponentTransferringRecipe> implements RecipeSerializer<T> {
   private final String recipeName;
   private final String sourceIngredientName;
   private final RecipeConstructor<T> constructor;

   public ComponentTransferringRecipeSerializer(String recipeName, String sourceName, RecipeConstructor<T> constructor) {
      this.recipeName = recipeName;
      this.sourceIngredientName = sourceName;
      this.constructor = constructor;
   }

   public ComponentTransferringRecipeSerializer(String serializedSourceIngredientName, RecipeConstructor<T> constructor) {
      this("component_transferring", serializedSourceIngredientName, constructor);
   }

   public ComponentTransferringRecipeSerializer(RecipeConstructor<T> constructor) {
      this("component_transferring", "source_ingredient", constructor);
   }

   public @NotNull T fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
      if (!json.has(this.sourceIngredientName)) {
         throw new JsonParseException("Missing '" + this.sourceIngredientName + "' in " + this.recipeName + " recipe '" + recipeId + "'");
      }
      Ingredient source = BackportRecipes.ingredientFromJson(json.get(this.sourceIngredientName));
      if (source.isEmpty()) {
         throw new JsonParseException("'" + this.sourceIngredientName + "' of " + this.recipeName + " recipe '" + recipeId + "' matches no items");
      }
      JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
      if (array.isEmpty()) {
         throw new JsonParseException("No ingredients for %s recipe".formatted(this.recipeName));
      }
      if (array.size() > 9) {
         throw new JsonParseException("Too many ingredients for %s recipe. Maximum is: %s".formatted(this.recipeName, 9));
      }
      NonNullList<Ingredient> ingredients = NonNullList.create();
      for (JsonElement element : array) {
         ingredients.add(BackportRecipes.ingredientFromJson(element));
      }
      ItemStack result = BackportRecipes.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
      return this.constructor.create(recipeId, source, ingredients, result);
   }

   public @NotNull T fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
      Ingredient source = Ingredient.fromNetwork(buffer);
      int count = buffer.readVarInt();
      NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
      for (int i = 0; i < count; i++) {
         ingredients.set(i, Ingredient.fromNetwork(buffer));
      }
      ItemStack result = buffer.readItem();
      return this.constructor.create(recipeId, source, ingredients, result);
   }

   public void toNetwork(FriendlyByteBuf buffer, T recipe) {
      recipe.getSourceIngredient().toNetwork(buffer);
      buffer.writeVarInt(recipe.getIngredients().size());
      for (Ingredient ingredient : recipe.getIngredients()) {
         ingredient.toNetwork(buffer);
      }
      buffer.writeItem(recipe.getResult());
   }

   @FunctionalInterface
   public interface RecipeConstructor<T extends ComponentTransferringRecipe> {
      T create(ResourceLocation id, Ingredient source, NonNullList<Ingredient> ingredients, ItemStack result);
   }
}
