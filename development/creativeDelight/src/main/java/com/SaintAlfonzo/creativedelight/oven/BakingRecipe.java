package com.SaintAlfonzo.creativedelight.oven;

import com.SaintAlfonzo.creativedelight.CDConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.food.CanServings;
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
 * <p>
 * An ingredient may carry {@code "count": n} (default 1): with {@code [cooking] ovenIngredientCounts} on, its slot must
 * hold at least n of it, and a bake takes n from that slot. This lets a 9-item crafting recipe (3 wheat, 3 apples,
 * 2 sugar, a crust) fit the 6 slots unchanged. With it off (the default) every count counts as 1.
 */
public class BakingRecipe implements Recipe<RecipeWrapper> {

    public static final int INPUT_SLOTS = 6;

    private final ResourceLocation id;
    private final String group;
    private final NonNullList<Ingredient> inputItems;
    // Per ingredient, parallel to inputItems: how many one bake takes from the slot that ingredient matched.
    private final int[] inputCounts;
    // null = the recipe names no tool. Not Ingredient.isEmpty(): a tag tool whose tag is empty must still block.
    private final Ingredient tool;
    private final ItemStack output;
    private final ItemStack container;
    private final float experience;
    private final int cookTime;

    public BakingRecipe(ResourceLocation id, String group, NonNullList<Ingredient> inputItems, int[] inputCounts,
                        @Nullable Ingredient tool, ItemStack output, ItemStack container, float experience, int cookTime) {
        this.id = id;
        this.group = group;
        this.inputItems = inputItems;
        this.inputCounts = inputCounts;
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
        return slotCounts(inv) != null && (tool == null || tool.test(inv.getItem(OvenBlockEntity.TOOL_SLOT)));
    }

    /**
     * How many items one bake takes from each ingredient slot (index = slot), or null when the ingredients don't
     * match: every non-empty slot must match a different ingredient and hold at least that ingredient's count.
     * A partly eaten can matches nothing, so a pie filling bakes only from a full can.
     */
    @Nullable
    public int[] slotCounts(RecipeWrapper inv) {
        List<ItemStack> inputs = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < INPUT_SLOTS; ++slot) {
            ItemStack stack = inv.getItem(slot);
            if (!stack.isEmpty()) {
                inputs.add(stack);
                slots.add(slot);
            }
        }
        if (inputs.size() != inputItems.size()) {
            return null;
        }
        List<Predicate<ItemStack>> tests = new ArrayList<>();
        for (int i = 0; i < inputItems.size(); ++i) {
            Ingredient ingredient = inputItems.get(i);
            int count = getIngredientCount(i);
            tests.add(stack -> ingredient.test(stack) && stack.getCount() >= count && !CanServings.isPartial(stack));
        }
        int[] match = RecipeMatcher.findMatches(inputs, tests);
        if (match == null) {
            return null;
        }
        int[] counts = new int[INPUT_SLOTS];
        for (int k = 0; k < match.length; ++k) {
            counts[slots.get(k)] = getIngredientCount(match[k]);
        }
        return counts;
    }

    /** How many of ingredient {@code index} (as in {@link #getIngredients}) one bake takes: 1 unless counts are on. */
    public int getIngredientCount(int index) {
        return CDConfig.COOKING.ovenIngredientCounts.get() ? inputCounts[index] : 1;
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
            List<Integer> countList = new ArrayList<>();
            NonNullList<Ingredient> inputs = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"), countList);
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
            int[] counts = countList.stream().mapToInt(Integer::intValue).toArray();
            return new BakingRecipe(recipeId, group, inputs, counts, tool, output, container, experience, cookTime);
        }

        // An entry's optional "count" sits beside its item/tag; Ingredient.fromJson ignores the extra key.
        private static NonNullList<Ingredient> readIngredients(JsonArray array, List<Integer> counts) {
            NonNullList<Ingredient> list = NonNullList.create();
            for (int i = 0; i < array.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(array.get(i));
                if (!ingredient.isEmpty()) {
                    list.add(ingredient);
                    int count = array.get(i).isJsonObject() ? GsonHelper.getAsInt(array.get(i).getAsJsonObject(), "count", 1) : 1;
                    if (count < 1) {
                        throw new JsonParseException("Baking ingredient count must be at least 1, got " + count);
                    }
                    counts.add(count);
                }
            }
            return list;
        }

        @Override
        public BakingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            int count = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(count, Ingredient.EMPTY);
            int[] counts = new int[count];
            for (int i = 0; i < count; ++i) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
                counts[i] = buffer.readVarInt();
            }
            Ingredient tool = buffer.readBoolean() ? Ingredient.fromNetwork(buffer) : null;
            ItemStack output = buffer.readItem();
            ItemStack container = buffer.readItem();
            float experience = buffer.readFloat();
            int cookTime = buffer.readVarInt();
            return new BakingRecipe(recipeId, group, inputs, counts, tool, output, container, experience, cookTime);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BakingRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeVarInt(recipe.inputItems.size());
            for (int i = 0; i < recipe.inputItems.size(); ++i) {
                recipe.inputItems.get(i).toNetwork(buffer);
                buffer.writeVarInt(recipe.inputCounts[i]);
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
