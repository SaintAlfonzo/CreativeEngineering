package com.SaintAlfonzo.creativeengineering.oven;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.wrapper.RecipeWrapper;

/**
 * {@code creativeengineering:baking}: Farmer's Delight's {@code farmersdelight:cooking} JSON (CookingPotRecipe,
 * MIT) plus an optional {@code tool}, an ingredient that must sit in the oven's tool slot and is never used up.
 * <pre>
 * { "type": "creativeengineering:baking", "ingredients": [ ...1-6... ], "tool": {...}?, "container": {...}?,
 *   "result": {...}, "experience": 0.0?, "cookingtime": 200? }
 * </pre>
 * {@code container} is the plating item (FD semantics: the result waits until one is supplied); it defaults to the
 * result's crafting remainder.
 */
public class BakingRecipe implements Recipe<RecipeWrapper> {

    public static final int INPUT_SLOTS = 6;

    private final ResourceLocation id;
    private final String group;
    private final NonNullList<Ingredient> inputItems;
    // null = the recipe names no tool. Not Ingredient.isEmpty(): a tag tool whose tag is empty must still block.
    private final Ingredient tool;
    private final ItemStack output;
    private final ItemStack container;
    private final float experience;
    private final int cookTime;

    public BakingRecipe(ResourceLocation id, String group, NonNullList<Ingredient> inputItems, @Nullable Ingredient tool,
                        ItemStack output, ItemStack container, float experience, int cookTime) {
        this.id = id;
        this.group = group;
        this.inputItems = inputItems;
        this.tool = tool;
        this.output = output;
        if (!container.isEmpty()) {
            this.container = container;
        } else if (!output.getCraftingRemainingItem().isEmpty()) {
            this.container = output.getCraftingRemainingItem();
        } else {
            this.container = ItemStack.EMPTY;
        }
        this.experience = experience;
        this.cookTime = cookTime;
    }

    @Override
    public boolean matches(RecipeWrapper inv, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < INPUT_SLOTS; ++slot) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        if (inputs.size() != inputItems.size() || RecipeMatcher.findMatches(inputs, inputItems) == null) {
            return false;
        }
        return tool == null || tool.test(inv.getItem(OvenBlockEntity.TOOL_SLOT));
    }

    public boolean hasTool() {
        return tool != null;
    }

    /** Empty when the recipe needs no tool. */
    public Ingredient getTool() {
        return tool != null ? tool : Ingredient.EMPTY;
    }

    public ItemStack getOutputContainer() {
        return container;
    }

    public float getExperience() {
        return experience;
    }

    public int getCookTime() {
        return cookTime;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    @Override
    public ItemStack assemble(RecipeWrapper inv) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= inputItems.size();
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(OvenRegistry.OVEN_ITEM.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return OvenRegistry.BAKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return OvenRegistry.BAKING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BakingRecipe> {

        @Override
        public BakingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            NonNullList<Ingredient> inputs = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (inputs.isEmpty()) {
                throw new JsonParseException("No ingredients for baking recipe");
            } else if (inputs.size() > INPUT_SLOTS) {
                throw new JsonParseException("Too many ingredients for baking recipe! The max is " + INPUT_SLOTS);
            }
            Ingredient tool = json.has("tool") ? Ingredient.fromJson(json.get("tool")) : null;
            ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            ItemStack container = GsonHelper.isValidNode(json, "container")
                    ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "container"), true)
                    : ItemStack.EMPTY;
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookTime = GsonHelper.getAsInt(json, "cookingtime", 200);
            return new BakingRecipe(recipeId, group, inputs, tool, output, container, experience, cookTime);
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray array) {
            NonNullList<Ingredient> list = NonNullList.create();
            for (int i = 0; i < array.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(array.get(i));
                if (!ingredient.isEmpty()) {
                    list.add(ingredient);
                }
            }
            return list;
        }

        @Override
        public BakingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int count = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(count, Ingredient.EMPTY);
            for (int i = 0; i < count; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            Ingredient tool = buffer.readBoolean() ? Ingredient.fromNetwork(buffer) : null;
            ItemStack output = buffer.readItem();
            ItemStack container = buffer.readItem();
            float experience = buffer.readFloat();
            int cookTime = buffer.readVarInt();
            return new BakingRecipe(recipeId, group, inputs, tool, output, container, experience, cookTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BakingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputItems.size());
            for (Ingredient ingredient : recipe.inputItems) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeBoolean(recipe.hasTool());
            if (recipe.hasTool()) {
                recipe.tool.toNetwork(buffer);
            }
            buffer.writeItem(recipe.output);
            buffer.writeItem(recipe.container);
            buffer.writeFloat(recipe.experience);
            buffer.writeVarInt(recipe.cookTime);
        }
    }
}
