package com.SaintAlfonzo.creativebrewing;

import net.minecraftforge.common.ForgeConfigSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creative Brewing's config, {@code config/creativebrewing-common.toml} (creative_brewing.md 2.3):
 * <ul>
 * <li>{@code [members]}: one switch per nested member, default on; off makes CB's scripts skip that member's rows
 * ({@code global.CB.on(id)}), the member's own data stays.</li>
 * <li>one section per family of moved pack features ({@link #SECTIONS}: {@code brewing}, {@code alcohol},
 * {@code effects}), one switch per feature, default on; scripts read it through {@code global.CB.feature(id)}, Java
 * through {@link #feature}. A section is written only once it has a switch.</li>
 * </ul>
 * Dials moved from creativeengineering-common.toml keep their keys, comments and defaults (creative_brewing.md 4.3):
 * {@code [alcohol]}, {@code [effects]}, {@code [magic_resistance]}, {@code [copper_poisoning]} ({@link #MOVED_SECTIONS},
 * phase 3, 2026-11-01). {@code [alcohol]} and {@code [effects]} also take that family's feature switches.
 *
 * Scripts read it through {@code global.CB} (kubejs_startup/creativebrewing/cb_core.js). COMMON configs load after
 * mod construction, so a startup script must read it inside an event callback, not at top level; before the file is
 * loaded every switch reads as its default.
 */
public final class CBConfig {

	/** Mod ids of the nested members, from creativebrewing_members.txt (generated from bundle_members). */
	public static final List<String> MEMBERS = readMembers();

	/** Feature sections, in file order. */
	public static final List<String> SECTIONS = List.of("brewing", "alcohol", "effects");

	/**
	 * Feature switches: {section, id, comment lines...}. Add one row per feature as it moves in from patcherMod or the
	 * pack scripts (creative_brewing.md 4); the id is what {@code global.CB.feature(id)} and {@link #feature} take.
	 */
	private static final List<String[]> FEATURE_ROWS = List.of();

	/** Feature sections that also hold moved dials (built by their dial class). */
	private static final List<String> DIAL_SECTIONS = List.of("alcohol", "effects");

	/** Sections moved here from creativeengineering-common.toml (phase 3), for the carry-over and the leftover WARN. */
	public static final List<String> MOVED_SECTIONS = List.of("alcohol", "effects", "magic_resistance", "copper_poisoning");

	public static final ForgeConfigSpec SPEC;
	public static final Alcohol ALCOHOL;
	public static final Effects EFFECTS;
	public static final Magic MAGIC;
	public static final CopperPoisoning COPPER_POISONING;
	private static final Map<String, ForgeConfigSpec.BooleanValue> MEMBER_SWITCHES = new LinkedHashMap<>();
	private static final Map<String, ForgeConfigSpec.BooleanValue> FEATURES = new LinkedHashMap<>();

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		b.comment("One switch per Creative Brewing member. Off: Creative Brewing's scripts skip that member's recipe,",
				"tag and loot rows (the member's own data is untouched). Server scripts re-read it on /reload.").push("members");
		for (String id : MEMBERS) MEMBER_SWITCHES.put(id, b.define(id, true));
		b.pop();

		// Sections with only feature switches; alcohol and effects also hold dials moved from patcherMod and write
		// their switches inside their own section (featureRows).
		for (String section : SECTIONS) {
			if (DIAL_SECTIONS.contains(section) || rows(section).isEmpty()) continue;
			b.comment("Creative Brewing " + section + " features (feature_writeup/creative_brewing.md 4). Off: that feature",
					"is skipped entirely. Startup and Java parts need a restart, server scripts re-read on /reload, client",
					"scripts on F3+T.").push(section);
			featureRows(b, section);
			b.pop();
		}
		// Moved from creativeengineering-common.toml on 2026-11-01 (creative_brewing.md 4.2 phase 3): same keys,
		// comments and defaults; ConfigRename.mergeSections carries the player's values over on the first boot.
		ALCOHOL = new Alcohol(b);
		EFFECTS = new Effects(b);
		MAGIC = new Magic(b);
		COPPER_POISONING = new CopperPoisoning(b);
		SPEC = b.build();
	}

	private static List<String[]> rows(String section) {
		return FEATURE_ROWS.stream().filter(r -> r[0].equals(section)).toList();
	}

	/** The feature switches of {@code section}, inside the section the caller has pushed. */
	private static void featureRows(ForgeConfigSpec.Builder b, String section) {
		for (String[] row : rows(section)) {
			String[] comment = new String[row.length - 2];
			System.arraycopy(row, 2, comment, 0, comment.length);
			if (FEATURES.containsKey(row[1])) throw new IllegalStateException("duplicate CB feature id " + row[1]);
			FEATURES.put(row[1], b.comment(comment).define(row[1], true));
		}
	}

	private CBConfig() {}

	/** True if feature switch {@code id} is on (or the config is not loaded yet); false for an unknown id. */
	public static boolean feature(String id) {
		ForgeConfigSpec.BooleanValue v = FEATURES.get(id);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	/** True if {@code modId} is one of the nested members. */
	public static boolean isMember(String modId) {
		return MEMBERS.contains(modId);
	}

	/** True if {@code modId} is a member and its switch is on (or the config is not loaded yet). */
	public static boolean member(String modId) {
		ForgeConfigSpec.BooleanValue v = MEMBER_SWITCHES.get(modId);
		if (v == null) return false;
		return !SPEC.isLoaded() || v.get();
	}

	public static List<String> members() {
		return MEMBERS;
	}

	private static List<String> readMembers() {
		List<String> ids = new ArrayList<>();
		try (InputStream in = CBConfig.class.getResourceAsStream("/creativebrewing_members.txt")) {
			if (in == null) return List.of();
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String line; (line = r.readLine()) != null; ) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) ids.add(line);
			}
		} catch (IOException e) {
			throw new IllegalStateException("creativebrewing_members.txt unreadable", e);
		}
		return Collections.unmodifiableList(ids);
	}

	/** Dials for {@code potion/MagicResistanceEffect}'s damage-reduction curve. */
	public static final class Magic {
		public final ForgeConfigSpec.DoubleValue reductionPerLevel;
		public final ForgeConfigSpec.ConfigValue<String> negatedEffectTicks;

		private Magic(ForgeConfigSpec.Builder b) {
			b.comment(
					"Tuning for creativebrewing:magic_resistance's damage reduction against",
					"the creativecommon:magic damage tag (the same tag the shields' MAGIC column",
					"reads: potions, poison, evoker fangs, thorns, sonic booms, curses, life drain) and",
					"against the periodic damage of the effects listed in negatedEffectTicks."
			).push("magic_resistance");

			reductionPerLevel = b.comment(
					"Damage reduction per effect amplifier level (amplifier 0 = level I), same",
					"shape as vanilla Resistance. Total reduction is capped at 1.0 (full immunity)",
					"regardless of amplifier."
			).defineInRange("reductionPerLevel", 0.2, 0.0, 1.0);

			negatedEffectTicks = b.comment(
					"Effects whose periodic damage Magic Resistance stops outright, comma-separated",
					"effect ids. A tick of one of these deals no damage while its level is at most the",
					"Magic Resistance level (Poison I against Magic Resistance I); a higher level takes",
					"the per-level cut instead. These are ticks no shield can block",
					"([shields].unblockableEffectTicks)."
			).define("negatedEffectTicks", "minecraft:poison, minecraft:wither");

			b.pop(); // magic_resistance
		}
	}


	/** Copper poisoning from oxidized Everything Copper gear ({@code potion/CopperPoisoningHandler}). */
	public static final class CopperPoisoning {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue intervalSeconds;
		public final ForgeConfigSpec.IntValue jitterSeconds;
		public final ForgeConfigSpec.DoubleValue chancePerPiece;
		public final ForgeConfigSpec.IntValue baseSeconds;
		public final ForgeConfigSpec.IntValue secondsPerExtraPiece;
		public final ForgeConfigSpec.IntValue maxSeconds;
		public final ForgeConfigSpec.IntValue effectPoisonSeconds;

		private CopperPoisoning(ForgeConfigSpec.Builder b) {
			b.comment(
					"Fully oxidized Everything Copper gear (armor worn, or any piece in the hotbar or",
					"offhand) poisons its holder now and then, and the Copper Poisoning effect (copper",
					"nuggets) gives Poison instead of dealing damage itself."
			).push("copper_poisoning");

			enabled = b.comment("Roll for poison from oxidized gear.").define("enabled", true);
			intervalSeconds = b.comment("Average seconds between rolls, per player.")
					.defineInRange("intervalSeconds", 300, 10, 86400);
			jitterSeconds = b.comment("Random spread either side of intervalSeconds.")
					.defineInRange("jitterSeconds", 120, 0, 86400);
			chancePerPiece = b.comment(
					"Chance per fully oxidized piece that a roll poisons; pieces roll independently, so",
					"n pieces give 1 - (1 - chancePerPiece)^n."
			).defineInRange("chancePerPiece", 0.15, 0.0, 1.0);
			baseSeconds = b.comment("Poison I duration with one oxidized piece.")
					.defineInRange("baseSeconds", 4, 1, 600);
			secondsPerExtraPiece = b.comment("Extra Poison seconds per oxidized piece after the first.")
					.defineInRange("secondsPerExtraPiece", 2, 0, 600);
			maxSeconds = b.comment("Cap on the Poison duration from one roll.")
					.defineInRange("maxSeconds", 20, 1, 600);
			effectPoisonSeconds = b.comment(
					"Poison I seconds the Copper Poisoning effect gives per effect level (a copper nugget",
					"gives level II, so twice this)."
			).defineInRange("effectPoisonSeconds", 4, 1, 600);

			b.pop(); // copper_poisoning
		}
	}


	/** Alcoholic drinks (alcohol.md 11): the pack side lives in KubeJS {@code global.alcohol}; this is the client tooltip. */
	public static final class Alcohol {
		public final ForgeConfigSpec.BooleanValue quarkLine;

		private Alcohol(ForgeConfigSpec.Builder b) {
			b.comment("Alcoholic drinks (client side).").push("alcohol");

			quarkLine = b.comment(
					"Replace Quark's saturation word ('Disgusting Snack') with 'Intoxicating Drink' on every food whose",
					"effects include Brewin' and Chewin's Intoxication (alcohol.IntoxicatingDrinkTooltip)."
			).define("quarkLine", true);

			featureRows(b, "alcohol");
			b.pop(); // alcohol
		}
	}


	/** Mob effect category overrides ({@code potion.EffectCategories}, {@code mixin/MobEffectCategoryMixin}). */
	public static final class Effects {
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> categoryOverrides;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> forceHudIcon;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> forceHudIconItems;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> remap;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> removed;

		private Effects(ForgeConfigSpec.Builder b) {
			b.comment("Mob effects (client and server).").push("effects");
			categoryOverrides = b.comment(
					"Category overrides, \"namespace:effect=CATEGORY\" with CATEGORY one of BENEFICIAL, HARMFUL, NEUTRAL.",
					"The category colours the effect's name in potion, food and gear tooltips (HARMFUL red, the other",
					"two blue), and AppleSkin, Quark's saturation word and isBeneficial() go by it. The defaults are the",
					"effects whose own category showed the wrong colour (feature_writeup/effects.md 2.4). Read live."
			).defineList("categoryOverrides", List.of(
					"create_confectionery:rest=BENEFICIAL",
					"alexsmobs:debilitating_sting=HARMFUL",
					"alexsmobs:fear=HARMFUL",
					"alexsmobs:power_down=HARMFUL",
					"neapolitan:slipping=HARMFUL",
					"vinery:trippy=HARMFUL"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=(BENEFICIAL|HARMFUL|NEUTRAL)"));
			forceHudIcon = b.comment(
					"Effects that always get their HUD icon (top right), even when the mod that gave them hid it",
					"(showIcon false, mostly to drop particles). The inventory list shows them either way. Armor, held",
					"tool and aura effects are left hidden on purpose (feature_writeup/effects.md 2.6). Client, read live."
			).defineList("forceHudIcon", List.of(
					"brewinandchewin:intoxication",
					"via_romana:travellers_fatigue",
					"monobank:thief"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			forceHudIconItems = b.comment(
					"Items whose effects always get their HUD icon: every effect added while an entity finishes using",
					"one (eating, drinking, the item's own code). \"namespace\" for a whole mod or \"namespace:item\". Effect",
					"ids in forceHudIcon show from any source; this list only for these items, so the same effect from",
					"armor stays hidden. Server side, read live."
			).defineList("forceHudIconItems", List.of(
					"create:builders_tea",
					"canned_goods:canned_pufferfishs",
					"farmersrespite",
					"immersive_weathering"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+(:[a-z0-9_/.-]+)?"));
			remap = b.comment(
					"Effects swapped for another wherever they are applied, \"from=to\". Same duration, level and",
					"visibility. Covers foods, potions, blocks, commands and mob code (LivingEntity#addEffect), and",
					"Fruits Delight's jam / jelly effect lines. Food tooltips are swapped by KubeJS",
					"(global.foodEffectSwaps). Server side, read live."
			).defineList("remap", List.of(
					"neapolitan:agility=vinery:climbing_effect",
					"fruitsdelight:chorus=vinery:teleport",
					"quark:resilience=alexsmobs:knockback_resistance"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			removed = b.comment(
					"Effects that are never applied (LivingEntity#addEffect refuses them). Their brews are removed",
					"by KubeJS (startup_scripts/cooking/potions.js). Server side, read live."
			).defineList("removed", List.of(
					"upgrade_aquatic:restfulness",
					"upgrade_aquatic:insomnia",
					"alexsmobs:power_down"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			featureRows(b, "effects");
			b.pop(); // effects
		}
	}
}
