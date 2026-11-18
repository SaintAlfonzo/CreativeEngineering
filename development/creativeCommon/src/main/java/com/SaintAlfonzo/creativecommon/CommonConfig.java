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

	/** The toml tables of this file, carried over from creativeengineering-common.toml on the first boot after the swap. */
	public static final List<String> SECTIONS = List.of("changelog", "damage_tags", "perf", "wandering_trader", "advancements", "mod_names");

	/** Sections taken over from Creative Brewing's creativebrewing-common.toml (plan_cb_polish.md 2). */
	public static final List<String> EFFECT_SECTIONS = List.of("effects", "magic_resistance");

	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Changelog CHANGELOG;
	public static final DamageTagConfig DAMAGE_TAGS;
	public static final Perf PERF;
	public static final WanderingTrader WANDERING_TRADER;
	public static final Bees BEES;
	public static final Advancements ADVANCEMENTS;
	public static final CreativeMenu CREATIVE_MENU;
	public static final ModNames MOD_NAMES;
	public static final Effects EFFECTS;
	public static final MagicResistance MAGIC_RESISTANCE;
	public static final MeltingConfig MELTING;
	public static final Tooltips TOOLTIPS;

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
		WANDERING_TRADER = new WanderingTrader(builder);
		BEES = new Bees(builder);
		ADVANCEMENTS = new Advancements(builder);
		CREATIVE_MENU = new CreativeMenu(builder);
		MOD_NAMES = new ModNames(builder);
		// [effects], [magic_resistance]: from creativebrewing-common.toml (plan_cb_polish.md 2); the constructor
		// carries the player's values over (EFFECT_SECTIONS)
		EFFECTS = new Effects(builder);
		MAGIC_RESISTANCE = new MagicResistance(builder);
		// [melting]: new (melting/ package, feature_writeup/melting.md, plan_bugfix_1114.md 3.8)
		MELTING = new MeltingConfig(builder);
		// [tooltips] paletteDemo: the color palette test item (plan_rules_fixes.md section 9, V-45)
		TOOLTIPS = new Tooltips(builder);
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

	/**
	 * Dials for {@code trader.WanderingTraderTweaks} (Wandering Trader wait and offer counts, through
	 * {@code mixin.WanderingTraderSpawnerMixin} / {@code mixin.WanderingTraderTradesMixin}). The Wandering
	 * Winemaker's spawner is the Vinery port's own (vinery-common.toml [trader]). What the pools hold is
	 * set in KubeJS ({@code kubejs/server_scripts/economy/wandering_trades.js}).
	 */
	// Reserved for Creative Economy (creative_split.md 2.3): [wandering_trader] moves with the trader code.
	public static final class WanderingTrader {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue traderIntervalTicks;
		public final ForgeConfigSpec.IntValue commonTrades;
		public final ForgeConfigSpec.IntValue rareTrades;

		private WanderingTrader(ForgeConfigSpec.Builder b) {
			b.comment(
					"Wandering Trader spawning and offer counts (the Wandering Winemaker: vinery-common.toml [trader]).",
					"The spawner rolls once per interval, counted in server ticks (20 = 1 second) while the",
					"overworld runs with doMobSpawning on. Day length (Better Days), sleeping and /time do not",
					"change it. A roll is vanilla's: 25/50/75% chance (rising until a spawn), then 1 in 10, then",
					"a free spot near a random player; on average about 14 rolls per spawn.",
					"/gamerule doTraderSpawning false stops it and the Winemaker."
			).push("wandering_trader");

			enabled = b.comment("Off = vanilla Wandering Trader (24000-tick interval, 5 + 1 offers). Default off; the pack sets true.")
					.define("enabled", false);

			traderIntervalTicks = b.comment(
					"Ticks between Wandering Trader spawn rolls. Vanilla: 24000 (20 minutes). 96000 = 80 minutes",
					"(the pack's value). A change takes effect after the wait already counting down."
			).defineInRange("traderIntervalTicks", 24000, 1200, 100_000_000);

			commonTrades = b.comment(
					"Offers drawn from the common pool. Vanilla: 5 (the pack: 25). Each offer is a different listing; a pool",
					"smaller than this gives every listing once."
			).defineInRange("commonTrades", 5, 0, 200);

			rareTrades = b.comment("Offers drawn from the rare pool, all different items. Vanilla: 1 (the pack: 5).")
					.defineInRange("rareTrades", 1, 1, 50);

			b.pop(); // wandering_trader
		}
	}

	/** {@code mixin/BeehiveHoneycombMixin}: honeycomb from shearing a full hive or nest (farming_crops A9). */
	public static final class Bees {
		public final ForgeConfigSpec.IntValue honeycombMultiplier;

		private Bees(ForgeConfigSpec.Builder b) {
			b.comment("Bees.").push("bees");
			honeycombMultiplier = b.comment(
					"Multiplies the honeycomb a full hive or nest drops when sheared (by hand or by a dispenser).",
					"Vanilla drops 3, so 2 gives 6 (the pack's setting). 1 = vanilla. Read live."
			).defineInRange("honeycombMultiplier", 2, 1, 16);
			b.pop(); // bees
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

		private CreativeMenu(ForgeConfigSpec.Builder b) {
			b.comment("Creative inventory (client side).").push("creative_menu");
			disabled = b.comment(
					"true: creative players open the survival inventory (2x2 crafting grid) instead of the creative tabs,",
					"which skips the creative search cache build (a multi-second freeze on the first E press with many",
					"mods); JEI's cheat mode covers item browsing. Default false (vanilla); the pack sets true.",
					"Takes effect the next time the inventory opens."
			).define("disabled", false);
			b.pop();
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

		private Effects(ForgeConfigSpec.Builder b) {
			b.comment("Mob effects (client and server).").push("effects");
			categoryOverrides = b.comment(
					"Category overrides, \"namespace:effect=CATEGORY\" with CATEGORY one of BENEFICIAL, HARMFUL, NEUTRAL.",
					"The category colors the effect's name in potion, food and gear tooltips (HARMFUL red, the other",
					"two blue), and AppleSkin, Quark's saturation word and isBeneficial() go by it. The defaults are the",
					"effects whose own category showed the wrong color (feature_writeup/effects.md 2.4). Read live."
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
					"by KubeJS (Creative Brewing's potions.js). Server side, read live."
			).defineList("removed", List.of(
			), o -> o instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"));
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
	 * Melting ({@code melting/} package, feature_writeup/melting.md): salt, fire and lava melt ice and snow; packed and
	 * blue ice step down one tier at a low chance. Sources and targets are the {@code creativecommon:melting/*} block
	 * tags. Every value but {@code extraRandomTickBlocks} is read live.
	 */
	public static final class MeltingConfig {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue saltRange;
		public final ForgeConfigSpec.IntValue fireRange;
		public final ForgeConfigSpec.IntValue lavaRange;
		public final ForgeConfigSpec.IntValue slowLavaRange;
		public final ForgeConfigSpec.IntValue slowMagmaRange;
		public final ForgeConfigSpec.DoubleValue meltChance;
		public final ForgeConfigSpec.DoubleValue slowMeltChance;
		public final ForgeConfigSpec.BooleanValue placeWater;
		public final ForgeConfigSpec.BooleanValue vanillaLightMelting;
		public final ForgeConfigSpec.BooleanValue ultraWarmInstantMelt;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> slowSteps;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> extraRandomTickBlocks;
		public final ForgeConfigSpec.DoubleValue snowLayerMeltChance;
		public final ForgeConfigSpec.IntValue meltTickInterval;
		public final ForgeConfigSpec.IntValue magmaInstantRange;
		public final ForgeConfigSpec.BooleanValue magmaStopsFreezing;
		public final ForgeConfigSpec.IntValue saltMeltSnowLayers;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> saltProducts;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> meltProducts;
		public final ForgeConfigSpec.DoubleValue sluggishMeltFactor;
		public final ForgeConfigSpec.IntValue hotMeltTickInterval;
		public final ForgeConfigSpec.BooleanValue endContactOnly;
		public final ForgeConfigSpec.BooleanValue endNoFire;

		private MeltingConfig(ForgeConfigSpec.Builder b) {
			b.comment(
					"Melting: salt, fire and lava melt ice and snow (server side). Sources are the block tags",
					"creativecommon:melting/salt, /fire (a block with a lit property counts only while lit) and /lava (plus any",
					"block holding lava fluid); targets are /meltable (ice and frosted ice become water, snow loses a layer, frozen",
					"decor becomes its meltProducts row, the rest air) and /meltable_slow (packed and blue ice, IW cryosol and",
					"permafrost, one tier down; only lava and magma heat them, /lava_only ones only by touching lava). Salt melts",
					"only /saltable blocks. A pair is found by the target's random tick or by a source's own random tick",
					"(magma, lava, salt blocks); the target then gets a melt tick every meltTickInterval while a source stays in",
					"reach, rolling its chance each time. Ranges are spheres measured through any blocks: 1 = the 6 touching",
					"blocks, 0 = this source never melts that kind. A target looks past its 6 neighbors for fire and lava only",
					"when block light reaches it, so a fire behind a sealed wall melts nothing. feature_writeup/melting.md."
			).push("melting");

			enabled = b.comment("Master switch for salt, fire and lava melting. The Salt item click is The Salt's own and not covered.")
					.define("enabled", true);
			saltRange = b.comment("Salt: reach for every target (meltable and meltable_slow). 1 = touching. Above 1 every",
							"target scans a sphere on each roll (salt gives no light to gate it), so keep it at 1 on big glaciers.")
					.defineInRange("saltRange", 1, 0, 4);
			fireRange = b.comment("Fire, torches, lanterns, glowstone, magma: reach for meltable targets.")
					.defineInRange("fireRange", 3, 0, 8);
			lavaRange = b.comment("Lava: reach for meltable targets.")
					.defineInRange("lavaRange", 4, 0, 8);
			slowLavaRange = b.comment("Lava: reach for meltable_slow targets (packed ice, cryosol). Blue ice and permafrost",
							"(creativecommon:melting/lava_only) melt only touching lava, whatever this says. Other fire never melts them.")
					.defineInRange("slowLavaRange", 2, 0, 8);
			slowMagmaRange = b.comment("Magma (creativecommon:melting/magma): reach for meltable_slow targets that are not lava_only.")
					.defineInRange("slowMagmaRange", 2, 0, 8);
			meltChance = b.comment(
					"Chance per tick (its random tick and each melt tick) that a meltable target with a source in reach",
					"melts. Snow layers use snowLayerMeltChance."
			).defineInRange("meltChance", 0.4, 0.0, 1.0);
			slowMeltChance = b.comment("The same for meltable_slow targets, one tier per success.")
					.defineInRange("slowMeltChance", 0.05, 0.0, 1.0);
			placeWater = b.comment("Melted ice (#minecraft:ice) leaves a water source; false leaves air. Ultra-warm dimensions",
							"(the Nether) always leave air.")
					.define("placeWater", true);
			vanillaLightMelting = b.comment(
					"Vanilla melts ice above block light 11 and snow layers above 11, from any light (sea lanterns,",
					"shroomlights, redstone lamps). false: only the sources above melt anything (torches, lanterns and",
					"glowstone through the fire rule). true: vanilla's light melting runs as well. Serene Seasons' seasonal",
					"melt and Immersive Weathering's ice melt are their own and not touched by this."
			).define("vanillaLightMelting", false);
			ultraWarmInstantMelt = b.comment(
					"Blocks in creativecommon:melting/ultrawarm_instant (snow layers, snow blocks, powder snow) become air the",
					"moment they are placed in an ultra-warm dimension (the Nether): by hand, dispenser, /setblock or a mod."
			).define("ultraWarmInstantMelt", true);
			slowSteps = b.comment(
					"\"from=to\" block ids: what a meltable_slow target becomes when it melts. A meltable_slow block with no",
					"row here melts fully, like a meltable one."
			).defineList("slowSteps", List.of(
					"minecraft:blue_ice=minecraft:packed_ice",
					"minecraft:packed_ice=minecraft:ice",
					"immersive_weathering:permafrost=immersive_weathering:cryosol",
					"immersive_weathering:cryosol=minecraft:dirt",
					"architects_palette:polished_packed_ice=minecraft:ice",
					"architects_palette:chiseled_packed_ice=minecraft:ice",
					"architects_palette:packed_ice_pillar=minecraft:ice"
			), o -> o instanceof String str && str.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			extraRandomTickBlocks = b.comment(
					"Blocks that get random ticks so they can find a fire or lava by themselves (vanilla gives none to these).",
					"Ice, frosted ice, snow layers and Immersive Weathering's snowy and frosty blocks tick already. A target left",
					"out (packed and blue ice, cryosol, permafrost) is found only by a source's own tick (magma, lava, salt",
					"blocks). Sources listed here (Molten Nether Bricks) scan from their own tick like magma. Read once at",
					"startup: a change needs a restart."
			).defineList("extraRandomTickBlocks", List.of(
					"minecraft:snow_block",
					"minecraft:powder_snow",
					"immersive_weathering:thin_ice",
					"immersive_weathering:snow_bricks",
					"immersive_weathering:snow_brick_slab",
					"immersive_weathering:snow_brick_stairs",
					"immersive_weathering:snow_brick_wall",
					"immersive_weathering:snow_brick_vertical_slab",
					"architects_palette:coarse_snow",
					"architects_palette:molten_nether_bricks"
			), o -> o instanceof String str && str.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			snowLayerMeltChance = b.comment("Chance per tick that a snow layer with a source in reach loses one layer.")
					.defineInRange("snowLayerMeltChance", 0.8, 0.0, 1.0);
			meltTickInterval = b.comment(
					"Ticks between melt ticks of a target with a source in reach (75 to 125 percent of this, vanilla scheduled",
					"ticks saved with the chunk). 100 = 5 s. 0 = off: targets melt only on random ticks, as before 2026-11-17."
			).defineInRange("meltTickInterval", 100, 0, 12000);
			magmaInstantRange = b.comment(
					"Snow (layers, blocks, powder snow) within this reach of a block in creativecommon:melting/magma melts at",
					"once: when placed, on magma's own tick, or on its melt tick. 0 = off."
			).defineInRange("magmaInstantRange", 2, 0, 4);
			magmaStopsFreezing = b.comment(
					"No snow forms from snowfall and no water freezes into ice within fireRange of a creativecommon:melting/magma",
					"block (the weather's snow and ice placement, server only; world generation is not checked)."
			).define("magmaStopsFreezing", true);
			saltMeltSnowLayers = b.comment(
					"Snow layers one Salt Melt use or one salt dispense removes; a Snow Block counts as 8 layers. A salt block",
					"melting by its tick removes one."
			).defineInRange("saltMeltSnowLayers", 3, 1, 8);
			saltProducts = b.comment(
					"\"from=to\" block ids: what a saltable block becomes when salt melts it (shared properties copied). Not",
					"listed: ice (#minecraft:ice) becomes water as placeWater says, anything else air. Snow loses layers instead."
			).defineList("saltProducts", List.of(
					"minecraft:packed_ice=minecraft:ice",
					"immersive_weathering:cryosol=minecraft:dirt"
			), o -> o instanceof String str && str.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			meltProducts = b.comment(
					"\"from=to\" block ids: what a meltable target becomes when heat melts it (and salt, when saltProducts has",
					"no row), every property the two share copied by name (facing, half, slab type, wall sides, waterlogged).",
					"minecraft:water becomes air in ultra-warm dimensions or with placeWater off. Not listed: water for",
					"#minecraft:ice, else air. A row whose product is missing (mod not loaded) is skipped."
			).defineList("meltProducts", List.of(
					"immersive_weathering:snowy_stone=minecraft:stone",
					"immersive_weathering:snowy_stone_slab=minecraft:stone_slab",
					"immersive_weathering:snowy_stone_stairs=minecraft:stone_stairs",
					"immersive_weathering:snowy_stone_wall=immersive_weathering:stone_wall",
					"immersive_weathering:snowy_stone_vertical_slab=quark:stone_vertical_slab",
					"immersive_weathering:snowy_cobblestone=minecraft:cobblestone",
					"immersive_weathering:snowy_cobblestone_slab=minecraft:cobblestone_slab",
					"immersive_weathering:snowy_cobblestone_stairs=minecraft:cobblestone_stairs",
					"immersive_weathering:snowy_cobblestone_wall=minecraft:cobblestone_wall",
					"immersive_weathering:snowy_cobblestone_vertical_slab=quark:cobblestone_vertical_slab",
					"immersive_weathering:snowy_stone_bricks=minecraft:stone_bricks",
					"immersive_weathering:snowy_stone_brick_slab=minecraft:stone_brick_slab",
					"immersive_weathering:snowy_stone_brick_stairs=minecraft:stone_brick_stairs",
					"immersive_weathering:snowy_stone_brick_wall=minecraft:stone_brick_wall",
					"immersive_weathering:snowy_stone_brick_vertical_slab=quark:stone_brick_vertical_slab",
					"immersive_weathering:snowy_chiseled_stone_bricks=minecraft:chiseled_stone_bricks",
					"immersive_weathering:frosty_glass=minecraft:glass",
					"immersive_weathering:frosty_glass_pane=minecraft:glass_pane",
					"immersive_weathering:frosty_grass=minecraft:grass",
					"immersive_weathering:frosty_fern=minecraft:fern",
					"immersive_weathering:thin_ice=minecraft:water",
					"architects_palette:coarse_snow=minecraft:gravel"
			), o -> o instanceof String str && str.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+=[a-z0-9_.-]+:[a-z0-9_/.-]+"));
			sluggishMeltFactor = b.comment("Chance multiplier for creativecommon:melting/sluggish targets (snow bricks).")
					.defineInRange("sluggishMeltFactor", 0.25, 0.0, 1.0);
			hotMeltTickInterval = b.comment(
					"Hot places: in an ultra-warm dimension (the Nether) every target but the lava_only ones (blue ice,",
					"permafrost), and in a creativecommon:melting/hot biome (arid, dry and tropical) ice, snow and frozen decor, take a",
					"melt tick this often with no source needed, rolling meltChance each time (the slow tier too). 0 = off."
			).defineInRange("hotMeltTickInterval", 20, 0, 12000);
			endContactOnly = b.comment(
					"The End: every source (salt, fire, magma, lava) melts only the blocks touching it; no reach, no magma",
					"instant zone past the touching blocks.")
					.define("endContactOnly", true);
			endNoFire = b.comment(
					"No minecraft:fire in the End: any placement of it is refused (flint and steel, fire charges, lava, End",
					"crystals, commands) and fire already there goes out on its next tick. Soul fire is not affected.")
					.define("endNoFire", true);

			b.pop(); // melting
		}
	}
}
