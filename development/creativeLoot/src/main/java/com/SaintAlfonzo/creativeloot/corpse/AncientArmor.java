package com.SaintAlfonzo.creativeloot.corpse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ancient corpse armor rolls (feature_writeup/plan_corpses.md 3.4, rulings 7 and 14 to 17; TODO `corpses #5`).
 * <ul>
 * <li>One equip chain is picked at random; its slots are rolled in order at {@code chances} (56 / 35 / 15 / 5 %) and
 * the first miss stops the chain.</li>
 * <li>Pieces 1 and 2 roll their material by weight; pieces 3 and 4 take one of those two, 50/50.</li>
 * <li>Every piece is worn (damage a random share of its durability) and rarely enchanted.</li>
 * <li>Dyed leather: all dyed pieces one color, or two colors far apart in RGB, each piece taking one of them.</li>
 * <li>Copper (Everything Is Copper, item NBT {@code state} and {@code waxed}): unwaxed pieces are fully oxidized; a
 * waxed piece takes unaffected, exposed or weathered by weight, with the dye rule (one state or two).</li>
 * </ul>
 * The result is indexed by armor slot (0 feet, 1 legs, 2 chest, 3 head), as the Corpse mod's armor inventory.
 */
public final class AncientArmor {
	private AncientArmor() {}

	static int slotIndex(String slot) {
		return switch (slot) {
			case "feet" -> 0;
			case "legs" -> 1;
			case "chest" -> 2;
			case "head" -> 3;
			default -> -1;
		};
	}

	public static ItemStack[] roll(AncientCorpseConfig.Armor a, RandomSource r) {
		ItemStack[] out = new ItemStack[4];
		for (int i = 0; i < 4; i++) out[i] = ItemStack.EMPTY;
		if (a.chains().isEmpty()) return out;
		List<String> chain = a.chains().get(r.nextInt(a.chains().size()));

		List<String> slots = new ArrayList<>();
		for (int i = 0; i < chain.size() && i < a.chances().size(); i++) {
			if (r.nextDouble() >= a.chances().get(i)) break;
			slots.add(chain.get(i));
		}
		if (slots.isEmpty()) return out;

		Map<String, Integer> weights = new java.util.LinkedHashMap<>();
		for (Map.Entry<String, Integer> e : a.materialWeights().entrySet()) if (a.items().containsKey(e.getKey())) weights.put(e.getKey(), e.getValue());
		if (weights.isEmpty()) return out;
		List<String> mats = new ArrayList<>();
		for (int i = 0; i < slots.size(); i++) {
			if (i < 2) mats.add(weighted(weights, r));
			else mats.add(mats.get(r.nextInt(2)));
		}

		int dyed = (int) mats.stream().filter("dyed_leather"::equals).count();
		int[] colors = dyed > 0 ? colors(a, r) : null;
		boolean twoColors = colors != null && colors.length > 1;

		String[] waxedStates = null;
		for (int i = 0; i < slots.size(); i++) {
			String mat = mats.get(i);
			String slot = slots.get(i);
			int idx = slotIndex(slot);
			Map<String, String> set = a.items().get(mat);
			if (idx < 0 || set == null || !set.containsKey(slot)) continue;
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(set.get(slot)));
			if (item == null || item == net.minecraft.world.item.Items.AIR) continue;
			ItemStack stack = new ItemStack(item);

			if ("dyed_leather".equals(mat)) {
				int color = twoColors ? colors[r.nextInt(2)] : colors[0];
				stack.getOrCreateTagElement("display").putInt("color", color);
			}
			if ("copper".equals(mat)) {
				boolean waxed = r.nextDouble() < a.copperWaxChance();
				String state = "oxidized";
				if (waxed) {
					if (waxedStates == null) waxedStates = waxedStates(a, r);
					state = waxedStates.length > 1 ? waxedStates[r.nextInt(2)] : waxedStates[0];
				}
				stack.getOrCreateTag().putString("state", state);
				stack.getOrCreateTag().putBoolean("waxed", waxed);
			}
			if (stack.isDamageableItem()) {
				double share = a.damageMin() + r.nextDouble() * Math.max(0.0, a.damageMax() - a.damageMin());
				stack.setDamageValue(Math.min(stack.getMaxDamage() - 1, (int) Math.round(stack.getMaxDamage() * share)));
			}
			if (r.nextDouble() < a.enchantChance()) {
				int lvl = a.enchantLevelMin() + r.nextInt(Math.max(1, a.enchantLevelMax() - a.enchantLevelMin() + 1));
				stack = EnchantmentHelper.enchantItem(r, stack, lvl, false);
			}
			out[idx] = stack;
		}
		return out;
	}

	/** One color, or two at least {@code dyeMinDistance} apart in RGB (the farthest of 64 tries if none is). */
	static int[] colors(AncientCorpseConfig.Armor a, RandomSource r) {
		int c1 = r.nextInt(0x1000000);
		if (r.nextDouble() < a.dyeSameColorChance()) return new int[]{c1};
		int best = -1;
		double bestDist = -1;
		for (int i = 0; i < 64; i++) {
			int c2 = r.nextInt(0x1000000);
			double d = distance(c1, c2);
			if (d > bestDist) {
				bestDist = d;
				best = c2;
			}
			if (d >= a.dyeMinDistance()) break;
		}
		return new int[]{c1, best};
	}

	static double distance(int c1, int c2) {
		int dr = ((c1 >> 16) & 0xFF) - ((c2 >> 16) & 0xFF);
		int dg = ((c1 >> 8) & 0xFF) - ((c2 >> 8) & 0xFF);
		int db = (c1 & 0xFF) - (c2 & 0xFF);
		return Math.sqrt(dr * dr + dg * dg + db * db);
	}

	static String[] waxedStates(AncientCorpseConfig.Armor a, RandomSource r) {
		String s1 = weightedD(a.waxedStateWeights(), r, null);
		if (r.nextDouble() < a.waxedSameStateChance()) return new String[]{s1};
		String s2 = weightedD(a.waxedStateWeights(), r, s1);
		return s2 == null ? new String[]{s1} : new String[]{s1, s2};
	}

	static String weighted(Map<String, Integer> weights, RandomSource r) {
		int total = 0;
		for (int w : weights.values()) total += Math.max(0, w);
		if (total <= 0) return weights.keySet().iterator().next();
		int pick = r.nextInt(total);
		for (Map.Entry<String, Integer> e : weights.entrySet()) {
			pick -= Math.max(0, e.getValue());
			if (pick < 0) return e.getKey();
		}
		return weights.keySet().iterator().next();
	}

	/** Weighted pick over doubles, skipping {@code except}; null when nothing is left. */
	static String weightedD(Map<String, Double> weights, RandomSource r, String except) {
		double total = 0;
		for (Map.Entry<String, Double> e : weights.entrySet()) if (!e.getKey().equals(except)) total += Math.max(0, e.getValue());
		if (total <= 0) return null;
		double pick = r.nextDouble() * total;
		String last = null;
		for (Map.Entry<String, Double> e : weights.entrySet()) {
			if (e.getKey().equals(except)) continue;
			last = e.getKey();
			pick -= Math.max(0, e.getValue());
			if (pick < 0) return e.getKey();
		}
		return last;
	}
}
