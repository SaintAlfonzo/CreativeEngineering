// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Stage and wax on an item, as two top-level NBT keys (upstream CreateOxidizedStage / CreateOxidizedWaxed). A plain item
 * (unaffected, not waxed) carries neither key, and no empty tag is left behind, so it stacks with a fresh one. No
 * custom name is stored (R3): the name comes from the stage on display ({@link #displayName}, ItemStackMixin).
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

	/**
	 * The shown name (R3): "Waxed Weathered Fluid Pipe" from the stage keys around the item's own name. ItemStackMixin
	 * asks only when the stack has no custom name, so an anvil rename shows the rename.
	 */
	public static Component displayName(ItemStack stack, Component base) {
		Optional<StoredState> stored = read(stack);
		return stored.isEmpty() ? base : Component.translatable(variantKey(stored.get()), base);
	}

	/** A target's own item: a BlockItem of a target block, or a target's placer item (OxidationTargets.PLACER_ITEMS). */
	public static boolean isTargetItem(ItemStack stack) {
		Item item = stack.getItem();
		String blockId = item instanceof BlockItem blockItem ? id(ForgeRegistries.BLOCKS.getKey(blockItem.getBlock())) : null;
		return OxidationTargets.isTargetItem(id(ForgeRegistries.ITEMS.getKey(item)), blockId);
	}

	/**
	 * Breaking a staged or waxed block (P3, R8): with keepStageOnBreak on, every drop that is a target's own item
	 * takes the stage and wax (the coin stack block drops createdeco:copper_coinstack; glass and encased fluid pipes
	 * drop the fluid pipe). Off, drops stay plain.
	 */
	public static void keepStageInDrops(BlockState state, List<ItemStack> drops) {
		if (!OxidationProperties.isOxidizable(state)) return;
		OxidationStage stage = state.getValue(OxidationProperties.STAGE);
		boolean waxed = state.getValue(OxidationProperties.WAXED);
		if (!OxidationRules.dropKeepsStage(OxidationConfig.keepStageOnBreak(), waxed, stage)) return;
		for (ItemStack drop : drops) {
			if (!drop.isEmpty() && isTargetItem(drop)) write(drop, stage, waxed);
		}
	}

	private static String id(ResourceLocation key) {
		return key == null ? null : key.toString();
	}
}
