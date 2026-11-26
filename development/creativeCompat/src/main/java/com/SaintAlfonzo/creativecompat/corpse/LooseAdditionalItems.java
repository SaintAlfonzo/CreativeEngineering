// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.corpse;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The additional-items list minus the section stacks, indexed through a map fixed when the menu opened. Swapped in
 * as the Additional Items container's inventory by CorpseAdditionalContainerMixin. Lives outside the mixin package
 * (agent note mixin-package-helpers).
 */
public final class LooseAdditionalItems implements Container {
	private final NonNullList<ItemStack> list;
	private final int[] map;

	public LooseAdditionalItems(NonNullList<ItemStack> list, int cap) {
		this.list = list;
		CorpseSections.Layout layout = CorpseSections.layout(list, cap);
		List<Integer> keep = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (!layout.holds(list.get(i))) keep.add(i);
		}
		map = keep.stream().mapToInt(Integer::intValue).toArray();
	}

	private boolean valid(int slot) {
		return slot >= 0 && slot < map.length && map[slot] < list.size();
	}

	@Override
	public int getContainerSize() {
		return map.length;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < map.length; i++) if (!getItem(i).isEmpty()) return false;
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return valid(slot) ? list.get(map[slot]) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (!valid(slot)) return ItemStack.EMPTY;
		return ContainerHelper.removeItem(list, map[slot], amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (!valid(slot)) return ItemStack.EMPTY;
		return ContainerHelper.takeItem(list, map[slot]);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (valid(slot)) list.set(map[slot], stack);
	}

	@Override
	public void setChanged() {}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < map.length; i++) setItem(i, ItemStack.EMPTY);
	}
}
