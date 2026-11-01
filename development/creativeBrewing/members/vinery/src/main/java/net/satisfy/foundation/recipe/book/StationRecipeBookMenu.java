package net.satisfy.foundation.recipe.book;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Implement on a station menu to get the recipe book.
 * Ingredient {@code i} of a recipe goes into menu slot {@code recipeBookInputSlots(type)[i]}.
 * <p>
 * One recipe type: override {@link #recipeBookType()} and {@link #recipeBookInputSlots()}.
 * Several types (one tab each): override {@link #recipeBookTypes()} and {@link #recipeBookInputSlots(RecipeType)}.
 * <p>
 * Port: 1.21's {@code RecipeHolder} is the 1.19.2 {@link Recipe} itself (it carries its id). Upstream's unlock system
 * ({@code recipeBookRequiresUnlock}) has no user in Vinery and is left out.
 */
public interface StationRecipeBookMenu {

    /** Recipes of this type are listed in the book. */
    default RecipeType<?> recipeBookType() {
        throw new UnsupportedOperationException("Override recipeBookType() or recipeBookTypes()");
    }

    /** Menu slot indices (not container indices) for the ingredients, in ingredient order. */
    default int[] recipeBookInputSlots() {
        throw new UnsupportedOperationException("Override recipeBookInputSlots() or recipeBookInputSlots(RecipeType)");
    }

    /** All listed types, the book shows a tab per type when there is more than one. */
    default List<RecipeType<?>> recipeBookTypes() {
        return List.of(recipeBookType());
    }

    default int[] recipeBookInputSlots(RecipeType<?> type) {
        return recipeBookInputSlots();
    }

    /** Tooltip of the tab, null for none. */
    default @Nullable Component recipeBookTabName(RecipeType<?> type) {
        return null;
    }

    /**
     * Requirements outside the input slots (fluid, fuel...). When false the recipe shows as not craftable,
     * the ingredients can still be placed. Called on the client.
     */
    default boolean recipeBookExtrasMet(Recipe<?> recipe) {
        return true;
    }

    /** Extra tooltip lines below the ingredients, e.g. needed fluid. Called on the client. */
    default void appendRecipeBookTooltip(Recipe<?> recipe, List<Component> tooltip) {
    }

    /** Extra ghosts for a clicked recipe, menu slot index -> ingredient (juice, bottle...). Only shown, never placed. */
    default Map<Integer, Ingredient> recipeBookExtraGhosts(Recipe<?> recipe) {
        return Map.of();
    }

    /**
     * Items outside the recipe ingredients that the book places too (bottle, bowl...), menu slot index -> ingredient.
     * They count for "craftable" like normal ingredients. Called on both sides.
     */
    default Map<Integer, Ingredient> recipeBookExtraInputs(Recipe<?> recipe) {
        return Map.of();
    }

    /** Input slots of the recipe followed by the slots of {@link #recipeBookExtraInputs}. */
    default int[] recipeBookPlacementSlots(Recipe<?> recipe) {
        int[] inputs = recipeBookInputSlots(recipe.getType());
        Map<Integer, Ingredient> extras = recipeBookExtraInputs(recipe);
        if (extras.isEmpty()) {
            return inputs;
        }
        int[] slots = Arrays.copyOf(inputs, recipe.getIngredients().size() + extras.size());
        int i = recipe.getIngredients().size();
        for (int slot : extras.keySet()) {
            slots[i++] = slot;
        }
        return slots;
    }

    /** Ingredients of the recipe followed by {@link #recipeBookExtraInputs}, matching {@link #recipeBookPlacementSlots}. */
    default NonNullList<Ingredient> recipeBookPlacementIngredients(Recipe<?> recipe) {
        Map<Integer, Ingredient> extras = recipeBookExtraInputs(recipe);
        if (extras.isEmpty()) {
            return recipe.getIngredients();
        }
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(recipe.getIngredients());
        ingredients.addAll(extras.values());
        return ingredients;
    }

    /** Menu slot index where the result of this type ends up, the ghost shows the result there. -1 for none. */
    default int recipeBookResultSlot(RecipeType<?> type) {
        return -1;
    }
}
