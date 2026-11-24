package com.SaintAlfonzo.creativecommon;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Central Forge-native config spec for {@code creativecommon}, backing
 * {@code config/creativecommon-common.toml}. Unlike the KubeJS {@code global.config}
 * JSON framework, values defined here show up in Create's in-game Configs screen
 * (reachable from the pause menu), are editable live, and hot-reload without a restart.
 *
 * <p>Registered once, in {@link CreativeCommon}'s constructor - registration must
 * happen during mod construction, since Forge loads every mod's COMMON/CLIENT config
 * immediately after construction finishes for every mod (any later, e.g. from
 * {@code FMLCommonSetupEvent}, is silently too late and the file is never read).
 */
public final class CommonConfig {

	private CommonConfig() {}

	/**
	 * The toml tables of this file, carried over from creativeengineering-common.toml on the first boot after the swap.
	 * {@code wandering_trader} is no longer read here (Creative Loot, wave 2 carve); it stays in the list so a pack that
	 * skipped every version since still brings it along, and Creative Loot carries it on from this file.
	 */
	public static final List<String> SECTIONS = List.of("changelog", "damage_tags", "perf", "wandering_trader", "advancements", "mod_names");

	/** Sections taken over from a dependent jar's file (plan_cb_polish.md 2); that jar carries them over. */
	public static final List<String> EFFECT_SECTIONS = List.of("effects", "magic_resistance");

	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Changelog CHANGELOG;
	public static final DamageTagConfig DAMAGE_TAGS;
	public static final Perf PERF;
	public static final Advancements ADVANCEMENTS;
	public static final RecipeBook RECIPE_BOOK;
	public static final CreativeMenu CREATIVE_MENU;
	public static final ModNames MOD_NAMES;
	public static final Effects EFFECTS;
	public static final MagicResistance MAGIC_RESISTANCE;
	public static final Tooltips TOOLTIPS;
	public static final Privacy PRIVACY;
	public static final BrainFreezeConfig BRAIN_FREEZE;

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		// [forgiving_void], [leaf_decay], [raids], [worldgen], [named_mobs] and the [perf] mob cap keys:
		// creativeworldgen-common.toml (Creative Worldgen) [botania]: creativemagic-common.toml (Creative Magic)
		// [shrapnel], [surface], [vein_undo]: creativeengineering-common.toml (Creative Engineering)
		CHANGELOG = new Changelog(builder);
		// [loading_tips]: creativecommon-client.toml (CommonClientConfig, per client)
		DAMAGE_TAGS = new DamageTagConfig(builder);
		// [armor_overflow]: creativeequipment-common.toml; [jei], [gui_title], [toasts], [tooltips], [coaster],
		// [wthit]: creativecompat-common.toml (interface carve) [leaf_piles], [moss], [vine_shearing], [campfire],
		// [monster_box], [trash_slot]: creativecompat-common.toml (Creative Compat)
		PERF = new Perf(builder);
		// [seasons], [crop_seasons], [time]: creativecalendar-common.toml (Creative Calendar)
		// [wandering_trader]: creativeloot-common.toml (Creative Loot); [bees]: creativedelight-common.toml (Creative
		// Delight). Wave 2 carve (plan_wave2_publish.md W2-2, W2-8); each carries the player's values over once.
		ADVANCEMENTS = new Advancements(builder);
		// [recipe_book]: recipe unlocks switch (publish_readiness.md PR-CC-02)
		RECIPE_BOOK = new RecipeBook(builder);
		CREATIVE_MENU = new CreativeMenu(builder);
		MOD_NAMES = new ModNames(builder);
		// [effects], [magic_resistance]: taken over from a dependent jar (plan_cb_polish.md 2), which carries the
		// player's values over (EFFECT_SECTIONS)
		EFFECTS = new Effects(builder);
		MAGIC_RESISTANCE = new MagicResistance(builder);
		// [melting]: creativecompat-common.toml (Creative Compat, wave 2 carve; Compat carries the values over)
		// [tooltips] paletteDemo: the color palette test item (plan_rules_fixes.md section 9, V-45)
		TOOLTIPS = new Tooltips(builder);
		// [privacy]: unsigned chat on a dedicated server (feature_writeup/privacy.md 4.2)
		PRIVACY = new Privacy(builder);
		// [brain_freeze]: cold foods eaten close together (effect/BrainFreeze, TODO sweets #9, feature_writeup/smoothies.md 3)
		BRAIN_FREEZE = new BrainFreezeConfig(builder);
		// [dragon_gear], [air_brake], [stepstool], [crafting], [armor_enchantability], [shields], [bonemeal_charm],
		// [bottled_air], [backpackSkins]: creativeequipment-common.toml (Creative Equipment)
		COMMON_SPEC = builder.build();
	}

	/**
	 * Throttles for third-party mod code that the server profile (feature_writeup/perf.md section 7) found
	 * hot. Each one is a mixin in {@code mixin/} that skips or spaces out work the mod does every tick;
	 * the comments say what is traded away. Read live, no rejoin needed.
	 */
	public static final class Perf {
		public final ForgeConfigSpec.BooleanValue gravityLimitDimensions;
		public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> gravityDimensions;
		public final ForgeConfigSpec.IntValue mountEffectIntervalTicks;
		public final ForgeConfigSpec.BooleanValue idleRandomTicks;
		public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> idleRandomTickRules;

		private Perf(ForgeConfigSpec.Builder b) {
			b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
					.push("perf");

			// troughMissCooldownTicks: creativedelight-common.toml [perf] (Creative Delight)

			// seasonsMeltIntervalTicks: creativecalendar-common.toml [perf] (Creative Calendar)

			// mobCapLevels, mobCapForceLevel, mobCapLevel1/2Chunks, mobCapHysteresisChunks, mobCapLevel1/2Monster,
			// mobCapScaledCategories, spawnChunksLevel1/2, spawnBandLevel1/2, spawnBandCategories:
			// creativeworldgen-common.toml [perf] (Creative Worldgen)

			gravityLimitDimensions = b.comment(
					"Create: Gravity runs its biome lookup and gravity-attribute update for every living entity in",
					"every dimension on every tick. On: only entities in the dimensions below run it (entities that",
					"still carry the low-gravity modifier from one run it once more to drop it). Off: the mod's own",
					"behaviour."
			).define("gravityLimitDimensions", true);
			gravityDimensions = b.comment(
					"Dimensions Create: Gravity is allowed to run in. The pack's biome_configs lists only",
					"#minecraft:is_end, so the End is enough. Add a dimension here if you add a gravity biome",
					"outside the End."
			).defineList("gravityDimensions", java.util.List.of("minecraft:the_end"), o -> o instanceof String);

			mountEffectIntervalTicks = b.comment(
					"Ticks between passes that apply a worn horse armor's effects (dragon horse armor: Regeneration).",
					"The pass walks every entity in the level, which cost too much every tick. Effects last",
					"this + 40 ticks, so they stay on. A newly equipped armor starts working within one interval."
			).defineInRange("mountEffectIntervalTicks", 100, 1, 1200);

			// feature_writeup/random_tick_audit.md, perf/IdleRandomTicks
			idleRandomTicks = b.comment(
					"Block states that can no longer change on a random tick (full-grown colonies, waxed lanterns,",
					"Floral Enchantment flower bushes) stop asking for random ticks, so chunk sections holding only",
					"such blocks are skipped. Off: every block keeps its mod's own setting. Restart to apply."
			).define("idleRandomTicks", true);
			idleRandomTickRules = b.comment(
					"Which states stop. One line per rule: '<block id> [<property>=<value> | <property>!=<value> ...]",
					"[+wake_above]'. All conditions of a line must hold; several lines for one block are alternatives;",
					"no condition stops every state. 'max' / 'min' is the highest / lowest value of a number property.",
					"+wake_above: a stopped block runs its own random tick a few more times when a block is placed",
					"right above it (a grown tomato vine still climbs a rope placed later). Lines for blocks that are",
					"not installed are ignored. Restart to apply."
			).defineList("idleRandomTickRules", com.SaintAlfonzo.creativecommon.perf.IdleTickRules.defaults(),
					o -> o instanceof String);

			b.pop(); // perf
		}
	}


	// Season types ([seasons]), crop seasons ([crop_seasons]) and the [time] master: Creative Calendar's
	// CalendarConfig (creativecalendar-common.toml).

	/** Player changelog, "What's New" ({@code changelog/} package, feature_writeup/changelog.md). */
	public static final class Changelog {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.BooleanValue titleButton;
		public final ForgeConfigSpec.BooleanValue pauseButton;
		public final ForgeConfigSpec.BooleanValue updatePopup;

		private Changelog(ForgeConfigSpec.Builder b) {
			b.comment("Player changelog, \"What's New\" (client side). The notes are the pack's",
					"assets/creativecommon/changelog/<version>.md files. Takes effect the next time a menu opens."
			).push("changelog");

			enabled = b.comment("Master switch: off removes the book buttons and the update popup.")
					.define("enabled", true);
			titleButton = b.comment("Book button on the title screen, right of Singleplayer.")
					.define("titleButton", true);
			pauseButton = b.comment("Book button on the pause menu, right of Back to Game.")
					.define("pauseButton", true);
			updatePopup = b.comment(
					"Speech bubble beside the book button while the newest version's notes are unread. Opening the",
					"changelog once acknowledges it (config/creativecommon/changelog_seen.txt)."
			).define("updatePopup", true);

			b.pop(); // changelog
		}
	}

	/** Debug knob for the damage-tag layer ({@code damage/} package). */
	public static final class DamageTagConfig {
		public final ForgeConfigSpec.BooleanValue logSeenSources;

		private DamageTagConfig(ForgeConfigSpec.Builder b) {
			b.comment(
					"Damage tags classify every hit (fire, magic, electric, ...) so shields - and later armor -",
					"can treat kinds of damage differently. Every tag is a data/creativecommon/damage_tags/",
					"<name>.json file, merged across datapacks and reloaded by /reload: most list message ids",
					"directly (fire.json, magic.json, ... - these mirror vanilla's own damage flags, but as a",
					"visible, editable file instead of a hidden check), and a couple are pure META tags that",
					"only #include other tags (elemental.json = fire+cold+electric+chemical; physical.json =",
					"melee+machinery+piercing+firearm+explosion+projectile). Shields read only these tags -",
					"never a raw damage flag - so reclassifying a source, or moving a whole kind of damage",
					"between shield columns, is always a data edit here, never Java."
			).push("damage_tags");

			logSeenSources = b.comment(
					"Discovery aid. When true, the first time each distinct damage source (message id + flags)",
					"hurts anything on the server it is logged once as '[damage-tags] seen msgId=...'.",
					"'tags=NONE' means no damage_tags file claims that source yet. Off by default.",
					"Live: takes effect immediately."
			).define("logSeenSources", false);

			b.pop(); // damage_tags
		}
	}

	/** {@code mixin/ServerAdvancementManagerMixin}: the pack has no advancements at all (owner). */
	public static final class Advancements {
		public final ForgeConfigSpec.BooleanValue enabled;

		private Advancements(ForgeConfigSpec.Builder b) {
			b.comment("Advancements (vanilla, every mod, every datapack).").push("advancements");
			enabled = b.comment(
					"false: none are loaded, so none show, toast or give rewards (the pack's setting). Default true (vanilla).",
					"Read when data loads: a change applies on the next /reload or world start."
			).define("enabled", true);
			b.pop();
		}
	}

	/** {@code mixin/NoRecipeUnlockMixin}: recipe unlocks off (the pack's setting, design rule DR-C14). */
	public static final class RecipeBook {
		public final ForgeConfigSpec.BooleanValue unlocks;

		private RecipeBook(ForgeConfigSpec.Builder b) {
			b.comment("Recipe book (server side).").push("recipe_book");
			unlocks = b.comment(
					"false: no recipe is ever unlocked (crafting, furnaces, the knowledge book, advancement rewards,",
					"/recipe give, other mods), so the recipe book gains no entry and no \"new recipe\" toast shows.",
					"Entries already in a save stay. Default true (vanilla). Read live."
			).define("unlocks", true);
			b.pop();
		}
	}

	/** Pack tooltips that Common's config switches; read by {@code kubejs/client_scripts/tooltips.js} through NCH.cfg. */
	public static final class Tooltips {
		public final ForgeConfigSpec.BooleanValue paletteDemo;

		private Tooltips(ForgeConfigSpec.Builder b) {
			b.comment("Item tooltips (client side).").push("tooltips");
			paletteDemo = b.comment(
					"true: White Wool and White Concrete show the color palette test tooltips (every tooltip role and",
					"text color). Default false. Read when the client scripts load: a change applies after a client restart."
			).define("paletteDemo", false);
			b.pop();
		}
	}

	/** {@code mixin/CreativeMenuDisableMixin}: creative players get the survival inventory (client side). */
	public static final class CreativeMenu {
		public final ForgeConfigSpec.BooleanValue disabled;
		public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> orphanTabItems;

		private CreativeMenu(ForgeConfigSpec.Builder b) {
			b.comment("Creative inventory (client side).").push("creative_menu");
			disabled = b.comment(
					"true: creative players open the survival inventory (2x2 crafting grid) instead of the creative tabs,",
					"which skips the creative search cache build (a multi-second freeze on the first E press with many",
					"mods); JEI's cheat mode covers item browsing. Default false (vanilla); the pack sets true.",
					"Takes effect the next time the inventory opens."
			).define("disabled", false);
			orphanTabItems = b.comment(
					"Items that have no creative tab (so creative mode, its search and JEI never list them) but should be",
					"listed. Each goes into the tab most other items of its mod use (vanilla Food or Miscellaneous when the",
					"mod has none). Only items listed here change; an orphan left out stays hidden on purpose. The default",
					"is the Abnormals Delight compat foods that have pack recipes while their parent mods are absent.",
					"/dumpnotab lists every orphan with its recipe and loot sources. Takes effect on the next game start."
			).defineList("orphanTabItems", com.SaintAlfonzo.creativecommon.util.OrphanTabs.DEFAULTS, o -> o instanceof String);
			b.pop();
		}
	}

	/** {@code mixin/DedicatedServerUnsignedChatMixin}: chat signing on a dedicated server (feature_writeup/privacy.md 4.2). */
	public static final class Privacy {
		public final ForgeConfigSpec.BooleanValue acceptUnsignedChat;

		private Privacy(ForgeConfigSpec.Builder b) {
			b.comment("Chat signing (dedicated server).").push("privacy");
			acceptUnsignedChat = b.comment(
					"true: the server lets players join without a chat signing key, whatever enforce-secure-profile says in",
					"server.properties. Needed for the pack's clients, which hold no key ([privacy] blockMojangReporting in",
					"creativecommon-client.toml). false: server.properties decides. Read at each login."
			).define("acceptUnsignedChat", true);
			b.pop(); // privacy
		}
	}

	/** Mod display names ({@code util.ModDisplayNames}): what JEI, WTHIT and the Mods screen call a mod. */
	public static final class ModNames {
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;

		private ModNames(ForgeConfigSpec.Builder b) {
			b.comment("Mod display names (client side: JEI's mod line and @ search, WTHIT, the Mods screen).").push("mod_names");

			overrides = b.comment(
					"\"modid=Name\" entries replacing the name from the mod's mods.toml. Applied at startup and when this",
					"file is saved; JEI and WTHIT keep the name they read first, so a change shows after a restart.",
					"Removing an entry gives the mod its own name back. The mod id stays the same (JEI @<modid> still works).",
					"Empty by default; a pack lists its own names here (the Creative Engineering pack's list moved to its",
					"config 2026-11-07, creative_engineering.md 7.1)."
			).defineList("overrides", List.of(
			), o -> o instanceof String str && str.indexOf('=') > 0);

			b.pop(); // mod_names
		}
	}

	/** Mob effect category overrides ({@code effect.EffectCategories}, {@code mixin/MobEffectCategoryMixin}; from Creative Brewing's file, plan_cb_polish.md 2). */
	public static final class Effects {
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> categoryOverrides;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> forceHudIcon;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> forceHudIconItems;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> remap;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> removed;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> levelCaps;

		private Effects(ForgeConfigSpec.Builder b) {
			b.comment("Mob effects (client and server).").push("effects");
			categoryOverrides = b.comment(
					"Category overrides, \"namespace:effect=CATEGORY\" with CATEGORY one of BENEFICIAL, HARMFUL, NEUTRAL.",
					"The category colors the effect's name in potion, food and gear tooltips (HARMFUL red, the other",
					"two blue), and AppleSkin, Quark's saturation word and isBeneficial() go by it. Empty by default",
					"(feature_writeup/effects.md 2.4). Read live."
			).defineList("categoryOverrides", List.of(
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
					"(global.foodEffectSwaps). Empty by default. Server side, read live."
			).defineList("remap", List.of(
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			removed = b.comment(
					"Effects that are never applied (LivingEntity#addEffect refuses them). Their brews are removed",
					"by KubeJS (the potion scripts). Server side, read live."
			).defineList("removed", List.of(
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			levelCaps = b.comment(
					"Highest level an effect can have, \"namespace:effect=LEVEL\" (1 = level I). A higher level from any",
					"source (food, drink, wine, potion, mob, command) is lowered to it when applied, and every tooltip",
					"and the effect list show the capped level. The defaults are the effects that do the same at every",
					"level (feature_writeup/effects.md 2.1, Leveled N). Client and server, read live."
			).defineList("levelCaps", List.of(
					"minecraft:nausea=1",
					"minecraft:fire_resistance=1",
					"minecraft:water_breathing=1",
					"minecraft:invisibility=1",
					"minecraft:blindness=1",
					"minecraft:night_vision=1",
					"minecraft:glowing=1",
					"minecraft:slow_falling=1",
					"minecraft:dolphins_grace=1",
					"minecraft:darkness=1",
					"alexsmobs:bug_pheromones=1",
					"alexsmobs:clinging=1",
					"alexsmobs:earthquake=1",
					"alexsmobs:fear=1",
					"alexsmobs:fleet_footed=1",
					"alexsmobs:lava_vision=1",
					"alexsmobs:oiled=1",
					"alexsmobs:poison_resistance=1",
					"alexsmobs:power_down=1",
					"alexsmobs:sunbird_blessing=1",
					"alexsmobs:sunbird_curse=1",
					"alexsmobs:tigers_blessing=1",
					"better_beacons:patrol_nullifier=1",
					"better_beacons:phantom_bane=1",
					"botania:allure=1",
					"botania:bloodthirst=1",
					"botania:clear=1",
					"botania:emptiness=1",
					"botania:feather_feet=1",
					"botania:soul_cross=1",
					"brewinandchewin:intoxication=1",
					"buzzier_bees:sunny=1",
					"create_confectionery:rest=1",
					"create_confectionery:stimulation=1",
					"createaddition:shocking=1",
					"endersdelight:phasing=1",
					"farmersdelight:comfort=1",
					"farmersdelight:nourishment=1",
					"fruitsdelight:alienating=1",
					"fruitsdelight:appetizing=1",
					"fruitsdelight:astringent=1",
					"fruitsdelight:brightening=1",
					"fruitsdelight:chorus=1",
					"fruitsdelight:digesting=1",
					"fruitsdelight:disgusted=1",
					"fruitsdelight:leaf_piercing=1",
					"fruitsdelight:rage_aura=1",
					"fruitsdelight:recovering=1",
					"fruitsdelight:refreshing=1",
					"fruitsdelight:shrinking=1",
					"fruitsdelight:sliding=1",
					"fruitsdelight:suspicious_smell=1",
					"fruitsdelight:sweetening=1",
					"fungidelight:weak_stomach=1",
					"hearthandharvest:clarity=1",
					"hearthandharvest:pinned=1",
					"hostiledelight:collapsing=1",
					"hostiledelight:dioxide=1",
					"immersiveengineering:concrete_feet=1",
					"immersiveengineering:slippery=1",
					"immersiveengineering:stunned=1",
					"immersivepetroleum:anti_fire=1",
					"neapolitan:agility=1",
					"neapolitan:harmony=1",
					"neapolitan:slipping=1",
					"neapolitan:vanilla_scent=1",
					"oceanic_delight:instant_breath=1",
					"untitledduckmod:intimidation=1",
					"via_romana:travellers_fatigue=1",
					"vinery:climbing_effect=1",
					"vinery:lava_walker=1",
					"vinery:party_effect=1",
					"vinery:teleport=1",
					"vinery:trippy=1",
					"vinery:water_walker=1",
					"xaerominimap:no_cave_maps=1",
					"xaerominimap:no_cave_maps_harmful=1",
					"xaerominimap:no_entity_radar=1",
					"xaerominimap:no_entity_radar_harmful=1",
					"xaerominimap:no_minimap=1",
					"xaerominimap:no_minimap_harmful=1",
					"xaerominimap:no_waypoints=1",
					"xaerominimap:no_waypoints_harmful=1",
					"xaeroworldmap:no_cave_maps=1",
					"xaeroworldmap:no_cave_maps_harmful=1",
					"xaeroworldmap:no_world_map=1",
					"xaeroworldmap:no_world_map_harmful=1"
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[1-9][0-9]*"));
			b.pop(); // effects
		}
	}

	/** Dials for {@code effect/MagicResistanceEffect}'s (from Creative Brewing's file, plan_cb_polish.md 2) damage-reduction curve. */
	public static final class MagicResistance {
		public final ForgeConfigSpec.DoubleValue reductionPerLevel;
		public final ForgeConfigSpec.ConfigValue<String> negatedEffectTicks;

		private MagicResistance(ForgeConfigSpec.Builder b) {
			b.comment(
					"Tuning for creativecommon:magic_resistance's damage reduction against",
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

	/**
	 * Brain freeze ({@code effect/BrainFreeze}, TODO sweets #9, feature_writeup/smoothies.md section 3): every cold food
	 * ({@code #creativecommon:brain_freeze}) eaten within {@code windowSeconds} of the one before rolls; the n-th roll in
	 * a row hits at n x {@code chanceStep}. A hit gives the Brain Freeze effect for the n-th entry of {@code lengths}
	 * (the last entry from then on) and resets the count. Read live.
	 */
	public static final class BrainFreezeConfig {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue windowSeconds;
		public final ForgeConfigSpec.DoubleValue chanceStep;
		public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> lengths;

		private BrainFreezeConfig(ForgeConfigSpec.Builder b) {
			b.comment(
					"Brain freeze: cold foods (item tag creativecommon:brain_freeze: smoothies, ice cream, popsicles,",
					"shaved ice, cold milkshakes, ice cubes) eaten close together can freeze the player: the powdered",
					"snow overlay and freeze damage while the Brain Freeze effect lasts, and a blue line above the hotbar."
			).push("brain_freeze");

			enabled = b.comment("Roll for brain freeze at all.").define("enabled", true);

			windowSeconds = b.comment(
					"A cold food eaten within this many seconds of the previous cold food rolls; a longer gap starts",
					"the count again."
			).defineInRange("windowSeconds", 60, 1, 3600);

			chanceStep = b.comment(
					"Chance of the first roll in a row; each further roll in the window adds this much again",
					"(0.2: 20%, 40%, 60%, 80%, 100%)."
			).defineInRange("chanceStep", 0.2, 0.0, 1.0);

			lengths = b.comment(
					"Brain freeze length in seconds by the number of rolls up to and including the hit: the first",
					"entry for a hit on the first roll, the second for the second roll, and so on; the last entry",
					"covers every later roll. A hit resets the count."
			).defineList("lengths", List.of(5, 8, 11, 14, 20), o -> o instanceof Integer n && n > 0 && n <= 3600);

			b.pop(); // brain_freeze
		}
	}
}
