package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

/**
 * The Stomping Basin takes only items that are an ingredient of some {@code hearthandharvest:stomping} recipe (owner
 * 2026-11-14, plan_bugfix_1114 3.10). The ingredient list is read from the level's recipe manager and kept until that manager
 * changes (a /reload builds a new one on the server) or the client's recipes are replaced (RecipesUpdatedEvent, see
 * {@link #clear}). Tag ingredients test the live tag, so tag reloads need nothing more.
 */
public final class StompingInputs {
    private static WeakReference<RecipeManager> source = new WeakReference<>(null);
    private static List<Ingredient> ingredients = List.of();

    private StompingInputs() {
    }

    public static boolean accepts(@Nullable Level level, ItemStack stack) {
        if (stack.isEmpty() || level == null) {
            return false;
        }
        for (Ingredient ingredient : ingredients(level.getRecipeManager())) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    private static synchronized List<Ingredient> ingredients(RecipeManager manager) {
        if (source.get() != manager) {
            List<Ingredient> out = new ArrayList<>();
            for (StompingRecipe recipe : manager.getAllRecipesFor(HHMachines.STOMPING_TYPE.get())) {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    if (!ingredient.isEmpty() && !out.contains(ingredient)) {
                        out.add(ingredient);
                    }
                }
            }
            ingredients = List.copyOf(out);
            source = new WeakReference<>(manager);
        }
        return ingredients;
    }

    /** The client keeps one recipe manager and refills it; forget the list when it does. */
    public static synchronized void clear() {
        source = new WeakReference<>(null);
        ingredients = List.of();
    }
}
