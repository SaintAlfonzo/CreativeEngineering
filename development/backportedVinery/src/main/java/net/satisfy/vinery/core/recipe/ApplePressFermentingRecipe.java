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
import net.satisfy.vinery.core.recipe.input.ApplePressFermentingRecipeInput;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

/** {@code vinery:apple_fermenting}: the press's second stage (mash + bottle -> apple juice). */
public class ApplePressFermentingRecipe implements Recipe<ApplePressFermentingRecipeInput> {
    private final ResourceLocation id;
    public final Ingredient input;
    private final ItemStack output;
    private final boolean requiresBottle;

    public ApplePressFermentingRecipe(ResourceLocation id, Ingredient input, ItemStack output, boolean requiresBottle) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.requiresBottle = requiresBottle;
    }

    public boolean requiresBottle() {
        return requiresBottle;
    }

    @Override
    public boolean matches(ApplePressFermentingRecipeInput inventory, @NotNull Level level) {
        return input.test(inventory.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull ApplePressFermentingRecipeInput container) {
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

    public boolean isRequiresBottle() {
        return requiresBottle;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeTypeRegistry.APPLE_PRESS_FERMENTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypeRegistry.APPLE_PRESS_FERMENTING_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ApplePressFermentingRecipe> {
        @Override
        public @NotNull ApplePressFermentingRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            return new ApplePressFermentingRecipe(id, Ingredient.fromJson(json.get("input")), RecipeJson.stack(GsonHelper.getAsJsonObject(json, "output")), RecipeJson.wineBottleRequired(json));
        }

        @Override
        public ApplePressFermentingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            return new ApplePressFermentingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), buf.readBoolean());
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, ApplePressFermentingRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.output);
            buf.writeBoolean(recipe.requiresBottle);
        }
    }
}
