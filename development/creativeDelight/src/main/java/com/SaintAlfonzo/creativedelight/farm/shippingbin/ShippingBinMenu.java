package com.SaintAlfonzo.creativedelight.farm.shippingbin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Two 3x2 grids (inputs left, outputs right, take only), the fill bar and the sale arrow; the player inventory below.
 * Shift-click from the inventory goes to the inputs, from the outputs to the inventory.
 */
public class ShippingBinMenu extends AbstractContainerMenu {
	public static final int INPUT_X = 20, OUTPUT_X = 112, GRID_Y = 20;
	private final Container bin;
	private final ContainerData data;

	/** Client side. */
	public ShippingBinMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
		this(id, inventory, new SimpleContainer(ShippingBinBlockEntity.SLOTS), new SimpleContainerData(4));
	}

	public ShippingBinMenu(int id, Inventory inventory, Container bin, ContainerData data) {
		super(ShippingBin.SHIPPING_BIN_MENU.get(), id);
		checkContainerSize(bin, ShippingBinBlockEntity.SLOTS);
		this.bin = bin;
		this.data = data;
		for (int i = 0; i < ShippingBinBlockEntity.INPUTS; i++) {
			addSlot(new Slot(bin, i, INPUT_X + (i % 3) * 18, GRID_Y + (i / 3) * 18));
		}
		for (int i = 0; i < ShippingBinBlockEntity.INPUTS; i++) {
			addSlot(new Slot(bin, ShippingBinBlockEntity.INPUTS + i, OUTPUT_X + (i % 3) * 18, GRID_Y + (i / 3) * 18) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return false;
				}
			});
		}
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
			}
		}
		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(inventory, column, 8 + column * 18, 142));
		}
		addDataSlots(data);
	}

	/** Stored value (capped at 32767 for the wire). */
	public int value() {
		return data.get(0);
	}

	public int capacity() {
		return Math.max(1, data.get(1));
	}

	/** Sale bar, 0 to {@link ShippingBinBlockEntity#SALE_TICKS}. */
	public int saleProgress() {
		return data.get(2);
	}

	/** value / capacity in 1/1000, capped at 1000. */
	public int fillPermille() {
		return data.get(3);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		int binSlots = ShippingBinBlockEntity.SLOTS;
		if (index < binSlots) {
			if (!moveItemStackTo(stack, binSlots, slots.size(), true)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(stack, 0, ShippingBinBlockEntity.INPUTS, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
		else slot.setChanged();
		if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, stack);
		return original;
	}

	@Override
	public boolean stillValid(Player player) {
		return bin.stillValid(player);
	}
}
