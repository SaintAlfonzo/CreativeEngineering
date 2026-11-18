// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.perf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rule lines of {@code [perf] idleRandomTickRules} (feature_writeup/random_tick_audit.md): which block states stop
 * claiming random ticks. Pure Java (no game classes) so it is testable headless; {@link IdleRandomTicks} applies it.
 *
 * <p>Line format: {@code <block id> [<property>=<value> | <property>!=<value> ...] [+wake_above]}. Every condition
 * must hold (AND); several lines for one block are alternatives (OR); a line with no condition stops every state of
 * the block. A value is the property's serialized value, or {@code max} / {@code min} for the highest / lowest value
 * of an integer property. {@code +wake_above}: while stopped, a block newly placed directly above makes the block run
 * its own random tick again for a few scheduled tries (tomato vines climbing a rope placed above a grown vine).
 */
public final class IdleTickRules {
	private IdleTickRules() {}

	public static final String WAKE_ABOVE = "+wake_above";

	/** One property test. */
	public record Cond(String prop, boolean negate, String value) {}

	/** One parsed line. */
	public record Rule(String blockId, List<Cond> conds, boolean wakeAbove) {}

	/** What a rule needs to know about one block state. */
	public interface StateView {
		/** Serialized value of the property, or null when the state has no such property. */
		String value(String prop);

		/** Serialized highest (max = true) or lowest value of an integer property; null for other properties. */
		String bound(String prop, boolean max);
	}

	/** Parses one line; throws IllegalArgumentException with a reason on a malformed line. */
	public static Rule parse(String line) {
		String[] parts = line.trim().split("\\s+");
		if (parts.length == 0 || parts[0].isEmpty()) throw new IllegalArgumentException("empty line");
		String id = parts[0].toLowerCase(Locale.ROOT);
		if (id.indexOf(':') <= 0 || id.endsWith(":")) throw new IllegalArgumentException("not a block id: " + parts[0]);
		List<Cond> conds = new ArrayList<>();
		boolean wake = false;
		for (int i = 1; i < parts.length; i++) {
			String p = parts[i];
			if (p.equalsIgnoreCase(WAKE_ABOVE)) {
				wake = true;
				continue;
			}
			int ne = p.indexOf("!=");
			int eq = p.indexOf('=');
			boolean negate = ne > 0;
			int cut = negate ? ne : eq;
			int valueAt = negate ? ne + 2 : eq + 1;
			if (cut <= 0 || valueAt >= p.length()) throw new IllegalArgumentException("not a condition: " + p);
			conds.add(new Cond(p.substring(0, cut).toLowerCase(Locale.ROOT), negate,
					p.substring(valueAt).toLowerCase(Locale.ROOT)));
		}
		return new Rule(id, List.copyOf(conds), wake);
	}

	/**
	 * Null when every property the rule names exists on the block (and max / min only on integer properties), else the
	 * first problem. Checked once per block so a typo is reported instead of silently never matching.
	 */
	public static String check(Rule rule, StateView anyState) {
		for (Cond c : rule.conds()) {
			if (anyState.value(c.prop()) == null) return "no property '" + c.prop() + "'";
			if ((c.value().equals("max") || c.value().equals("min")) && anyState.bound(c.prop(), true) == null) {
				return "'" + c.prop() + "' is not an integer property, so " + c.value() + " does not apply";
			}
		}
		return null;
	}

	public static boolean matches(Rule rule, StateView state) {
		for (Cond c : rule.conds()) {
			String have = state.value(c.prop());
			if (have == null) return false;
			String want = switch (c.value()) {
				case "max" -> state.bound(c.prop(), true);
				case "min" -> state.bound(c.prop(), false);
				default -> c.value();
			};
			if (want == null) return false;
			if (have.equals(want) == c.negate()) return false;
		}
		return true;
	}

	/** Pack defaults: rows 1-7, 9, 11 and 12 of random_tick_audit.md (owner, 2026-11-17). */
	public static List<String> defaults() {
		List<String> l = new ArrayList<>(List.of(
				// 1: FD tomato vines and the Supplementaries / Creative Delight climbing variants. Grown vines still
				// climb a rope or stick placed above later (+wake_above); a picked vine is age 0 and ticks again.
				"farmersdelight:tomatoes age=max " + WAKE_ABOVE,
				"supplementaries:rope_tomatoes age=max " + WAKE_ABOVE,
				"supplementaries:stick_tomatoes age=max " + WAKE_ABOVE,
				"creativedelight:treated_stick_tomatoes age=max " + WAKE_ABOVE,
				// 2: rice, grown and already carrying its panicles (supporting turns false when they are cut)
				"farmersdelight:rice age=max supporting=true",
				// 3-5: mushroom and fungus colonies at full size
				"farmersdelight:brown_mushroom_colony age=max",
				"farmersdelight:red_mushroom_colony age=max",
				"fungidelight:inky_cap_mushroom_colony age=max",
				"fungidelight:morel_mushroom_colony age=max",
				"nethersdelight:crimson_fungus_colony age=max",
				"nethersdelight:warped_fungus_colony age=max",
				// 6: the top half of hemp never grows (the grown bottom still regrows a cut top)
				"immersiveengineering:hemp growth=top0",
				// 7, 9: Neapolitan strawberry bush and adzuki sprouts at full age (mint, row 8, keeps spreading)
				"neapolitan:strawberry_bush age=max",
				"neapolitan:adzuki_sprouts age=max",
				// 11: Redeco stone lanterns, waxed or fully mossed
				"redeco:stone_lantern waxed!=0",
				"redeco:stone_lantern mossing=max",
				"redeco:deepslate_lantern waxed!=0",
				"redeco:deepslate_lantern mossing=max"));
		// 12: Floral Enchantment flower bushes: their growth method has the 1.18 signature, never called on 1.19.2
		// (they grow only with bone meal), so the ticks do nothing (owner, 2026-11-17: stop them)
		for (String f : List.of("allium", "azure_bluet", "blue_orchid", "cornflower", "dandelion", "lilac",
				"lily_of_the_valley", "orange_tulip", "oxeye_daisy", "peony", "pink_tulip", "poppy", "red_tulip",
				"rose_bush", "sunflower", "white_tulip", "wither_rose")) {
			l.add("floralench:" + f + "_bush");
		}
		return List.copyOf(l);
	}
}
