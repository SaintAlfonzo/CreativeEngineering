package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * hearthandharvest:stomping: up to four slot ingredients (matched like 1.4.1, ingredient by ingredient over the
 * basin's slots), an optional fluid and an optional item output.
 */
public class StompingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final FluidStack resultFluid;
    private final ItemStack resultItem;

    public StompingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, FluidStack resultFluid, ItemStack resultItem) {
        this.id = id;
        this.ingredients = ingredients;
        this.resultFluid = resultFluid;
        this.resultItem = resultItem;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return findAssignment(container) != null;
    }

    /** One slot per ingredient (a slot may serve several while it has items), or null if the items do not fit. */
    @Nullable
    public int[] findAssignment(Container container) {
        if (ingredients.isEmpty()) return null;
        int slots = Math.min(container.getContainerSize(), 4);
        ItemStack[] stacks = new ItemStack[slots];
        int[] remaining = new int[slots];
        for (int slot = 0; slot < slots; slot++) {
            stacks[slot] = container.getItem(slot);
            remaining[slot] = stacks[slot].getCount();
        }
        int[] assignment = new int[ingredients.size()];
        return assign(0, stacks, remaining, assignment) ? assignment : null;
    }

    private boolean assign(int index, ItemStack[] stacks, int[] remaining, int[] assignment) {
        if (index >= ingredients.size()) return true;
        Ingredient ingredient = ingredients.get(index);
        for (int slot = 0; slot < stacks.length; slot++) {
            if (remaining[slot] > 0 && ingredient.test(stacks[slot])) {
                remaining[slot]--;
                assignment[index] = slot;
                if (assign(index + 1, stacks, remaining, assignment)) return true;
                remaining[slot]++;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(Container container) {
        return resultItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return resultItem;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HHMachines.STOMPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return HHMachines.STOMPING_TYPE.get();
    }

    public FluidStack resultFluid() {
        return resultFluid;
    }

    public static class Serializer implements RecipeSerializer<StompingRecipe> {
        @Override
        public StompingRecipe fromJson(ResourceLocation id, JsonObject json) {
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) ingredients.add(Ingredient.fromJson(element));
            FluidStack fluid = FluidStack.EMPTY;
            if (json.has("result_fluid")) {
                JsonObject object = GsonHelper.getAsJsonObject(json, "result_fluid");
                ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(object, "id"));
                Fluid value = ForgeRegistries.FLUIDS.getValue(fluidId);
                if (value == null) throw new IllegalStateException("Unknown fluid " + fluidId);
                fluid = new FluidStack(value, GsonHelper.getAsInt(object, "amount", 1000));
            }
            ItemStack item = json.has("result_item") ? ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result_item")) : ItemStack.EMPTY;
            return new StompingRecipe(id, ingredients, fluid, item);
        }

        @Override
        public StompingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            FluidStack fluid = FluidStack.readFromPacket(buffer);
            ItemStack item = buffer.readItem();
            return new StompingRecipe(id, ingredients, fluid, item);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, StompingRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) ingredient.toNetwork(buffer);
            recipe.resultFluid.writeToPacket(buffer);
            buffer.writeItem(recipe.resultItem);
        }
    }
}
