package com.SaintAlfonzo.creativeengineering.oven;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import com.SaintAlfonzo.creativeengineering.cooking.RemainderSlot;

/**
 * FD's CookingPotMenu layout (so FD's cooking_pot.png lines up) plus the tool slot left of the ingredient grid, and
 * without the recipe book. Menu slots: 0-5 ingredients, 6 tool, 7 waiting result, 8 container, 9 output, then the
 * player inventory (10-36) and hotbar (37-45), then the two leftover slots (46-47, take only).
 */
public class OvenMenu extends AbstractContainerMenu {

    public static final int TOOL_X = 8;
    public static final int TOOL_Y = 26;
    private static final int PLAYER_INV_START = OvenBlockEntity.INVENTORY_SIZE;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 36;

    public final OvenBlockEntity blockEntity;
    private final ItemStackHandler inventory;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public OvenMenu(int windowId, Inventory playerInventory, OvenBlockEntity blockEntity, ContainerData data) {
        super(OvenRegistry.OVEN_MENU.get(), windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.data = data;
        this.access = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos());

        // Ingredients, 2 rows x 3 columns
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 3; ++column) {
                addSlot(new SlotItemHandler(inventory, row * 3 + column, 30 + column * 18, 17 + row * 18));
            }
        }
        // Tool: one bakeware item, never used up
        addSlot(new SlotItemHandler(inventory, OvenBlockEntity.TOOL_SLOT, TOOL_X, TOOL_Y) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return stack.is(OvenItemHandler.BAKEWARE);
            }
        });
        // Result waiting for plating: display only
        addSlot(new SlotItemHandler(inventory, OvenBlockEntity.MEAL_DISPLAY_SLOT, 124, 26) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        // Plating container
        addSlot(new SlotItemHandler(inventory, OvenBlockEntity.CONTAINER_SLOT, 92, 55));
        // Output
        addSlot(new OvenResultSlot(playerInventory.player, blockEntity, inventory, OvenBlockEntity.OUTPUT_SLOT, 124, 55));

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
        for (int i = 0; i < blockEntity.getRemainders().getSlots(); ++i) {
            addSlot(new RemainderSlot(blockEntity.getRemainders(), i));
        }

        addDataSlots(data);
    }

    /** Client side, from IForgeMenuType: the server sends the position. */
    public OvenMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, buffer), new SimpleContainerData(3));
    }

    private static OvenBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf buffer) {
        BlockEntity blockEntity = playerInventory.player.level.getBlockEntity(buffer.readBlockPos());
        if (blockEntity instanceof OvenBlockEntity oven) {
            return oven;
        }
        throw new IllegalStateException("Block entity is not an oven: " + blockEntity);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, OvenRegistry.OVEN.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            if (index >= PLAYER_INV_END) {
                // Leftover slot: set() (not an in-place change) so the buffer marks the oven changed.
                ItemStack stack = slot.getItem().copy();
                copy = stack.copy();
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, true) || stack.getCount() == copy.getCount()) {
                    return ItemStack.EMPTY;
                }
                slot.set(stack);
                return copy;
            }
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < PLAYER_INV_START) {
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, index == OvenBlockEntity.OUTPUT_SLOT)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.is(OvenItemHandler.BAKEWARE)) {
                if (!moveItemStackTo(stack, OvenBlockEntity.TOOL_SLOT, OvenBlockEntity.TOOL_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (blockEntity.isPlatingItem(stack)) {
                if (!moveItemStackTo(stack, OvenBlockEntity.CONTAINER_SLOT, OvenBlockEntity.CONTAINER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, BakingRecipe.INPUT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copy;
    }

    public int getCookProgressionScaled() {
        int time = data.get(0);
        int total = data.get(1);
        return total != 0 && time != 0 ? time * 24 / total : 0;
    }

    public boolean isHeated() {
        return data.get(2) != 0;
    }

    public IItemHandler getInventory() {
        return inventory;
    }
}
