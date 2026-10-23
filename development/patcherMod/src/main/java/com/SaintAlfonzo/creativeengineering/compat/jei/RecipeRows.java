package com.SaintAlfonzo.creativeengineering.compat.jei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.SaintAlfonzo.creativeengineering.compat.jei.RowPagesCategory.Row;
import com.SaintAlfonzo.creativeengineering.compat.jei.RowPagesCategory.Slot;

import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Turns crafting, Create (Spout, Item Drain) and IE Bottling Machine recipes into {@link Row}s for
 * {@link RowPagesCategory}, and hides the moved recipes from their old JEI pages. Create and IE classes are only
 * touched from the nested holders, so either mod can be missing.
 */
final class RecipeRows {

    private RecipeRows() {}

    /**
     * A crafting recipe as one row: equal ingredients merged into one slot with a count (4 potions, not 4 slots); the
     * outputs are the result and what the inputs leave behind in the grid (the bucket, the glass bottles). Null if it
     * doesn't fit a row.
     */
    static Row crafting(Recipe<?> recipe, ItemStack machine) {
        if (!(recipe instanceof CraftingRecipe crafting) || crafting.getResultItem().isEmpty()) {
            return null;
        }
        return craft(machine, merge(crafting.getIngredients()), crafting.getResultItem());
    }

    /** A hand craft as one row (see {@link #crafting}); {@code inputs} are already merged slot contents. */
    static Row craft(ItemStack machine, List<List<ItemStack>> inputs, ItemStack result) {
        List<List<ItemStack>> outputs = new ArrayList<>();
        outputs.add(List.of(result.copy()));
        List<List<ItemStack>> leftovers = new ArrayList<>();
        for (List<ItemStack> input : inputs) {
            ItemStack first = input.get(0);
            if (first.hasCraftingRemainingItem()) {
                ItemStack left = first.getCraftingRemainingItem();
                leftovers.add(List.of(withCount(left, left.getCount() * first.getCount())));
            }
        }
        outputs.addAll(fold(leftovers));
        return row(machine, toSlots(inputs), toSlots(outputs));
    }

    /** A Create processing recipe (filling, emptying, ...) as one row, or null. */
    static Row create(Recipe<?> recipe, ItemStack machine) {
        return ModList.get().isLoaded("create") ? CreateRows.row(recipe, machine) : null;
    }

    /** An IE Bottling Machine recipe as one row, or null. */
    static Row bottling(Recipe<?> recipe, ItemStack machine) {
        return ModList.get().isLoaded("immersiveengineering") ? IERows.row(recipe, machine) : null;
    }

    static ItemStack stack(ResourceLocation id) {
        return ForgeRegistries.ITEMS.containsKey(id) ? new ItemStack(ForgeRegistries.ITEMS.getValue(id)) : ItemStack.EMPTY;
    }

    private static Row row(ItemStack machine, List<Slot> inputs, List<Slot> outputs) {
        Row row = new Row(machine, inputs, outputs);
        return row.fits() ? row : null;
    }

    private static List<Slot> toSlots(List<List<ItemStack>> stacks) {
        List<Slot> slots = new ArrayList<>(stacks.size());
        for (List<ItemStack> s : stacks) {
            slots.add(Slot.items(s));
        }
        return slots;
    }

