package com.SaintAlfonzo.creativedelight.oven;

import javax.annotation.Nonnull;

import com.SaintAlfonzo.creativedelight.CreativeDelight;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * The oven's automation face (oven.md 3.5), the same on every side: the oven's bottom sits on its heat source and
 * its top usually carries a pot, so FD's "top = ingredients" split would leave nothing open. Inserting routes by
 * item: {@code #creativedelight:bakeware} to the tool slot, the plating item to the container slot, anything
 * else to the ingredient slots. Extracting takes from the output slot only.
 */
public class OvenItemHandler implements IItemHandler {

    public static final TagKey<Item> BAKEWARE = ItemTags.create(new ResourceLocation(CreativeDelight.MOD_ID, "bakeware"));

    private final OvenBlockEntity oven;

    public OvenItemHandler(OvenBlockEntity oven) {
        this.oven = oven;
    }

    private IItemHandler inventory() {
        return oven.getInventory();
    }

    @Override
    public int getSlots() {
        return inventory().getSlots();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return inventory().getStackInSlot(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isValidForAutomation(slot, stack) ? inventory().insertItem(slot, stack, simulate) : stack;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot == OvenBlockEntity.OUTPUT_SLOT ? inventory().extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isValidForAutomation(slot, stack);
    }

    private boolean isValidForAutomation(int slot, ItemStack stack) {
        if (stack.is(BAKEWARE)) {
            return slot == OvenBlockEntity.TOOL_SLOT;
        }
        if (oven.isPlatingItem(stack)) {
            return slot == OvenBlockEntity.CONTAINER_SLOT;
        }
        return slot < BakingRecipe.INPUT_SLOTS;
    }
}
