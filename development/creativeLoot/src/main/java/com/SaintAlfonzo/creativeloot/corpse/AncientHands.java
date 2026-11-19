// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeloot.corpse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ancient corpse held items (feature_writeup/plan_corpses.md 3.5, rulings 24 to 26 and 28; TODO `corpses #8`).
 * <ul>
 * <li>Main hand at {@code main_chance} (45%): a sword or pickaxe ({@code tool_weights}) of a material by
 * {@code material_weights} (wood, stone, copper 25, iron 8).</li>
 * <li>Offhand on its own roll at {@code offhand_chance} (20%; ruling 25 as answered 2026-11-18): the corpse kind's
 * supply ({@code offhand_supplies}: cave torches 1 to 6, ocean bread 1 to 3) at {@code offhand_supply_chance}, else a
 * tool, which copies the main hand tool's material at {@code same_material_chance} when there is one.</li>
 * <li>Tools are worn and rarely enchanted like the armor; copper follows the armor's copper rules and shares the
 * corpse's waxed states ({@link AncientArmor#copper}). No dye.</li>
 * <li>{@code guarantee_tool} (ruling 24): when the armor and both hands rolled nothing, the main hand gets a tool,
 * so no ancient corpse spawns empty.</li>
 * </ul>
 * Returns {main hand, offhand}.
 */
public final class AncientHands {
	private AncientHands() {}

	public static ItemStack[] roll(AncientCorpseConfig.Hands h, AncientCorpseConfig.Armor a, RandomSource r, String[][] wax,
			String kind, boolean armorRolled) {
		ItemStack main = ItemStack.EMPTY;
		String mainMat = null;
		if (r.nextDouble() < h.mainChance()) {
			mainMat = material(h, r, null);
			main = tool(h, a, r, wax, mainMat);
		}
		ItemStack off = ItemStack.EMPTY;
		if (r.nextDouble() < h.offhandChance()) {
			AncientCorpseConfig.Supply sup = h.supplies().get(kind);
			if (sup != null && r.nextDouble() < h.offhandSupplyChance()) off = supply(sup, r);
			else off = tool(h, a, r, wax, material(h, r, main.isEmpty() ? null : mainMat));
		}
		if (h.guaranteeTool() && !armorRolled && main.isEmpty() && off.isEmpty()) {
			main = tool(h, a, r, wax, material(h, r, null));
		}
		return new ItemStack[]{main, off};
	}

	/** A weighted material that has an item for at least one tool type; with {@code copy}, that one at same_material_chance. */
	static String material(AncientCorpseConfig.Hands h, RandomSource r, String copy) {
		if (copy != null && r.nextDouble() < h.sameMaterialChance()) return copy;
		Map<String, Integer> weights = new LinkedHashMap<>();
		for (Map.Entry<String, Integer> e : h.materialWeights().entrySet()) if (h.items().containsKey(e.getKey())) weights.put(e.getKey(), e.getValue());
		return weights.isEmpty() ? null : AncientArmor.weighted(weights, r);
	}

	static ItemStack tool(AncientCorpseConfig.Hands h, AncientCorpseConfig.Armor a, RandomSource r, String[][] wax, String mat) {
		if (mat == null || h.toolWeights().isEmpty()) return ItemStack.EMPTY;
		Map<String, String> set = h.items().get(mat);
		if (set == null) return ItemStack.EMPTY;
		String type = AncientArmor.weighted(h.toolWeights(), r);
		Item item = set.containsKey(type) ? ForgeRegistries.ITEMS.getValue(new ResourceLocation(set.get(type))) : null;
		if (item == null || item == Items.AIR) return ItemStack.EMPTY;
		ItemStack stack = new ItemStack(item);
		if ("copper".equals(mat)) AncientArmor.copper(stack, a, r, wax);
		return AncientArmor.wear(stack, h.damageMin(), h.damageMax(), h.enchantChance(), h.enchantLevelMin(), h.enchantLevelMax(), r);
	}

	static ItemStack supply(AncientCorpseConfig.Supply s, RandomSource r) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s.item()));
		if (item == null || item == Items.AIR) return ItemStack.EMPTY;
		int n = s.min() + r.nextInt(Math.max(1, s.max() - s.min() + 1));
		return n > 0 ? new ItemStack(item, Math.min(n, item.getMaxStackSize())) : ItemStack.EMPTY;
	}
}
