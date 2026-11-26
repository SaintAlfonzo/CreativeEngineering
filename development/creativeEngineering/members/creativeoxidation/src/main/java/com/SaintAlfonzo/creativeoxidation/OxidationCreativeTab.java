// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * "Creative Oxidation": every target block that has an item, in OxidationTargets order, as the 7 variants (exposed,
 * weathered, oxidized, and waxed unaffected to waxed oxidized). The plain items stay in their own mods' tabs. Listed
 * whatever keepStageOnBreak says (R16: every variant viewable in JEI; spout and fan still make them).
 */
public final class OxidationCreativeTab extends CreativeModeTab {
	public OxidationCreativeTab() {
		super(CreativeOxidation.MOD_ID);
	}

	@Override
	public ItemStack makeIcon() {
		Item casing = item("create:copper_casing");
		ItemStack icon = new ItemStack(casing == null ? Items.COPPER_BLOCK : casing);
		if (casing != null) OxidationItemData.write(icon, OxidationStage.OXIDIZED, false);
		return icon;
	}

	@Override
	public void fillItemList(NonNullList<ItemStack> items) {
		for (Item item : targetItems()) {
			ItemStack base = new ItemStack(item);
			for (OxidationStage stage : OxidationStage.values()) {
				if (stage != OxidationStage.UNAFFECTED) items.add(OxidationItemData.staged(base, stage, false));
				items.add(OxidationItemData.staged(base, stage, true));
			}
		}
	}

	/**
	 * The target items in OxidationTargets order (the tab, the JEI subtype interpreter and the JEI world rows): each
	 * target block's item, or for a block without one the placer item that places it (OxidationTargets.PLACER_ITEMS,
	 * the Create Deco coin stack); blocks with neither (glass and encased fluid pipes) are left out.
	 */
	public static List<Item> targetItems() {
		Set<Item> out = new LinkedHashSet<>();
		for (String id : OxidationTargets.ORDERED_BLOCK_IDS) {
			Item item = item(id);
			if (item == null) item = placerItem(id);
			if (item != null) out.add(item);
		}
		return List.copyOf(out);
	}

	private static Item placerItem(String blockId) {
		for (Map.Entry<String, String> e : OxidationTargets.PLACER_ITEMS.entrySet()) {
			if (!e.getValue().equals(blockId)) continue;
			ResourceLocation id = ResourceLocation.tryParse(e.getKey());
			if (id != null && Registry.ITEM.containsKey(id)) return Registry.ITEM.get(id);
		}
		return null;
	}

	/** The item of a target block, or null when the block (its mod) is missing or has no item. */
	public static Item item(String blockId) {
		ResourceLocation id = ResourceLocation.tryParse(blockId);
		if (id == null || !Registry.BLOCK.containsKey(id)) return null;
		Block block = Registry.BLOCK.get(id);
		Item item = block.asItem();
		return item == Items.AIR ? null : item;
	}
}
