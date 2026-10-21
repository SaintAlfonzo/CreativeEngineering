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
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * {@code vinery:apple_fermenting}: the press's second stage (mash + bottle -> apple juice). Port: the bottle is an
 * ingredient per recipe ({@code "bottle"}; upstream's {@code "wine_bottle": {"required": true}} means a wine bottle), so
 * honey can take a glass bottle (alcohol.md B7.2).
 */
public class ApplePressFermentingRecipe implements Recipe<ApplePressFermentingRecipeInput> {
    private final ResourceLocation id;
    public final Ingredient input;
    private final ItemStack output;
    private final Ingredient bottle;

    public ApplePressFermentingRecipe(ResourceLocation id, Ingredient input, ItemStack output, Ingredient bottle) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.bottle = bottle;
    }

    public boolean requiresBottle() {
        return !bottle.isEmpty();
    }

    /** The bottle the right-hand slot needs; {@link Ingredient#EMPTY} when none. */
    public Ingredient getBottle() {
        return bottle;
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
        return requiresBottle();
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
            Ingredient bottle = json.has("bottle") ? Ingredient.fromJson(json.get("bottle"))
                    : RecipeJson.wineBottleRequired(json) ? Ingredient.of(ObjectRegistry.WINE_BOTTLE.get()) : Ingredient.EMPTY;
            return new ApplePressFermentingRecipe(id, Ingredient.fromJson(json.get("input")), RecipeJson.stack(GsonHelper.getAsJsonObject(json, "output")), bottle);
        }

        @Override
        public ApplePressFermentingRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            return new ApplePressFermentingRecipe(id, Ingredient.fromNetwork(buf), buf.readItem(), Ingredient.fromNetwork(buf));
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, ApplePressFermentingRecipe recipe) {
            recipe.input.toNetwork(buf);
            buf.writeItem(recipe.output);
            recipe.bottle.toNetwork(buf);
        }
    }
}
