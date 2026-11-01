package net.satisfy.vinery.core.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.recipe.input.ApplePressMashingRecipeInput;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

/** {@code vinery:apple_mashing}: the press's first stage (apple -> mash). */
public class ApplePressMashingRecipe implements Recipe<ApplePressMashingRecipeInput> {
    private final ResourceLocation id;
    public final Ingredient input;
    private final ItemStack output;

    public ApplePressMashingRecipe(ResourceLocation id, Ingredient input, ItemStack output) {
        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(ApplePressMashingRecipeInput inventory, @NotNull Level level) {
        return input.test(inventory.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ApplePressMashingRecipeInput container) {
        return this.output.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return this.output.copy();
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeTypeRegistry.APPLE_PRESS_MASHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypeRegistry.APPLE_PRESS_MASHING_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ApplePressMashingRecipe> {
        @Override
        public @NotNull ApplePressMashingRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            return new ApplePressMashingRecipe(id, Ingredient.fromJson(json.get("input")), RecipeJson.stack(GsonHelper.getAsJsonObject(json, "output")));
        }

        @Override
        public ApplePressMashingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            return new ApplePressMashingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem());
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, ApplePressMashingRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.output);
        }
    }
}
