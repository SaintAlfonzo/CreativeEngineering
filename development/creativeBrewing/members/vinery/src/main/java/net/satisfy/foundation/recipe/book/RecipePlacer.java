package net.satisfy.foundation.recipe.book;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/** Moves recipe ingredients from the player inventory into the station input slots. */
public final class RecipePlacer {
    private RecipePlacer() {
    }

    /** True if the inventory plus whatever already sits in the input slots covers one craft. */
    public static boolean canCraft(Inventory inventory, AbstractContainerMenu menu, int[] inputSlots, NonNullList<Ingredient> ingredients) {
        if (ingredients.size() > inputSlots.length) {
            return false;
        }
        List<ItemStack> pool = new ArrayList<>();
        for (ItemStack stack : inventory.items) {
            pool.add(stack.copy());
        }
        for (int index : inputSlots) {
            pool.add(menu.getSlot(index).getItem().copy());
        }
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack match = pool.stream().filter(stack -> !stack.isEmpty() && ingredient.test(stack)).findFirst().orElse(null);
            if (match == null) {
                return false;
            }
            match.shrink(1);
        }
        return true;
    }

    /**
     * Server side. Puts the current inputs back, then fills one craft, or as many as fit when {@code max}.
     * Every round is all or nothing, so the inputs never end up half filled.
     */
    public static void place(Inventory inventory, AbstractContainerMenu menu, int[] inputSlots, NonNullList<Ingredient> ingredients, boolean max) {
        if (ingredients.size() > inputSlots.length) {
            return;
        }
        for (int index : inputSlots) {
            Slot slot = menu.getSlot(index);
            if (slot.hasItem()) {
                inventory.placeItemBackInInventory(slot.getItem());
                slot.set(ItemStack.EMPTY);
            }
        }
        int rounds = max ? 64 : 1;
        for (int round = 0; round < rounds; round++) {
            int[] sources = new int[ingredients.size()];
            int[] taken = new int[inventory.items.size()];
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                sources[i] = -1;
                if (ingredient.isEmpty()) {
                    continue;
                }
                Slot target = menu.getSlot(inputSlots[i]);
                ItemStack placed = target.getItem();
                if (!placed.isEmpty() && placed.getCount() >= target.getMaxStackSize(placed)) {
                    return;
                }
                int source = findSource(inventory, ingredient, placed, taken);
                if (source < 0) {
                    return;
                }
                sources[i] = source;
                taken[source]++;
            }
            for (int i = 0; i < sources.length; i++) {
                if (sources[i] < 0) {
                    continue;
                }
                Slot target = menu.getSlot(inputSlots[i]);
                ItemStack moved = inventory.items.get(sources[i]).split(1);
                if (target.hasItem()) {
                    target.getItem().grow(1);
                    target.setChanged();
                } else {
                    target.set(moved);
                }
            }
        }
    }

    private static int findSource(Inventory inventory, Ingredient ingredient, ItemStack placed, int[] taken) {
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (stack.getCount() - taken[i] <= 0 || !ingredient.test(stack)) {
                continue;
            }
            if (placed.isEmpty() || ItemStack.isSameItemSameTags(placed, stack)) {
                return i;
            }
        }
        return -1;
    }
}
