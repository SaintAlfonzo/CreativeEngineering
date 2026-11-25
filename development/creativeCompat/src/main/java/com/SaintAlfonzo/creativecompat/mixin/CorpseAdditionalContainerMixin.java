package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.corpse.CorpseSections;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseContainerBase;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Additional Items without the corpse's section stacks (corpse/CorpseSections; feature_writeup/plan_corpses.md 3.6
 * item 3). On the server the container's {@code inventory} (the Corpse mod's view of the whole additional-items list)
 * is swapped at {@code <init>} TAIL for a filtered view over the stacks the sections do not show, and the page is
 * rebuilt with {@code setSlots(0)}, so the pages, Transfer Items and Corpse Curios Compat all see the filtered list.
 * The client container keeps the mod's own (it holds placeholder stacks only).
 * <p>{@code setSlots} HEAD trims the menu's {@code lastSlots} and {@code remoteSlots} ({@link CorpseSections#trimSynced}): the mod clears only
 * {@code slots}, so each call grew them by 90 and the next full sync (opening the menu) sent 180 stacks to a 90-slot
 * client menu, IndexOutOfBounds 90 on every content packet (creative-compat-publish #20; page switches too).
 * Applied only with the Corpse mod (CompatMixinPlugin). remap = false.
 */
@Mixin(value = CorpseAdditionalContainer.class, remap = false)
public abstract class CorpseAdditionalContainerMixin extends CorpseContainerBase {

	@Shadow
	public abstract void setSlots(int start);

	private CorpseAdditionalContainerMixin(MenuType<?> type, int id, Inventory inv, CorpseEntity corpse, boolean editable, boolean history) {
		super(type, id, inv, corpse, editable, history);
	}

	@Inject(method = "setSlots", at = @At("HEAD"), remap = false)
	private void creativecompat$trimSynced(int start, CallbackInfo ci) {
		CorpseSections.trimSynced(this);
	}

	@Inject(method = "<init>", at = @At("TAIL"), remap = false)
	private void creativecompat$filter(int id, Inventory playerInventory, CorpseEntity corpse, boolean editable, boolean history,
			CallbackInfo ci) {
		if (corpse.level.isClientSide) return;
		int cap = CorpseSections.capOf(corpse);
		if (cap < 0) return;
		this.inventory = new Loose(corpse.getDeath().getAdditionalItems(), Math.min(cap, CorpseSections.MAX_CAP));
		setSlots(0);
	}

	/** The additional-items list minus the section stacks, indexed through a map fixed when the menu opened. */
	private static final class Loose implements Container {
		private final NonNullList<ItemStack> list;
		private final int[] map;

		Loose(NonNullList<ItemStack> list, int cap) {
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
}
