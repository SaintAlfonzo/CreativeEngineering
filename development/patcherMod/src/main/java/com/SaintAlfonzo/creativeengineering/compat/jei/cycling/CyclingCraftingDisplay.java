package com.SaintAlfonzo.creativeengineering.compat.jei.cycling;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * A JEI-only crafting page for a special recipe JEI can't draw: a 3x3 grid and a result, each a list of
 * variants that cycle together ({@link CyclingCraftingExtension} focus-links every slot). Variant {@code i}
 * is the real recipe's result for the grid made of entry {@code i} of each slot, as {@link CraftingProbe}
 * computed it. Never registered with the game's recipe manager, so it never crafts anything.
 */
@ParametersAreNonnullByDefault
public final class CyclingCraftingDisplay implements CraftingRecipe {

    private final ResourceLocation id;
    private final List<List<ItemStack>> slots;
    private final List<ItemStack> outputs;
    private final boolean shapeless;

    CyclingCraftingDisplay(ResourceLocation id, List<List<ItemStack>> slots, List<ItemStack> outputs, boolean shapeless) {
        this.id = id;
        this.slots = slots;
        this.outputs = outputs;
        this.shapeless = shapeless;
    }

    /** One page from aligned variants: {@code grids.get(i)} (9 stacks, EMPTY for a blank cell) crafts {@code results.get(i)}. */
    static CyclingCraftingDisplay of(ResourceLocation id, List<List<ItemStack>> grids, List<ItemStack> results, boolean shapeless) {
        List<List<ItemStack>> slots = new ArrayList<>(9);
        for (int s = 0; s < 9; s++) {
            List<ItemStack> slot = new ArrayList<>(grids.size());
            for (List<ItemStack> grid : grids) {
                slot.add(grid.get(s));
            }
            slots.add(slot);
        }
        return new CyclingCraftingDisplay(id, slots, List.copyOf(results), shapeless);
    }

    /** Per grid cell, the stack each variant puts there (EMPTY when the cell is blank in that variant). */
    public List<List<ItemStack>> slots() {
        return slots;
    }

    public List<ItemStack> outputs() {
        return outputs;
    }

    public boolean shapeless() {
        return shapeless;
    }

    public int variants() {
        return outputs.size();
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem() {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (List<ItemStack> slot : slots) {
            List<ItemStack> filled = slot.stream().filter(s -> !s.isEmpty()).toList();
            if (!filled.isEmpty()) {
                list.add(Ingredient.of(filled.stream()));
            }
        }
        return list;
    }

    /** Keeps JEI's own crafting extension (registered for non-special recipes only) off this class. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }
}