    /** Ingredients as slot contents, equal ones folded into one with the summed count; empty ones dropped. */
    static List<List<ItemStack>> merge(List<Ingredient> ingredients) {
        List<List<ItemStack>> slots = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            slots.add(Arrays.asList(ingredient.getItems()));
        }
        return fold(slots);
    }

    /** Slot contents with equal ones folded into one with the summed count; empty ones dropped. */
    private static List<List<ItemStack>> fold(List<List<ItemStack>> slots) {
        List<List<ItemStack>> merged = new ArrayList<>();
        for (List<ItemStack> items : slots) {
            if (items.isEmpty()) {
                continue;
            }
            boolean folded = false;
            for (int m = 0; m < merged.size() && !folded; m++) {
                List<ItemStack> into = merged.get(m);
                if (same(into, items)) {
                    int add = items.get(0).getCount();
                    List<ItemStack> grown = new ArrayList<>(into.size());
                    for (ItemStack s : into) {
                        grown.add(withCount(s, s.getCount() + add));
                    }
                    merged.set(m, grown);
                    folded = true;
                }
            }
            if (!folded) {
                List<ItemStack> copies = new ArrayList<>(items.size());
                for (ItemStack s : items) {
                    copies.add(s.copy());
                }
                merged.add(copies);
            }
        }
        return merged;
    }

    static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static boolean same(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int n = 0; n < a.size(); n++) {
            if (!ItemStack.isSameItemSameTags(a.get(n), b.get(n))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Hides the recipes with these ids from each JEI page (key: the page's recipe type id). Pages that aren't
     * registered (mod missing) are skipped. Returns how many were hidden.
     */
    static int hide(IRecipeManager recipes, Map<ResourceLocation, Set<ResourceLocation>> idsByPage) {
        int hidden = 0;
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> page : idsByPage.entrySet()) {
            if (page.getValue().isEmpty()) {
                continue;
            }
            Optional<RecipeType<?>> type = recipes.getRecipeType(page.getKey());
            if (type.isPresent()) {
                hidden += hideById(recipes, type.get(), page.getValue());
            }
        }
        return hidden;
    }

    private static <T> int hideById(IRecipeManager recipes, RecipeType<T> type, Set<ResourceLocation> ids) {
        List<T> hide = recipes.createRecipeLookup(type).includeHidden().get()
                .filter(r -> r instanceof Recipe<?> recipe && ids.contains(recipe.getId()))
                .toList();
        if (!hide.isEmpty()) {
            recipes.hideRecipes(type, hide);
        }
        return hide.size();
    }

    private static final class CreateRows {

        static Row row(Recipe<?> recipe, ItemStack machine) {
            if (!(recipe instanceof com.simibubi.create.content.processing.recipe.ProcessingRecipe<?> processing)) {
                return null;
            }
            List<Slot> inputs = toSlots(merge(processing.getIngredients()));
            for (com.simibubi.create.foundation.fluid.FluidIngredient fluid : processing.getFluidIngredients()) {
                inputs.add(Slot.fluids(fluid.getMatchingFluidStacks()));
            }
            List<Slot> outputs = new ArrayList<>();
            for (com.simibubi.create.content.processing.recipe.ProcessingOutput out : processing.getRollableResults()) {
                outputs.add(Slot.items(List.of(out.getStack().copy())));
            }
            for (net.minecraftforge.fluids.FluidStack fluid : processing.getFluidResults()) {
                outputs.add(Slot.fluids(List.of(fluid.copy())));
            }
            return RecipeRows.row(machine, inputs, outputs);
        }
    }

    private static final class IERows {

        static Row row(Recipe<?> recipe, ItemStack machine) {
            if (!(recipe instanceof blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe bottling)) {
                return null;
            }
            List<Slot> inputs = new ArrayList<>();
            for (blusunrize.immersiveengineering.api.crafting.IngredientWithSize in : bottling.inputs) {
                List<ItemStack> items = new ArrayList<>();
                for (ItemStack s : in.getMatchingStacks()) {
                    items.add(withCount(s, in.getCount()));
                }
                inputs.add(Slot.items(items));
            }
            if (bottling.fluidInput != null) {
                inputs.add(Slot.fluids(bottling.fluidInput.getMatchingFluidStacks()));
            }
            List<Slot> outputs = new ArrayList<>();
            for (ItemStack out : bottling.output.get()) {
                outputs.add(Slot.items(List.of(out.copy())));
            }
            return RecipeRows.row(machine, inputs, outputs);
        }
    }
}
