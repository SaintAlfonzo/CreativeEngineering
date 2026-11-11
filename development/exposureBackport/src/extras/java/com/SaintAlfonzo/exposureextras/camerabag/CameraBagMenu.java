package com.SaintAlfonzo.exposureextras.camerabag;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The open bag. Same model as Exposure's camera attachments menu: the contents are copied into a
 * SimpleContainer and written back to the bag after every change, and the bag's own inventory slot
 * is locked (hidden, can't be picked up or number-key swapped) so it can't move while open.
 *
 * <p>Slot layout: up to 9 per row, rows balanced (14 = 7 + 7), centred in the top panel. The slot
 * count comes from the server (exposureextras-common.toml [camera_bag]) in the open packet, so the client agrees.
 */
public class CameraBagMenu extends AbstractContainerMenu {

    public static final int MAX_SLOTS = 27;
    private static final int PANEL_WIDTH = 176;
    private static final int TOP_AREA_Y = 17;
    private static final int TOP_AREA_HEIGHT = 54;

    private final int bagSlot;
    private final int size;
    /** The bag as it was when opened. Writes go to this stack; the menu closes if it leaves its slot. */
    private final ItemStack bag;

    public CameraBagMenu(int containerId, Inventory playerInventory, int bagSlot, int size) {
        super(CameraBag.MENU.get(), containerId);
        this.bagSlot = bagSlot;
        this.size = Math.max(1, Math.min(MAX_SLOTS, size));
        this.bag = playerInventory.getItem(bagSlot);

        SimpleContainer contents = new SimpleContainer(CameraBag.load(bag, this.size)) {
            @Override
            public void setChanged() {
                super.setChanged();
                CameraBag.save(bag, this);
            }
        };
        addBagSlots(contents);
        addPlayerSlots(playerInventory);
    }

    public static CameraBagMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        return new CameraBagMenu(containerId, playerInventory, buf.readVarInt(), buf.readVarInt());
    }

    /** Number of bag slots; they are menu slots 0..size-1, the player's 36 follow. */
    public int bagSize() {
        return size;
    }

    private void addBagSlots(SimpleContainer contents) {
        int rows = (size + 8) / 9;
        int perRow = (size + rows - 1) / rows;
        int top = TOP_AREA_Y + (TOP_AREA_HEIGHT - rows * 18) / 2;
        for (int index = 0; index < size; index++) {
            int row = index / perRow;
            int inRow = Math.min(perRow, size - row * perRow);
            int left = (PANEL_WIDTH - inRow * 18) / 2 + 1;
            addSlot(new Slot(contents, index, left + (index % perRow) * 18, top + row * 18 + 1) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return CameraBag.canHold(stack);
                }
            });
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new PlayerSlot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new PlayerSlot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    /** A player inventory slot that is locked (and drawn empty) while it holds the open bag. */
    private class PlayerSlot extends Slot {
        PlayerSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        private boolean isBag() {
            return getContainerSlot() == bagSlot;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !isBag() && super.mayPickup(player);
        }

        @Override
        public boolean isActive() {
            return !isBag();
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Number key / F onto the bag's own hotbar or offhand slot would swap the bag out.
        if (clickType == ClickType.SWAP && button == bagSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < size) {
            if (!moveItemStackTo(stack, size, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!CameraBag.canHold(stack) || !moveItemStackTo(stack, 0, size, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(bagSlot) == bag;
    }
}
