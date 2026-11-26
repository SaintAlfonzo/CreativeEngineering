// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stage and wax on an item, as two top-level NBT keys (upstream CreateOxidizedStage / CreateOxidizedWaxed). A plain item
 * (unaffected, not waxed) carries neither key, and no empty tag is left behind, so it stacks with a fresh one. No
 * custom name is stored (R3): the name comes from the stage on display (P3).
 */
public final class OxidationItemData {
	public static final String STAGE_KEY = "CreativeOxidationStage";
	public static final String WAXED_KEY = "CreativeOxidationWaxed";

	private OxidationItemData() {
	}

	/** The item's stage and wax; empty for a plain item (no keys, or unaffected and not waxed). */
	public static Optional<StoredState> read(ItemStack stack) {
		return read(stack.getTag());
	}

	public static Optional<StoredState> read(CompoundTag tag) {
		if (tag == null || !(tag.contains(STAGE_KEY) || tag.contains(WAXED_KEY))) return Optional.empty();
		StoredState state = new StoredState(OxidationStage.fromSerializedName(tag.getString(STAGE_KEY)), tag.getBoolean(WAXED_KEY));
		return state.isPlain() ? Optional.empty() : Optional.of(state);
	}

	/** Writes the stage and wax; a plain state removes both keys (and the tag when nothing else is in it). */
	public static void write(ItemStack stack, OxidationStage stage, boolean waxed) {
		if (stage == OxidationStage.UNAFFECTED && !waxed) {
			CompoundTag tag = stack.getTag();
			if (tag == null) return;
			tag.remove(STAGE_KEY);
			tag.remove(WAXED_KEY);
			if (tag.isEmpty()) stack.setTag(null);
			return;
		}
		CompoundTag tag = stack.getOrCreateTag();
		tag.putString(STAGE_KEY, stage.getSerializedName());
		tag.putBoolean(WAXED_KEY, waxed);
	}

	public static void writeFromBlockState(ItemStack stack, BlockState state) {
		if (OxidationProperties.isOxidizable(state)) {
			write(stack, state.getValue(OxidationProperties.STAGE), state.getValue(OxidationProperties.WAXED));
		}
	}

	/** A copy of {@code base} with this stage and wax. */
	public static ItemStack staged(ItemStack base, OxidationStage stage, boolean waxed) {
		ItemStack stack = base.copy();
		write(stack, stage, waxed);
		return stack;
	}

	/** Lang key of the name prefix ("Waxed Weathered %s"), or null for a plain item. */
	public static String variantKey(StoredState state) {
		if (state.isPlain()) return null;
		return "item." + CreativeOxidation.MOD_ID + ".variant." + (state.waxed() ? "waxed." : "") + state.stage().getSerializedName();
	}

	public record StoredState(OxidationStage stage, boolean waxed) {
		public boolean isPlain() {
			return stage == OxidationStage.UNAFFECTED && !waxed;
		}
	}
}
