package net.satisfy.vinery.core.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.satisfy.vinery.core.recipe.input.FermentationBarrelRecipeInput;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.registry.RecipeTypeRegistry;
import org.jetbrains.annotations.NotNull;

/** {@code vinery:wine_fermentation}: juice type and units, 1-3 ingredients, an optional wine bottle. */
public class FermentationBarrelRecipe implements Recipe<FermentationBarrelRecipeInput> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final FermentationBarrelRecipeInput.JuiceData juiceData;
    private final boolean wineBottleRequired;

    public FermentationBarrelRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, FermentationBarrelRecipeInput.JuiceData data, ItemStack output, boolean wineBottleRequired) {
        this.id = id;
        this.inputs = inputs;
        this.juiceData = data;
        this.output = output;
        this.wineBottleRequired = wineBottleRequired;
    }

    public FermentationBarrelRecipeInput.JuiceData getJuiceData() {
        return this.juiceData;
    }

    public boolean isWineBottleRequired() {
        return wineBottleRequired;
    }

    @Override
    public boolean matches(@NotNull FermentationBarrelRecipeInput input, @NotNull Level level) {
        if (this.juiceData.amount() > 0) {
            if (input.data().amount() < this.juiceData.amount()) return false;
            if (!this.juiceData.type().equals(input.data().type())) return false;
        }

        if (this.wineBottleRequired) {
            ItemStack wineBottle = input.getItem(FermentationBarrelRecipeInput.WINE_BOTTLE_SLOT);
            if (wineBottle.isEmpty() || !wineBottle.is(ObjectRegistry.WINE_BOTTLE.get())) return false;
        }

        StackedContents recipeMatcher = new StackedContents();
        int matchingStacks = 0;

        for (ItemStack itemStack : input.getIngredientSlots()) {
            if (!itemStack.isEmpty()) {
                ++matchingStacks;
                recipeMatcher.accountStack(itemStack, 1);
            }
        }

        return matchingStacks == this.inputs.size() && recipeMatcher.canCraft(this, null);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull FermentationBarrelRecipeInput input) {
        return this.output.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return this.output.copy();
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeTypeRegistry.FERMENTATION_BARREL_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeTypeRegistry.FERMENTATION_BARREL_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<FermentationBarrelRecipe> {
        @Override
        public @NotNull FermentationBarrelRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) {
                inputs.add(Ingredient.fromJson(element));
            }
            if (inputs.size() > 3) {
                throw new JsonParseException("Too many ingredients for an aging barrel recipe (max 3)");
            }
            JsonObject juice = GsonHelper.getAsJsonObject(json, "juice");
            FermentationBarrelRecipeInput.JuiceData data = new FermentationBarrelRecipeInput.JuiceData(GsonHelper.getAsString(juice, "type"), GsonHelper.getAsInt(juice, "amount"));
            return new FermentationBarrelRecipe(id, inputs, data, RecipeJson.stack(GsonHelper.getAsJsonObject(json, "result")), RecipeJson.wineBottleRequired(json));
        }

        @Override
        public FermentationBarrelRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buf.readVarInt(), Ingredient.EMPTY);
            inputs.replaceAll(ignored -> Ingredient.fromNetwork(buf));
            ItemStack output = buf.readItem();
            FermentationBarrelRecipeInput.JuiceData data = FermentationBarrelRecipeInput.JuiceData.read(buf);
            return new FermentationBarrelRecipe(id, inputs, data, output, buf.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FermentationBarrelRecipe recipe) {
            buf.writeVarInt(recipe.inputs.size());
            recipe.inputs.forEach(ingredient -> ingredient.toNetwork(buf));
            buf.writeItem(recipe.output);
            recipe.juiceData.write(buf);
            buf.writeBoolean(recipe.wineBottleRequired);
        }
    }
}
