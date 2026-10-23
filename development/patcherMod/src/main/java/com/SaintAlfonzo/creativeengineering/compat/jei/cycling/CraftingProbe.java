package com.SaintAlfonzo.creativeengineering.compat.jei.cycling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

/**
 * Runs a real crafting recipe on a made-up 3x3 grid: {@code matches}, then {@code assemble}. The pages
 * only show grids the mod's own code accepts, with the exact result (NBT included) it would give.
 * Any exception from the mod's code counts as "no match".
 */
final class CraftingProbe {

    /** A menu that never reacts: CraftingContainer calls slotsChanged on every setItem. */
    private static final AbstractContainerMenu MENU = new AbstractContainerMenu(null, -1) {
        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }

        @Override
        public void slotsChanged(Container container) {
        }
    };

    private final CraftingRecipe recipe;
    private final Level level;
    private final CraftingContainer grid = new CraftingContainer(MENU, 3, 3);
    final List<List<ItemStack>> grids = new ArrayList<>();
    final List<ItemStack> results = new ArrayList<>();

    CraftingProbe(CraftingRecipe recipe, Level level) {
        this.recipe = recipe;
        this.level = level;
    }

    /** The result for this grid (9 cells, EMPTY or null for blank), or EMPTY when the recipe refuses it. */
    ItemStack craft(List<ItemStack> cells) {
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack s = i < cells.size() ? cells.get(i) : null;
                grid.setItem(i, s == null ? ItemStack.EMPTY : s.copy());
            }
            if (!recipe.matches(grid, level)) {
                return ItemStack.EMPTY;
            }
            ItemStack out = recipe.assemble(grid);
            return out == null ? ItemStack.EMPTY : out;
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    /** Crafts the grid and keeps it as a variant when the recipe accepts it; returns the result (EMPTY if not). */
    ItemStack add(List<ItemStack> cells) {
        ItemStack out = craft(cells);
        if (!out.isEmpty()) {
            List<ItemStack> copy = new ArrayList<>(9);
            for (int i = 0; i < 9; i++) {
                ItemStack s = i < cells.size() ? cells.get(i) : null;
                copy.add(s == null ? ItemStack.EMPTY : s.copy());
            }
            grids.add(copy);
            results.add(out.copy());
        }
        return out;
    }

    /** Grid cells in reading order, the rest blank. */
    static List<ItemStack> cells(ItemStack... stacks) {
        return Arrays.asList(Arrays.copyOf(stacks, 9));
    }

    /** {@code centre} in the middle cell, {@code ring} in the other eight. */
    static List<ItemStack> ring(ItemStack centre, ItemStack ring) {
        ItemStack[] g = new ItemStack[9];
        Arrays.fill(g, ring);
        g[4] = centre;
        return Arrays.asList(g);
    }

    /** Cells set by index: {@code at(4, a, 1, b)} puts a in the middle and b above it. */
    static List<ItemStack> at(Object... indexAndStack) {
        ItemStack[] g = new ItemStack[9];
        for (int i = 0; i + 1 < indexAndStack.length; i += 2) {
            g[(Integer) indexAndStack[i]] = (ItemStack) indexAndStack[i + 1];
        }
        return Arrays.asList(g);
    }

    boolean isEmpty() {
        return results.isEmpty();
    }

    @Nullable
    CyclingCraftingDisplay page(String path, boolean shapeless) {
        if (results.isEmpty()) {
            return null;
        }
        return CyclingCraftingDisplay.of(CyclingCraftingPages.pageId(recipe, path), grids, results, shapeless);
    }
}
