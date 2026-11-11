package com.SaintAlfonzo.creativeworldgen;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Creative Worldgen's COMMON config, {@code config/creativeworldgen-common.toml}. The sections moved out of patcherMod's
 * creativeengineering-common.toml on 2026-11-02 (creative_split.md 7.6, wave 5) with keys, comments and defaults
 * unchanged: {@code [forgiving_void]}, {@code [leaf_decay]}, {@code [raids]}, the mob cap and spawn area keys of
 * {@code [perf]}, {@code [worldgen]} with every subtable, and {@code [named_mobs]}. The first boot with Worldgen copies a
 * player's values over ({@code swap.ConfigRename.carrySections}, called from {@link CreativeWorldgen}). The static field
 * names are the pre-carve CEConfig's, so {@code global.NCH.cfg(..., 'WORLDGEN')} finds them once the scripts register this class.
 */
public final class CWGConfig {

    private CWGConfig() {}

    public static final ForgeConfigSpec SPEC;
    public static final ForgivingVoid FORGIVING_VOID;
    public static final LeafDecay LEAF_DECAY;
    public static final Raids RAIDS;
    public static final Perf PERF;
    public static final Worldgen WORLDGEN;
    public static final NamedMobs NAMED_MOBS;
    public static final WiseOak WISE_OAK;

    /** The toml tables carried over from creativeengineering-common.toml ({@code [perf]} keeps only its mob cap keys). */
    public static final List<String> SECTIONS =
            List.of("forgiving_void", "leaf_decay", "raids", "perf", "worldgen", "named_mobs");

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        FORGIVING_VOID = new ForgivingVoid(builder);
        LEAF_DECAY = new LeafDecay(builder);
        RAIDS = new Raids(builder);
        PERF = new Perf(builder);
        WORLDGEN = new Worldgen(builder);
        NAMED_MOBS = new NamedMobs(builder);
        WISE_OAK = new WiseOak(builder);
        SPEC = builder.build();
    }

    public enum PullDirection { TOWARD, AWAY }

    /** Timers for {@code mixin/wiseoak/WiseOakTileMixin} (feature_writeup/wise_oak.md 4.3). */
    public static final class WiseOak {
        public final ForgeConfigSpec.IntValue sleepDurationSeconds;
        public final ForgeConfigSpec.IntValue angerDurationSeconds;
        public final ForgeConfigSpec.IntValue angrySleepDurationSeconds;
        public final ForgeConfigSpec.DoubleValue giftChancePercent;
        public final ForgeConfigSpec.IntValue giftTrustThreshold;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> giftSaplings;
        public final ForgeConfigSpec.IntValue giftSaplingCount;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> giftFruitSaplings;
        public final ForgeConfigSpec.IntValue giftFruitSaplingCount;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> giftFruit;
        public final ForgeConfigSpec.IntValue giftFruitCount;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> giftLeaves;
        public final ForgeConfigSpec.IntValue giftLeavesCount;

        private WiseOak(ForgeConfigSpec.Builder b) {
            b.comment("Wise Oak sleep and anger timers, in real seconds. Server side.").push("wise_oak");
            sleepDurationSeconds = b.comment("How long a Wise Oak sleeps after it gets tired of talking.")
                    .defineInRange("sleepDurationSeconds", 300, 0, 86400);
            angerDurationSeconds = b.comment("How long a Wise Oak stays angry.")
                    .defineInRange("angerDurationSeconds", 30, 0, 86400);
            angrySleepDurationSeconds = b.comment("How long a Wise Oak sleeps once its anger ends.")
                    .defineInRange("angrySleepDurationSeconds", 600, 0, 86400);

            b.comment(
                    "Gifts: after a conversation with an awake Wise Oak, a player at or above the trust threshold has a",
                    "chance of a drop. One of the four lists is picked evenly, then one item in it. An empty list is left",
                    "out of the draw; an unknown id is skipped with a warning in the log."
            ).push("gifts");
            giftChancePercent = b.comment("Chance of a gift per conversation, in percent. 0 turns gifts off.")
                    .defineInRange("chancePercent", 5.0D, 0.0D, 100.0D);
            giftTrustThreshold = b.comment("Trust needed for a gift (0 to 100, +5 per conversation).")
                    .defineInRange("trustThreshold", 75, 0, 100);
            giftSaplings = b.comment("Saplings of Overworld trees.")
                    .defineListAllowEmpty(List.of("saplings"), () -> List.of(
                    "minecraft:oak_sapling", "minecraft:spruce_sapling", "minecraft:birch_sapling", "minecraft:jungle_sapling",
                    "minecraft:acacia_sapling", "minecraft:dark_oak_sapling", "minecraft:mangrove_propagule", "minecraft:azalea",
                    "minecraft:flowering_azalea", "myrtrees:rubberwood_sapling", "quark:ancient_sapling", "quark:blue_blossom_sapling",
                    "quark:lavender_blossom_sapling", "quark:orange_blossom_sapling", "quark:pink_blossom_sapling", "quark:red_blossom_sapling",
                    "quark:yellow_blossom_sapling"), o -> o instanceof String);
            giftSaplingCount = b.comment("Items dropped from the saplings list.").defineInRange("saplingCount", 1, 1, 64);
            giftFruitSaplings = b.comment("Fruit tree saplings.")
                    .defineListAllowEmpty(List.of("fruitSaplings"), () -> List.of(
                    "fruitsdelight:bayberry_sapling", "fruitsdelight:durian_sapling", "fruitsdelight:fig_sapling", "fruitsdelight:hawberry_sapling",
                    "fruitsdelight:kiwi_sapling", "fruitsdelight:lychee_sapling", "fruitsdelight:mango_sapling", "fruitsdelight:mangosteen_sapling",
                    "fruitsdelight:orange_sapling", "fruitsdelight:peach_sapling", "fruitsdelight:pear_sapling", "fruitsdelight:persimmon_sapling",
                    "vinery:apple_tree_sapling", "vinery:cherry_sapling"), o -> o instanceof String);
            giftFruitSaplingCount = b.comment("Items dropped from the fruitSaplings list.").defineInRange("fruitSaplingCount", 1, 1, 64);
            giftFruit = b.comment("Fruit of trees that grow in the Overworld.")
                    .defineListAllowEmpty(List.of("fruit"), () -> List.of(
                    "minecraft:apple", "vinery:cherry", "fruitsdelight:bayberry",
                    "fruitsdelight:durian", "fruitsdelight:fig", "fruitsdelight:hawberry", "fruitsdelight:kiwi",
                    "fruitsdelight:lychee", "fruitsdelight:mango", "fruitsdelight:mangosteen", "fruitsdelight:orange",
                    "fruitsdelight:peach", "fruitsdelight:pear", "fruitsdelight:persimmon"), o -> o instanceof String);
            giftFruitCount = b.comment("Items dropped from the fruit list.").defineInRange("fruitCount", 4, 1, 64);
            giftLeaves = b.comment("Leaves of the trees in the two sapling lists.")
                    .defineListAllowEmpty(List.of("leaves"), () -> List.of(
                    "minecraft:oak_leaves", "minecraft:spruce_leaves", "minecraft:birch_leaves", "minecraft:jungle_leaves",
                    "minecraft:acacia_leaves", "minecraft:dark_oak_leaves", "minecraft:mangrove_leaves", "minecraft:azalea_leaves",
                    "minecraft:flowering_azalea_leaves", "myrtrees:rubberwood_leaves", "quark:blue_blossom_leaves",
                    "quark:lavender_blossom_leaves", "quark:orange_blossom_leaves", "quark:pink_blossom_leaves", "quark:red_blossom_leaves",
                    "quark:yellow_blossom_leaves", "fruitsdelight:bayberry_leaves", "fruitsdelight:durian_leaves", "fruitsdelight:fig_leaves",
                    "fruitsdelight:hawberry_leaves", "fruitsdelight:kiwi_leaves", "fruitsdelight:lychee_leaves", "fruitsdelight:mango_leaves",
                    "fruitsdelight:mangosteen_leaves", "fruitsdelight:orange_leaves", "fruitsdelight:peach_leaves", "fruitsdelight:pear_leaves",
                    "fruitsdelight:persimmon_leaves", "vinery:apple_leaves", "vinery:cherry_leaves"), o -> o instanceof String);
            giftLeavesCount = b.comment("Items dropped from the leaves list.").defineInRange("leavesCount", 16, 1, 64);
            b.pop();
            b.pop();
        }
    }

    /** Dials for {@code mixin/ForgivingVoidMixin}'s origin-pull behaviour. */
    public static final class ForgivingVoid {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<PullDirection> direction;
        public final ForgeConfigSpec.DoubleValue minDistance;
        public final ForgeConfigSpec.DoubleValue maxDistance;
        public final ForgeConfigSpec.DoubleValue defaultAnchorX;
        public final ForgeConfigSpec.DoubleValue defaultAnchorZ;
        public final ForgeConfigSpec.BooleanValue overworldOverride;
        public final ForgeConfigSpec.DoubleValue overworldAnchorX;
        public final ForgeConfigSpec.DoubleValue overworldAnchorZ;
        public final ForgeConfigSpec.BooleanValue netherOverride;
        public final ForgeConfigSpec.DoubleValue netherAnchorX;
        public final ForgeConfigSpec.DoubleValue netherAnchorZ;
        public final ForgeConfigSpec.BooleanValue endOverride;
        public final ForgeConfigSpec.DoubleValue endAnchorX;
        public final ForgeConfigSpec.DoubleValue endAnchorZ;

        private ForgivingVoid(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Forgiving Void catches players who fall out of the world; these dials control",
                    "how the landing spot is nudged each bounce so a void-fall loop can't run forever."
            ).push("forgiving_void");

            enabled = b.comment("Master on/off switch. When false, the landing spot is left untouched.")
                    .define("enabled", true);

            direction = b.comment(
                    "TOWARD pulls the landing spot closer to the anchor each bounce (default - kills fall loops).",
                    "AWAY pushes it further from the anchor instead."
            ).defineEnum("direction", PullDirection.TOWARD);

            minDistance = b.comment("Minimum blocks moved per bounce.")
                    .defineInRange("minDistance", 20.0, 0.0, 1.0E7);
            maxDistance = b.comment("Maximum blocks moved per bounce. Set equal to minDistance for no randomness.")
                    .defineInRange("maxDistance", 25.0, 0.0, 1.0E7);

            defaultAnchorX = b.comment("Anchor X for any dimension without an override below. Defaults to world center.")
                    .defineInRange("defaultAnchorX", 0.0, -3.0E7, 3.0E7);
            defaultAnchorZ = b.comment("Anchor Z for any dimension without an override below. Defaults to world center.")
                    .defineInRange("defaultAnchorZ", 0.0, -3.0E7, 3.0E7);

            b.push("overworldOverride");
            overworldOverride = b.comment("Use a separate anchor for the Overworld instead of the default anchor above.")
                    .define("enabled", false);
            overworldAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            overworldAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.push("netherOverride");
            netherOverride = b.comment("Use a separate anchor for the Nether instead of the default anchor above.")
                    .define("enabled", false);
            netherAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            netherAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.push("endOverride");
            endOverride = b.comment("Use a separate anchor for the End instead of the default anchor above.")
                    .define("enabled", false);
            endAnchorX = b.defineInRange("anchorX", 0.0, -3.0E7, 3.0E7);
            endAnchorZ = b.defineInRange("anchorZ", 0.0, -3.0E7, 3.0E7);
            b.pop();

            b.pop(); // forgiving_void
        }
    }

    /**
     * Leaf life range past vanilla's 7 ({@code block.LeafDecayRange}, {@code mixin/LeavesDecayRangeMixin});
     * feature_writeup/mob_block_tweaks.md 2.5. Read on every decay check.
     */
    public static final class LeafDecay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue onlyWhenDisturbed;
        public final ForgeConfigSpec.IntValue maxDistance;
        public final ForgeConfigSpec.IntValue maxSearchBlocks;
        public final ForgeConfigSpec.IntValue decayMinTicks;
        public final ForgeConfigSpec.IntValue decayMaxTicks;
        public final ForgeConfigSpec.BooleanValue leafPiles;

        private LeafDecay(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Natural (not player-placed) leaves decay when no log is close enough. Vanilla allows 6 blocks of",
                    "leaf path to a log and decays on slow random ticks; big generated trees have outer leaves past",
                    "that, which fell off on their own. Here a leaf decays shortly after a block next to it changes",
                    "(Fast Leaf Decay, ported in), and only if a search outward through the other far-out leaves",
                    "finds no log within maxDistance. Separated Leaves' rules (which leaves count which logs) are",
                    "kept. Server side."
            ).push("leaf_decay");

            enabled = b.comment("Master on/off switch. Off = vanilla decay (range and slow random-tick timing).")
                    .define("enabled", true);

            onlyWhenDisturbed = b.comment(
                    "true: random ticks never decay a natural leaf. A leaf only decays after a block next to it",
                    "changes: when its log is cut it rechecks 4-11 ticks later, and a far-out leaf rechecks when a",
                    "neighbour changes (so a cut tree's canopy falls away in a cascade). Trees from worldgen and",
                    "saplings are placed without neighbour updates, so an untouched tree keeps every leaf, even ones",
                    "past maxDistance or touching only diagonally. Cheaper than false: the range search runs only on",
                    "a change. false: vanilla timing (random ticks), with the maxDistance range."
            ).define("onlyWhenDisturbed", true);

            maxDistance = b.comment(
                    "Longest leaf path a natural leaf may be from a log and still live once it is checked, counted in",
                    "steps through leaves (a leaf touching the log is 1). Vanilla is 6; 6 here = vanilla range. Keeps a",
                    "big tree whole when something next to it changes (snow, vines, a placed block). Cost: a",
                    "chopped tree whose canopy touches a standing tree of the same kind keeps leaves out to this",
                    "range from the standing tree."
            ).defineInRange("maxDistance", 12, 6, 32);

            maxSearchBlocks = b.comment(
                    "Most leaf blocks one search may look at before giving up (the leaf then decays as in",
                    "vanilla). Bounds the cost in very large canopies; at maxDistance 12 a search rarely tops a",
                    "few hundred."
            ).defineInRange("maxSearchBlocks", 4096, 64, 65536);

            decayMinTicks = b.comment(
                    "Fast decay delay: a leaf whose log was cut, or that sits next to a block that just became air,",
                    "is checked a random number of ticks later between decayMinTicks and decayMaxTicks (Fast Leaf",
                    "Decay's MinimumDecayTime / MaximumDecayTime, 4 and 11). Spreads a felled canopy over a moment."
            ).defineInRange("decayMinTicks", 4, 1, 1200);
            decayMaxTicks = b.comment("Upper end of the fast decay delay (raised to decayMinTicks if lower).")
                    .defineInRange("decayMaxTicks", 11, 1, 1200);

            leafPiles = b.comment(
                    "A leaf that decays may leave an Immersive Weathering leaf pile in its place (which then falls),",
                    "as it did with Fast Leaf Decay installed. Chance and blacklist are IW's own [leaf_piles]",
                    "spawn_entity_from_decay / leaf_piles_blacklist. false: decayed leaves only drop their items."
            ).define("leafPiles", true);

            b.pop(); // leaf_decay
        }
    }

    /** Glowing Illagers port ({@code mixin/RaidGlowingMixin}). */
    public static final class Raids {
        public final ForgeConfigSpec.BooleanValue glowingRaiders;
        public final ForgeConfigSpec.IntValue glowingAmplifier;

        private Raids(ForgeConfigSpec.Builder b) {
            b.comment("Raids (server side).").push("raids");

            glowingRaiders = b.comment("Every raider a raid spawns glows for good (Glowing Illagers port).")
                    .define("glowingRaiders", true);
            glowingAmplifier = b.comment("Glowing amplifier (the mod used 1 = Glowing II; the level changes nothing for Glowing).")
                    .defineInRange("glowingAmplifier", 1, 0, 255);

            b.pop(); // raids
        }
    }

    /** How a structure's grass/dirt base is swapped for the local ground ({@code worldgen/GroundMatch}). */
    public enum GroundMode { TERRAIN, PALETTE, REMOVE }

    /** Which structure starts the earth-fill check logs ({@code worldgen/EarthFill}). */
    public enum EarthFillLog { OFF, CANCELLED, ALL }

    /** [worldgen.ocean_geodes] floorPatch: what happens to an ocean geode that reaches the sea floor. */
    public enum GeodeFloor { OFF, CUT, SWAP }

    /**
     * Every worldgen system's switch and every tuning constant that isn't per-structure data. Per-structure
     * and per-piece values stay in kubejs/generation_scripts/worldgen/*.json (hundreds of entries); this is
     * the layer above them, for testing: turn a system off, scale every structure's rate, tune the solvers.
     * Read through {@code worldgen/WorldgenConfig}, which falls back to the defaults before the file loads.
     * Most of it is read at world load (rejoin to apply); ground matching and /cdevplace read it live.
     */
    public static final class Worldgen {
        public final WorldgenSystems systems;
        public final WorldgenDebug debug;
        public final PoolTuning pools;
        public final BiomeSolver biomeSolver;
        public final GroundMatch groundMatch;
        public final EarthFill earthFill;
        public final WaterFill waterFill;
        public final TerrainAdaptation terrainAdaptation;
        public final MetaStructures metaStructures;
        public final OceanGeodes oceanGeodes;
        public final PlacementPriorityCfg placementPriority;
        public final LocateCfg locate;
        public final DistantCfg distant;
        public final FirstTickWaterCfg firstTickWater;

        private Worldgen(ForgeConfigSpec.Builder b) {
            b.comment(
                    "World generation: switches per system, a debug rate multiplier, and the constants of the pack's",
                    "worldgen code. The per-structure, per-piece and per-biome tables are the JSON files in",
                    "kubejs/generation_scripts/worldgen/ (see feature_writeup/structure_tuning.md). Structure and",
                    "biome settings apply when a world loads (rejoin), and only to chunks generated afterwards."
            ).push("worldgen");
            systems = new WorldgenSystems(b);
            debug = new WorldgenDebug(b);
            pools = new PoolTuning(b);
            biomeSolver = new BiomeSolver(b);
            groundMatch = new GroundMatch(b);
            earthFill = new EarthFill(b);
            waterFill = new WaterFill(b);
            terrainAdaptation = new TerrainAdaptation(b);
            metaStructures = new MetaStructures(b);
            oceanGeodes = new OceanGeodes(b);
            placementPriority = new PlacementPriorityCfg(b);
            locate = new LocateCfg(b);
            distant = new DistantCfg(b);
            firstTickWater = new FirstTickWaterCfg(b);
            b.pop(); // worldgen
        }
    }

    /** One switch per worldgen system, so each can be A/B tested against the mods' own defaults. */
    public static final class WorldgenSystems {
        public final ForgeConfigSpec.BooleanValue structureGroups;
        public final ForgeConfigSpec.BooleanValue structureFrequencies;
        public final ForgeConfigSpec.BooleanValue structureBiomeRules;
        public final ForgeConfigSpec.BooleanValue structureBiomeFits;
        public final ForgeConfigSpec.BooleanValue poolTuning;
        public final ForgeConfigSpec.BooleanValue lootContainerSwaps;
        public final ForgeConfigSpec.BooleanValue biomeLayout;
        public final ForgeConfigSpec.BooleanValue deadPlacementFilter;
        public final ForgeConfigSpec.BooleanValue groundMatch;
        public final ForgeConfigSpec.BooleanValue earthFill;
        public final ForgeConfigSpec.BooleanValue waterFill;
        public final ForgeConfigSpec.BooleanValue terrainAdaptation;
        public final ForgeConfigSpec.BooleanValue templateTrim;
        public final ForgeConfigSpec.BooleanValue metaStructures;
        public final ForgeConfigSpec.BooleanValue structureReplacements;
        public final ForgeConfigSpec.BooleanValue mushroomSpawns;
        public final ForgeConfigSpec.BooleanValue wildFlowers;
        public final ForgeConfigSpec.BooleanValue mobSpawns;
        public final ForgeConfigSpec.BooleanValue beeNests;
        public final ForgeConfigSpec.BooleanValue oceanGeodes;
        public final ForgeConfigSpec.BooleanValue placementPriority;
        public final ForgeConfigSpec.BooleanValue templateBlockSwaps;
        public final ForgeConfigSpec.BooleanValue spawnBoxes;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> removeSpawners;
        public final ForgeConfigSpec.BooleanValue permafrostMerge;
        public final ForgeConfigSpec.BooleanValue mulberrySapling;
        public final ForgeConfigSpec.BooleanValue firstTickWater;
        public final ForgeConfigSpec.BooleanValue holdGeneratedFallingBlocks;
        public final ForgeConfigSpec.BooleanValue dimensionFilter;
        public final ForgeConfigSpec.BooleanValue sharedTerrainCache;

        private WorldgenSystems(ForgeConfigSpec.Builder b) {
            b.comment("Switch a system off to see the mods' own behaviour. Off never deletes a config file.").push("systems");
            structureGroups = b.comment(
                    "structure_groups.json + group/* tags (kubejs worldgen/structure_overrides.js). Off: every structure",
                    "set as its mod ships it (debug.structureDensity still applies). Rejoin to apply."
            ).define("structureGroups", true);
            structureFrequencies = b.comment(
                    "The per-structure frequency roll (patcherMod StructureFrequencies). Off: every group member spawns",
                    "whenever it wins its slot, i.e. at its weight share only. Rejoin to apply."
            ).define("structureFrequencies", true);
            structureBiomeRules = b.comment(
                    "biome_rules in structure_groups.json (patcherMod StructureBiomeRules): only allowed structures start in",
                    "the rule's biomes (water: ships, wrecks, ocean ruins...). Off: every structure uses its own biome list. Rejoin."
            ).define("structureBiomeRules", true);
            structureBiomeFits = b.comment(
                    "biome_fits.json + the land/* structure tags (patcherMod StructureBiomeFits): a structure in a fit tag spawns in",
                    "its fit's biomes (land/oak: land/oak + land biomes) instead of its own list. Off: its own list. Rejoin; new chunks only."
            ).define("structureBiomeFits", true);
            poolTuning = b.comment("pool_config.json / pool_tags.json village-house weights (PoolTuner). Off: every pool at its first-seen weights. Rejoin.")
                    .define("poolTuning", true);
            lootContainerSwaps = b.comment("loot_container_swaps.json: modded loot containers placed by structures become vanilla ones for Lootr.")
                    .define("lootContainerSwaps", true);
            biomeLayout = b.comment("biome_config.json multipliers / replacements (BiomeLayoutTuner). Only affects worlds created afterwards.")
                    .define("biomeLayout", true);
            deadPlacementFilter = b.comment("Explorer maps and /locate skip structure sets at frequency 0 (without it they can hang the server).")
                    .define("deadPlacementFilter", true);
            groundMatch = b.comment("Swap a structure's grass/dirt base for the local ground where grass isn't native ([worldgen.ground_match]).")
                    .define("groundMatch", true);
            earthFill = b.comment("Cancel a structure whose foundation would need too much earth over water or air ([worldgen.earth_fill]).",
                    "New chunks only.")
                    .define("earthFill", true);
            waterFill = b.comment("Cancel a water structure whose water would replace too much dry ground or air ([worldgen.water_fill]).",
                    "New chunks only.")
                    .define("waterFill", true);
            terrainAdaptation = b.comment("The creativeworldgen:terrain_adaptation/* structure tags replace a structure's own terrain adaptation",
                    "(none, bury, beard_thin, beard_box, or the pack's soft beard; [worldgen.terrain_adaptation]). Rejoin; new chunks only.")
                    .define("terrainAdaptation", true);
            templateTrim = b.comment("Structures in the creativeworldgen:template_trim tag skip their template's air and bare pad (grass,",
                    "dirt, plants) outside the built columns, so the terrain around them stays ([worldgen.terrain_adaptation]). New chunks only.")
                    .define("templateTrim", true);
            metaStructures = b.comment("meta_structures.json: our structures built from other structures and features (a village with peaceful",
                    "towers, ruin clusters, pillager camps with siege weapons, a driftwood flotilla...; patcherMod MetaStructure, [worldgen.meta_structures]).",
                    "Off: none generate (their anchors spawn plain). Rejoin; new chunks only.")
                    .define("metaStructures", true);
            structureReplacements = b.comment("Group members with \"replaces\" + \"share\" in structure_groups.json take that share of another member's slots",
                    "(the vanilla swamp hut in YUNG's witch hut slots; patcherMod MetaSlots). Off: they never generate and the",
                    "structures they replace keep every slot. Rejoin; new chunks only.")
                    .define("structureReplacements", true);
            mushroomSpawns = b.comment("mushroom_spawns.json: wild mushrooms, colonies and huge Fungi Delight mushrooms at the surface and in caves",
                    "(kubejs worldgen/mushroom_spawns.js). Off: mushrooms as their mods ship them. Rejoin to apply; only new chunks change.")
                    .define("mushroomSpawns", true);
            wildFlowers = b.comment("wild_flowers.json: flower biomes and rates, Botania colors per biome, Flower Patch blocks in wild flowers",
                    "(kubejs worldgen/wild_flowers.js). Off: every flower as its mod ships it. Rejoin to apply; only new chunks change.")
                    .define("wildFlowers", true);
            mobSpawns = b.comment("mob_spawns.json: per-biome mob spawn weights, additions and removals (kubejs worldgen/mob_spawns.js,",
                    "patcherMod SpawnTuning). Off: every mob spawns as its mod ships it. Rejoin to apply; existing worlds change too.")
                    .define("mobSpawns", true);
            beeNests = b.comment("bee_nests.json: per-biome bee nest chance on generated trees, plus extra bee-nest trees (kubejs",
                    "worldgen/bee_nests.js, patcherMod BeeNests). Off: nests as the mods ship them. Rejoin to apply; only new chunks change.")
                    .define("beeNests", true);
            oceanGeodes = b.comment("Geodes under oceans generate lower so fewer break through the sea floor ([worldgen.ocean_geodes];",
                    "patcherMod OceanGeodes: vanilla amethyst and every Geode Plus geode). Off: at their own height. New chunks only.")
                    .define("oceanGeodes", true);
            placementPriority = b.comment("Placement priority (structure_groups.json \"priority\"; patcherMod PlacementPriority): a structure whose",
                    "pieces would overlap a higher-ranked structure's never starts (settlements > pillagers > dungeons > houses",
                    "and camps > ruins), and companion villages around fortified towns. Off: structures may overlap, no",
                    "companions. Rejoin; new chunks only.")
                    .define("placementPriority", true);
            templateBlockSwaps = b.comment("meat_block_swaps.json (patcherMod TemplateBlockSwaps): marked blocks in structure templates are swapped",
                    "when the template loads (lone red glazed terracotta \"meat\" -> Architect's Palette salmon log). Off: templates as",
                    "the mods ship them. Rejoin; new chunks only.")
                    .define("templateBlockSwaps", true);
            spawnBoxes = b.comment("spawn_boxes.json (patcherMod StructureSpawnBoxes): pillager and witch structures respawn their own mob mix",
                    "inside a spawn box, members of a meta structure keep their own boxes (or take the meta's), and box evokers drop no",
                    "totem. Off: only the mods' own spawn_overrides (inside metas each part keeps its own either way). Applies at once, old chunks too.")
                    .define("spawnBoxes", true);
            removeSpawners = b.comment("Mob spawners of these entities are removed from structure templates when the template loads (patcherMod",
                    "TemplateSpawnerFilter); the spawn boxes take their place. A template that loses one also loses its spawners with",
                    "no mob. Empty list: every spawner stays. Rejoin; new chunks only.")
                    .defineList("removeSpawners", java.util.List.of("minecraft:pillager"), o -> o instanceof String);
            permafrostMerge = b.comment("Quark's frozen peaks cave style lays Immersive Weathering's permafrost on floors, ceilings and pillars",
                    "instead of Quark's own block (renamed Froststone; patcherMod QuarkPermafrostStyleMixin). Walls stay packed ice.",
                    "Off: Quark's block. New chunks only.")
                    .define("permafrostMerge", true);
            mulberrySapling = b.comment("Upgrade Aquatic's river sapling (Mulberry Sapling) grows the datapack's upgrade_aquatic:river_tree, the oak",
                    "trunk tree that worldgen places by rivers (patcherMod MulberrySapling). Off: UA's built-in tree with river logs,",
                    "which drop oak logs. Applies at once.")
                    .define("mulberrySapling", true);
            firstTickWater = b.comment("Generated water that first flows when its chunk starts ticking (aquifer edges, cave springs) breaks the",
                    "plants, carpets and torches in its way without dropping items ([worldgen.first_tick_water]; patcherMod FirstTickWater).",
                    "Off: vanilla, they drop as items. Applies at once.")
                    .define("firstTickWater", true);
            holdGeneratedFallingBlocks = b.comment("Sand, gravel and other falling blocks left floating by generation stay put when their chunk first",
                    "ticks, until a later block update next to them (patcherMod GeneratedFallingBlocks). Covers the chunk's",
                    "post-processing, generated water settling in the [worldgen.first_tick_water] window and ticks scheduled during",
                    "generation. Off: vanilla, they fall on the first tick (in the outer ring of loaded chunks the falling entity never",
                    "lands and stays saved). Applies at once.")
                    .define("holdGeneratedFallingBlocks", true);
            dimensionFilter = b.comment("A structure that cannot start in any biome of a dimension is not tried there (patcherMod DimensionFilter):",
                    "no generation point, terrain reads or jigsaw assembly for Nether and End structures in the Overworld.",
                    "Same structures either way, only faster. Off: every structure set is tried in every dimension, as vanilla 1.19.2.")
                    .define("dimensionFilter", true);
            sharedTerrainCache = b.comment("Earth and water fill share one bounded cache of predicted terrain columns across starts, placement",
                    "priority predictions, meta retries and the real start of a chunk (patcherMod EarthFill). Same results either way,",
                    "only faster. Off: each start reads its own columns.")
                    .define("sharedTerrainCache", true);
            b.pop(); // systems
        }
    }

    /** Testing knobs that are wrong for a real world. */
    public static final class WorldgenDebug {
        public final ForgeConfigSpec.DoubleValue structureDensity;
        public final ForgeConfigSpec.BooleanValue testOverrides;

        private WorldgenDebug(ForgeConfigSpec.Builder b) {
            b.comment("For testing only; leave at the defaults for play.").push("debug");
            structureDensity = b.comment(
                    "Multiplies how often EVERY structure spawns (natural generation, every set, grouped or not). 1 = off.",
                    "Below 1: each set's frequency is multiplied (exact, spacing and gaps unchanged). Above 1: grids shrink",
                    "by sqrt(x) and the frequency absorbs the rounding, so the rate is exact until a grid reaches its",
                    "minimum (spacing = separation + 1); separations shrink with the grid. structure_tag_report.md shows",
                    "the result. Rejoin to apply; only new chunks change."
            ).defineInRange("structureDensity", 1.0, 0.01, 100.0);
            testOverrides = b.comment(
                    "Scratch servers only. On: kubejs/generation_scripts/worldgen/structure_test_overrides.json loosens soft",
                    "structure rules so rare structures can be found and tested (per structure frequency, weight, meta share;",
                    "group spacing and separation). Biomes, fits, surface checks, peaceful radius, priority and earth/water",
                    "fill never change. The log and structure_tag_report.md say TEST OVERRIDES ACTIVE. Rejoin to apply;",
                    "only new chunks change."
            ).define("testOverrides", false);
            b.pop(); // debug
        }
    }

    /** {@code worldgen/PoolTuner} constants. */
    public static final class PoolTuning {
        public final ForgeConfigSpec.IntValue maxFractionScale;
        public final ForgeConfigSpec.BooleanValue canonicalOrder;

        private PoolTuning(ForgeConfigSpec.Builder b) {
            b.push("pools");
            maxFractionScale = b.comment(
                    "Fractional piece weights are made whole by scaling the pool's list by up to this factor. Jigsaw",
                    "tries every copy of a piece in a pool, so a bigger factor is more exact but slower to fail."
            ).defineInRange("maxFractionScale", 10, 1, 100);
            canonicalOrder = b.comment(
                    "Sort every pool's pieces by template id at world load. Mods that add pieces in code leave the pools",
                    "in an order that changes from one server start to the next, and jigsaw picks by position in that",
                    "list, so the same seed built different villages after each restart. Sorted, a seed gives the same",
                    "layouts every time (not vanilla's: new chunks only). false = keep the order the mods left."
            ).define("canonicalOrder", true);
            b.pop(); // pools
        }
    }

    /** {@code worldgen/BiomeLayoutTuner} solver constants (they change the solve, so they're part of its cache key). */
    public static final class BiomeSolver {
        public final ForgeConfigSpec.IntValue maxCandidates;
        public final ForgeConfigSpec.IntValue rounds;

        private BiomeSolver(ForgeConfigSpec.Builder b) {
            b.push("biome_solver");
            maxCandidates = b.comment("Biomes considered per climate sample (the closest ones). More = slower, rarely different.")
                    .defineInRange("maxCandidates", 24, 2, 256);
            rounds = b.comment("Penalty-adjustment rounds. More = closer to the target shares, slower world creation.")
                    .defineInRange("rounds", 20, 1, 200);
            b.pop(); // biome_solver
        }
    }

    /**
     * Knobs for {@code worldgen/GroundMatch}: a structure template's base blocks (grass, dirt...) are
     * swapped for the ground actually around it when that ground isn't grass/dirt (a grassy ruin in
     * volcanic peaks sits on basalt instead). Which blocks count is block-tag driven (KubeJS
     * blocktags.js), so only the behaviour lives here. Read while chunks generate: already-generated
     * chunks never change. On/off: [worldgen.systems] groundMatch.
     */
    public static final class GroundMatch {
        public final ForgeConfigSpec.EnumValue<GroundMode> mode;
        public final ForgeConfigSpec.IntValue baseLayers;
        public final ForgeConfigSpec.DoubleValue nativeShare;
        public final ForgeConfigSpec.IntValue minSamples;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> families;
        public final ForgeConfigSpec.BooleanValue log;
        public final ForgeConfigSpec.IntValue centerStride;
        public final ForgeConfigSpec.IntValue neighbourStride;
        public final ForgeConfigSpec.IntValue subDepth;
        public final ForgeConfigSpec.IntValue maxDig;
        public final ForgeConfigSpec.IntValue maxPlantHeight;

        private GroundMatch(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structure bases that don't match the ground. As a structure generates, grass/dirt in the bottom",
                    "layers of its templates is compared with the natural ground sampled around that chunk; where the",
                    "ground isn't grass/dirt (volcanic peaks, deserts, snowy slopes, badlands...) the base becomes that",
                    "ground instead. Blocks: tags creativeworldgen:ground_match/* (kubejs blocktags.js). Exempt a",
                    "structure with the structure tag creativeworldgen:ground_match/exempt. New chunks only."
            ).push("ground_match");

            mode = b.comment(
                    "TERRAIN: a base block whose spot already holds natural ground is skipped, so the terrain stays;",
                    "  the rest (base above the ground) is filled with blocks sampled from the local ground.",
                    "PALETTE: every base block becomes a sampled ground block.",
                    "REMOVE: base blocks are never placed (whatever is there stays, air included)."
            ).defineEnum("mode", GroundMode.TERRAIN);

            baseLayers = b.comment(
                    "How many template layers from the bottom count as the base. Family blocks higher up (roof",
                    "gardens, planters) are left alone."
            ).defineInRange("baseLayers", 3, 1, 16);

            nativeShare = b.comment(
                    "A family counts as native when at least this share of the sampled ground tops belong to it;",
                    "native chunks are left exactly as the template has them."
            ).defineInRange("nativeShare", 0.35, 0.0, 1.0);

            minSamples = b.comment(
                    "Fewest natural ground columns needed to decide (columns under structures are skipped). With",
                    "fewer, even after also sampling the 8 neighbouring chunks, the base is left as it is."
            ).defineInRange("minSamples", 16, 1, 1024);

            families = b.comment(
                    "Block tags, one per base family. A family block in a base is swapped where its family isn't",
                    "native. Add a tag here (and fill it in blocktags.js) to match another kind of base, e.g. sand."
            ).defineList("families", List.of("creativeworldgen:ground_match/grass"),
                    o -> o instanceof String str && str.contains(":"));

            log = b.comment("Log each changed template (chunk, family share, palettes). For testing.")
                    .define("log", false);

            b.comment("How the natural ground is sampled.").push("sampling");
            centerStride = b.comment("Every Nth column of the chunk being placed is sampled (2 = 64 columns).")
                    .defineInRange("centerStride", 2, 1, 16);
            neighbourStride = b.comment("Every Nth column of each of the 8 neighbouring chunks (4 = 16 each).")
                    .defineInRange("neighbourStride", 4, 1, 16);
            subDepth = b.comment("Blocks sampled under each column's top block (the under-block palette).")
                    .defineInRange("subDepth", 3, 1, 16);
            maxDig = b.comment("How far down past leaves, logs and plants a column is searched for its ground.")
                    .defineInRange("maxDig", 24, 1, 128);
            maxPlantHeight = b.comment("Tallest plant (in blocks) removed from a swapped block.")
                    .defineInRange("maxPlantHeight", 4, 1, 16);
            b.pop(); // sampling

            b.pop(); // ground_match
        }
    }

    /**
     * Knobs for {@code worldgen/EarthFill}: a structure is cancelled before it is stored when the earth
     * blocks in the bottom layers of its templates would sit on water or in air, judged against the
     * terrain the generator predicts there. Which blocks are earth is a block tag (kubejs blocktags.js);
     * structures that float on purpose are in the structure tag creativeworldgen:earth_fill/exempt
     * (kubejs worldgen/earth_fill.js). Read while chunks generate. On/off: [worldgen.systems] earthFill.
     */
    public static final class EarthFill {
        public final ForgeConfigSpec.IntValue maxOverWater;
        public final ForgeConfigSpec.DoubleValue maxWaterSurplus;
        public final ForgeConfigSpec.BooleanValue footprintAllPieces;
        public final ForgeConfigSpec.IntValue beardReach;
        public final ForgeConfigSpec.IntValue buryReach;
        public final ForgeConfigSpec.IntValue waterReach;
        public final ForgeConfigSpec.IntValue maxMoundHeight;
        public final ForgeConfigSpec.BooleanValue beardFootprint;
        public final ForgeConfigSpec.BooleanValue prune;
        public final ForgeConfigSpec.IntValue pruneMaxHanging;
        public final ForgeConfigSpec.IntValue pruneMaxMound;
        public final ForgeConfigSpec.IntValue pruneMinWetColumns;
        public final ForgeConfigSpec.BooleanValue pruneWetJigsaws;
        public final ForgeConfigSpec.IntValue footprintMinWater;
        public final ForgeConfigSpec.IntValue baseLayers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blocks;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;
        public final ForgeConfigSpec.IntValue sampleGrid;
        public final ForgeConfigSpec.EnumValue<EarthFillLog> log;
        public final EarthFillSizes sizes;

        private EarthFill(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structures that would need a lot of earth to stand on. Just before a structure start is stored,",
                    "every earth block (tag below) in the bottom layers of its template pieces is compared with the",
                    "terrain the generator predicts at that spot. Earth in water counts toward maxOverWater, earth in air",
                    "toward the size's air limit, but only where the structure's beard (its terrain adaptation) won't",
                    "fill under it: ground deeper than beardReach (buryReach for bury), or always with no adaptation.",
                    "Columns whose ground lies more than maxMoundHeight below a piece count toward the size's mound",
                    "limit. Past any limit the whole structure is cancelled and its slot stays empty (a slot shared",
                    "with other structures tries the next pick). The limits scale with the structure's size (its earth",
                    "base block count, [worldgen.earth_fill.sizes]). Village streets that follow the terrain are never",
                    "counted. Exempt a structure with the structure tag creativeworldgen:earth_fill/exempt. New",
                    "chunks only; /place structure is not checked."
            ).push("earth_fill");

            maxOverWater = b.comment(
                    "Most earth blocks that may stand in water the beard won't fill (a pad reaching out over a lake or the",
                    "sea deeper than beardReach). -1 = no limit, 0 = none at all. Vanilla villages set house floors into the",
                    "top water layer of rivers and coasts; their beard fills under them, so that doesn't count. Per",
                    "structure: see overrides."
            ).defineInRange("maxOverWater", 24, -1, 100000);
            maxWaterSurplus = b.comment(
                    "The footprint's water share, checked after the limits: the bounding box of every piece with earth, in the",
                    "layer the structure was set onto the surface with, and (footprintAllPieces) every other piece's box where",
                    "the surface lies within its height. Water there (filled by its earth or the beard, or left",
                    "standing) against dry ground there (replaced or left alone): the structure is cancelled when the water",
                    "outnumbers the ground by more than this share. 0.5 = water more than 50% over ground (60:40 passes, 61:39",
                    "doesn't), 0 = more water than ground, -1 = off. Catches camps on islands and ruins reaching out over a lake,",
                    "whose beard fills the shallow water so maxOverWater never sees it. A spot under more than baseLayers",
                    "blocks of terrain (underground structures, aquifers) counts as neither. Not for structures in",
                    "creativeworldgen:water_fill/structures; an override with every limit -1 skips it too. Default 0",
                    "(2026-10-30, was 0.5): the strictest setting, any start standing more on water than on ground goes."
            ).defineInRange("maxWaterSurplus", 0.0, -1.0, 1000.0);
            footprintAllPieces = b.comment(
                    "The footprint water share also judges pieces with no earth in their base: plank, wool, sandstone or brick",
                    "floors, and pieces that are not jigsaw or template pieces (vanilla desert and jungle pyramids, igloos).",
                    "A column of such a piece counts when the predicted surface (its topmost block) lies within the piece's",
                    "height (from baseLayers below its bottom to its top): water when that surface is water, ground when it",
                    "is solid. Pieces buried under the surface or floating above it count as neither. Village streets that",
                    "follow the terrain never count. Off: only pieces with earth make the footprint (before 2026-10-30)."
            ).define("footprintAllPieces", true);
            beardReach = b.comment(
                    "How deep under a piece the beard (beard_thin, beard_box, the soft beard) still fills the ground, in",
                    "blocks between the piece's bottom and the ground (air and water both: the beard fills either). Earth",
                    "over a deeper gap hangs and counts; a shallower one is filled and doesn't. Measured with",
                    "tools/earth_fill_sim.sh beard on vanilla villages: the whole gap is filled 81% of the time at 6 blocks,",
                    "35% at 7."
            ).defineInRange("beardReach", 6, 0, 32);
            buryReach = b.comment(
                    "The same for bury (the structure sinks into the ground): filled 100% to 10 blocks, 27% at 11."
            ).defineInRange("buryReach", 10, 0, 32);
            waterReach = b.comment(
                    "Earth over water counts toward maxOverWater once the water under it is deeper than this (and than the",
                    "beard's reach, when that is shorter). The beard fills shallow water, but deep fill reads as a dam in",
                    "a lake or river. Air keeps beardReach / buryReach.",
                    "Tuned 2026-11-02 (user): 2 [the beard's reach, 6]."
            ).defineInRange("waterReach", 2, 0, 32);
            maxMoundHeight = b.comment(
                    "A column whose ground lies more than this many blocks below a piece's bottom is a mound column:",
                    "the beard would build a tall pillar there, or can't reach the piece at all (an edge of a ravine, a",
                    "pad over a cliff). Each column counts once per structure, toward the size's mound limit."
            ).defineInRange("maxMoundHeight", 8, 0, 64);
            beardFootprint = b.comment(
                    "Mound columns also under the built bottom of a bearded piece, not only under its earth: a stone or",
                    "plank floor over a cliff edge stands on a beard pillar the earth count never sees. Every column with a",
                    "solid block in a rigid piece's baseLayers (trimmed columns left out) whose ground lies more than",
                    "maxMoundHeight below it counts toward the mound limit; a start with no earth at all is judged as small.",
                    "Whole-start check only (pruning still judges earth). tools/earth_fill_sim.sh <seed> <n> footprint",
                    "measured 2026-10-30: 23% of vanilla outposts and 2 to 18% of villages would newly cross their mound",
                    "limit. Off until calibrated on a log = ALL scratch run."
            ).define("beardFootprint", false);
            prune = b.comment(
                    "For structures in the structure tag creativeworldgen:earth_fill/prune (villages): before the whole start",
                    "is judged, each jigsaw piece but the first is judged alone, and one past pruneMaxHanging or pruneMaxMound",
                    "is dropped with every piece attached below it (a house over deep water with its villagers). So is a piece",
                    "whose own footprint is too wet (maxWaterSurplus) with at least pruneMinWetColumns water columns. The street",
                    "that led to it stays as a dead end. Then the rest is judged as usual. Off: the whole start is judged (and pruneWetJigsaws is off too)."
            ).define("prune", true);
            pruneMaxHanging = b.comment(
                    "Earth blocks of one piece over water or air the beard won't fill before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxHanging", 8, -1, 100000);
            pruneMaxMound = b.comment(
                    "Mound columns of one piece before that piece is dropped. -1 = never."
            ).defineInRange("pruneMaxMound", 4, -1, 100000);
            pruneMinWetColumns = b.comment(
                    "Water columns a piece's own footprint needs before its water share can drop it (so a well or a hut over",
                    "a small pond stays). -1 = never drop a piece for its water share."
            ).defineInRange("pruneMinWetColumns", 16, -1, 100000);
            pruneWetJigsaws = b.comment(
                    "With prune on, every other jigsaw structure on the surface (camps, outposts, ruins, towers; meta",
                    "structure parts and anchors too) drops a piece whose own footprint is too wet the same way, with every",
                    "piece attached below it; the piece limits above stay for villages. The first piece is never dropped,",
                    "and the rest is then judged as usual. Not for creativeworldgen:water_fill/structures,",
                    "creativeworldgen:earth_fill/wet_ok or the structure tag creativeworldgen:earth_fill/no_prune",
                    "(structures whose pieces only make sense together). Off: only villages are pruned (before 2026-10-30)."
            ).define("pruneWetJigsaws", true);
            footprintMinWater = b.comment(
                    "Water columns a whole start's footprint needs before its water share (maxWaterSurplus) can cancel it,",
                    "so a cliff-edge start whose layer is mostly air doesn't drop on one wet column. 0 = any (before",
                    "2026-10-30)."
            ).defineInRange("footprintMinWater", 9, 0, 100000);
            baseLayers = b.comment(
                    "How many template layers from the bottom count as the foundation. Earth higher up (roof gardens,",
                    "planters) is not counted: above the ground everything is air."
            ).defineInRange("baseLayers", 3, 1, 16);
            blocks = b.comment(
                    "Block tags, one entry each, that count as earth. Fill the default in kubejs blocktags.js; add a tag",
                    "here to count another kind of block."
            ).defineList("blocks", List.of("creativeworldgen:earth_fill/earth"),
                    o -> o instanceof String str && str.contains(":"));
            overrides = b.comment(
                    "Per-structure limits, one entry each: \"<structure id or #structure tag>=<water>,<air>[,<mound>]\",",
                    "e.g. \"philipsruins:ancient_ruins=0,16\" (never over water, at most 16 over air after the beard,",
                    "mound from its size) or \"#creativeworldgen:type/village=-1,-1,-1\" (no limits). They replace the",
                    "size's limits. The first matching entry wins; -1 = no limit."
            ).defineList("overrides", List.of(), o -> o instanceof String str && str.contains("="));
            sampleGrid = b.comment(
                    "The predicted terrain is read once per N x N block cell instead of per column (cheaper, a little",
                    "coarser at cliffs). 1 = every column. [worldgen.water_fill] uses it too."
            ).defineInRange("sampleGrid", 4, 1, 8);
            log = b.comment(
                    "CANCELLED: log each structure that is cancelled, and each pruned village or meta part dropped (id, chunk,",
                    "counts, limits). ALL: log every checked structure that has earth in its base or (footprintAllPieces) a",
                    "surface piece. For tuning the limits."
            ).defineEnum("log", EarthFillLog.CANCELLED);
            sizes = new EarthFillSizes(b);
            b.pop(); // earth_fill
        }
    }

    /**
     * {@code [worldgen.earth_fill.sizes]}: earth-fill limits by the size of the structure, so a fish pond and a
     * fortified town aren't held to one number. The size is the start's own earth base block count.
     */
    public static final class EarthFillSizes {
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> bounds;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> small;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> medium;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> large;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> huge;

        private EarthFillSizes(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A structure's size is its earth base block count (each start counted as it generates, a meta",
                    "structure part by part). Each size has [maxOverAir, maxMound]: earth blocks over air after the beard,",
                    "and mound columns; -1 = no limit. First guesses, to calibrate with log = ALL."
            ).push("sizes");
            bounds = b.comment(
                    "Earth base blocks where medium, large and huge start: under the first is small, from the last on huge.")
                    .defineList("bounds", List.of(150, 600, 2000), o -> o instanceof Integer i && i > 0);
            small = b.comment("[maxOverAir, maxMound] for small structures (a well, a small ruin, a fish pond).")
                    .defineList("small", List.of(24, 8), o -> o instanceof Integer i && i >= -1);
            medium = b.comment("[maxOverAir, maxMound] for medium structures (a house, a camp, a tower).")
                    .defineList("medium", List.of(64, 24), o -> o instanceof Integer i && i >= -1);
            large = b.comment("[maxOverAir, maxMound] for large structures (a village, a fortified village, a mansion).")
                    .defineList("large", List.of(160, 64), o -> o instanceof Integer i && i >= -1);
            huge = b.comment("[maxOverAir, maxMound] for huge structures.")
                    .defineList("huge", List.of(320, 128), o -> o instanceof Integer i && i >= -1);
            b.pop(); // sizes
        }
    }

    /**
     * Knobs for the water half of {@code worldgen/EarthFill}: a structure in the structure tag
     * creativeworldgen:water_fill/structures (kubejs worldgen/water_fill.js) is cancelled before it is
     * stored when the water its templates carry would replace too much dry ground or air, judged against
     * the same predicted terrain as earth fill (and its sampleGrid). On/off: [worldgen.systems] waterFill.
     */
    public static final class WaterFill {
        public final ForgeConfigSpec.IntValue maxIntoGround;
        public final ForgeConfigSpec.IntValue maxIntoAir;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> overrides;
        public final ForgeConfigSpec.EnumValue<EarthFillLog> log;

        private WaterFill(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Water structures that would flood the land. Just before a structure start in the structure tag",
                    "creativeworldgen:water_fill/structures is stored, every water block of its template pieces (water,",
                    "waterlogged blocks, kelp, seagrass; all layers) is compared with the terrain the generator predicts",
                    "at that spot. Water in water is fine, and so is water replacing a sea, lake or river bed. Water",
                    "replacing dry ground counts toward maxIntoGround, water in air (above the sea surface or on land)",
                    "toward maxIntoAir; past either limit the whole structure is cancelled and its slot stays empty.",
                    "Uses [worldgen.earth_fill] sampleGrid. New chunks only; /place structure is not checked."
            ).push("water_fill");

            maxIntoGround = b.comment(
                    "Most water blocks that may replace dry ground (a ruin reaching onto a beach or into a hillside).",
                    "-1 = no limit, 0 = none at all. Per structure: see overrides."
            ).defineInRange("maxIntoGround", 32, -1, 100000);
            maxIntoAir = b.comment(
                    "Most water blocks that may land in air (water standing above the sea surface or on land, where it",
                    "spills). -1 = no limit, 0 = none at all. Per structure: see overrides."
            ).defineInRange("maxIntoAir", 16, -1, 100000);
            overrides = b.comment(
                    "Per-structure limits, one entry each: \"<structure id or #structure tag>=<ground>,<air>\". The default",
                    "lets the structures in creativeworldgen:water_fill/ponds (fish ponds, a house on a pond) dig their",
                    "water into the ground and allows them 16 in air. The first matching entry wins; -1 = no limit.",
                    "Only structures in creativeworldgen:water_fill/structures are checked at all."
            ).defineList("overrides", List.of("#creativeworldgen:water_fill/ponds=-1,16"), o -> o instanceof String str && str.contains("="));
            log = b.comment(
                    "CANCELLED: log each structure that is cancelled (id, chunk, counts, limits). ALL: log every checked",
                    "structure that carries any water. For tuning the limits."
            ).defineEnum("log", EarthFillLog.CANCELLED);
            b.pop(); // water_fill
        }
    }

    /**
     * Knobs for {@code worldgen/TerrainAdaptation} and {@code worldgen/TemplateTrim}: which structures get which
     * terrain adaptation, and which are trimmed, is structure tags (kubejs worldgen/terrain_adaptation.js); only
     * the soft beard's width lives here. On/off: [worldgen.systems] terrainAdaptation and templateTrim.
     */
    public static final class TerrainAdaptation {
        public final ForgeConfigSpec.IntValue skirtBump;

        private TerrainAdaptation(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Terrain around structures. Vanilla's beard flattens the whole template bounding box and blends it out",
                    "over about 8 blocks around it, so a small structure with a flat pad or a lot of saved air leaves a flat",
                    "rectangle the size of its whole template in a curved hillside. Structures in the structure tag",
                    "creativeworldgen:terrain_adaptation/soft get the soft beard instead: the same fill and carve, but",
                    "only under the template's built columns (pad and air columns left out when the structure is also in",
                    "creativeworldgen:template_trim), blending out over a short round skirt. Tags: kubejs",
                    "worldgen/terrain_adaptation.js. New chunks only."
            ).push("terrain_adaptation");
            skirtBump = b.comment(
                    "Blocks added to the soft beard's minimum skirt of 4 (where its fill and carve fade out, measured from",
                    "the built footprint). 0 = the tightest blend; 4 = as wide as vanilla's beard (8), but round and",
                    "around the footprint; 8 = the most there is room for (12, vanilla's reach). Read per chunk."
            ).defineInRange("skirtBump", 0, 0, 8);
            b.pop(); // terrain_adaptation
        }
    }

    /** {@code /cdevplace}: whole structures or raw templates laid out in rows, for inspecting them in a flat world. */
    /**
     * Knobs for {@code worldgen/MetaStructure}. The structures themselves are defined in
     * kubejs/generation_scripts/worldgen/meta_structures.json; on/off is [worldgen.systems] metaStructures.
     */
    /** worldgen/OceanGeodes: how far geodes under oceans are pushed down; on/off is [worldgen.systems] oceanGeodes. */
    public static final class OceanGeodes {
        public final ForgeConfigSpec.IntValue lowestFloorY;
        public final ForgeConfigSpec.IntValue clearance;
        public final ForgeConfigSpec.IntValue spread;
        public final ForgeConfigSpec.DoubleValue stayShare;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> untouched;
        public final ForgeConfigSpec.EnumValue<GeodeFloor> floorPatch;

        private OceanGeodes(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Geodes whose column is an ocean (the biome at the sea floor is #minecraft:is_ocean) are moved down before",
                    "they generate, so they stay under the deepest sea floor the world can have. A moved geode's top (its",
                    "origin + the geode's own maxGenOffset) ends at least clearance blocks under lowestFloorY; geodes already",
                    "that deep stay where they are. Which geodes stay and how far each spreads is a fixed roll per position."
            ).push("ocean_geodes");
            lowestFloorY = b.comment("The lowest sea floor y any ocean in the pack generates (ST-133: Terralith deep oceans near y 0;",
                    "Tectonic deeper_oceans can go lower).")
                    .defineInRange("lowestFloorY", -8, -64, 64);
            clearance = b.comment("Blocks of stone kept between a moved geode's top and lowestFloorY.")
                    .defineInRange("clearance", 2, 0, 16);
            spread = b.comment("Moved geodes land up to this many blocks under the highest allowed origin (a roll per position),",
                    "so they don't all sit on one layer.")
                    .defineInRange("spread", 8, 0, 32);
            stayShare = b.comment("Share of geodes under oceans that keep their own height (0.1 = 1 in 10).")
                    .defineInRange("stayShare", 0.1, 0.0, 1.0);
            untouched = b.comment("Geodes whose inner layer is one of these blocks are left at their own height (Geode Plus'",
                    "prismarine geode belongs on the sea floor).")
                    .defineList("untouched", java.util.List.of("geode:budding_prismarine"), o -> o instanceof String);
            floorPatch = b.comment("A geode under an ocean that still reaches the sea floor (the stayShare ones; not the untouched",
                    "ones). SWAP: it keeps its shape, and its outer and middle layer blocks from the floor up become the floor",
                    "block that column had before (sand, gravel, clay), so it reads as a mound of sea floor with the opening",
                    "into its crystals. CUT: it is cut flush with the floor (its blocks above the floor become water) and the",
                    "rim left on the surface becomes the floor block. OFF: it sticks out as generated.")
                    .defineEnum("floorPatch", GeodeFloor.SWAP);
            b.pop();
        }
    }

    /** worldgen/PlacementPriority; on/off is [worldgen.systems] placementPriority. */
    public static final class PlacementPriorityCfg {
        public final ForgeConfigSpec.BooleanValue log;

        private PlacementPriorityCfg(ForgeConfigSpec.Builder b) {
            b.push("placement_priority");
            log = b.comment("Log every structure start dropped because a higher-ranked structure overlaps it, and every",
                    "companion village. For testing.")
                    .define("log", false);
            b.pop();
        }
    }

    /**
     * Structure searches (/locate, explorer maps, map trades, dolphins, Eyes of Ender; all through Async Locator):
     * the cheap "could it start here?" prediction, chunk loads for misses, StructureCheck thread safety, miss
     * logging and the /cwglocate batch and census commands. See feature_writeup/locate.md.
     */
    public static final class LocateCfg {
        public final ForgeConfigSpec.BooleanValue foldPlacement;
        public final ForgeConfigSpec.BooleanValue foldBiomes;
        public final ForgeConfigSpec.BooleanValue nonBlockingLoads;
        public final ForgeConfigSpec.BooleanValue threadSafeStructureCheck;
        public final ForgeConfigSpec.IntValue logMissesAtLeast;
        public final ForgeConfigSpec.IntValue batchRadius;
        public final ForgeConfigSpec.IntValue batchTimeoutSeconds;
        public final ForgeConfigSpec.IntValue censusRegionChunks;
        public final ForgeConfigSpec.IntValue loadTimeoutSeconds;
        public final ForgeConfigSpec.IntValue searchTimeoutSeconds;

        private LocateCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Structure searches. A miss is a slot the cheap prediction called possible, so the search generated that",
                    "chunk's structure starts, and nothing was there. Each fold below makes the prediction say \"no\" earlier,",
                    "only where generation would certainly say no. Async Locator's thread count is its own server config",
                    "(serverconfig/asynclocator-server.toml, asyncLocatorThreads)."
            ).push("locate");
            foldPlacement = b.comment(
                    "Stage A: the structure set's own frequency and exclusion zone (vanilla checks both when generating but",
                    "not when searching). Exact. Applies to new searches immediately.")
                    .define("foldPlacement", true);
            foldBiomes = b.comment(
                    "Stage B: the biome at the structure's generation point, with the pack's biome rules and fits (vanilla",
                    "never tests the biome in the prediction). Exact. Applies to new searches immediately.")
                    .define("foldBiomes", true);
            nonBlockingLoads = b.comment(
                    "A search running off the server thread (Async Locator) waits for a miss's chunk itself instead of making",
                    "the server thread block until the chunk's structure starts exist. Off: vanilla's blocking load.")
                    .define("nonBlockingLoads", true);
            threadSafeStructureCheck = b.comment(
                    "Make vanilla's structure-presence cache safe for several search threads at once (needed before raising",
                    "asyncLocatorThreads above 1). Read when a dimension loads (rejoin to apply).")
                    .define("threadSafeStructureCheck", true);
            logMissesAtLeast = b.comment(
                    "Log one [locate] line for every search with at least this many misses (0 = every search, -1 = never).")
                    .defineInRange("logMissesAtLeast", 8, -1, Integer.MAX_VALUE);
            batchRadius = b.comment("Default search radius of /cwglocate batch, in chunks (/locate uses 100).")
                    .defineInRange("batchRadius", 100, 1, 1000);
            batchTimeoutSeconds = b.comment(
                    "A /cwglocate batch search still running after this long is reported as timed out and cancelled (Async",
                    "Locator never completes a search that threw).")
                    .defineInRange("batchTimeoutSeconds", 120, 5, 3600);
            censusRegionChunks = b.comment(
                    "/cwglocate census splits its square into regions this many chunks wide; each region is one task on",
                    "Async Locator's thread pool.")
                    .defineInRange("censusRegionChunks", 32, 4, 256);
            loadTimeoutSeconds = b.comment(
                    "A non-blocking miss load that hasn't finished after this long counts as a miss (protects search threads",
                    "from a stopping server).")
                    .defineInRange("loadTimeoutSeconds", 300, 10, 3600);
            searchTimeoutSeconds = b.comment(
                    "Any structure search (/locate, maps, Async Locator) still running after this long ends as not found and",
                    "logs a [locate] line with its counters; its thread is free again. /cwglocate stop ends running searches",
                    "too, and so does the server stopping. 0 = no deadline. Applies to new searches immediately.",
                    "Tuned 2026-11-02 (user): 120 [300]; the fortified metas answer within 120 s.")
                    .defineInRange("searchTimeoutSeconds", 120, 0, 3600);
            b.pop();
        }
    }

    /**
     * Distant Horizons' own world generation (its throwaway LOD chunks, never real chunks): the bottom of the world
     * is not generated; oceans get a fake seabed instead. See feature_writeup/locate.md 3.7.
     */
    /** Settling window of {@code worldgen.FirstTickWater} ([worldgen.systems] firstTickWater switches it). */
    public static final class FirstTickWaterCfg {
        public final ForgeConfigSpec.IntValue windowTicks;

        private FirstTickWaterCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Generated water flows for the first time when its chunk starts ticking. For windowTicks after that, that",
                    "water and every block it spreads to drop no items when the flow breaks something."
            ).push("first_tick_water");
            windowTicks = b.comment("Length of the window in ticks (20 = 1 s), counted from the chunk's first tick and refreshed",
                    "each time the water spreads to a block. 0: no window (vanilla drops). Applies at once.")
                    .defineInRange("windowTicks", 600, 0, 72000);
            b.pop(); // first_tick_water
        }
    }

    public static final class DistantCfg {
        public final ForgeConfigSpec.BooleanValue clip;
        public final ForgeConfigSpec.IntValue clipBelowY;
        public final ForgeConfigSpec.IntValue oceanFloorY;
        public final ForgeConfigSpec.IntValue deepOceanFloorY;
        public final ForgeConfigSpec.IntValue floorVariation;
        public final ForgeConfigSpec.IntValue floorScale;
        public final ForgeConfigSpec.BooleanValue skipFeatures;

        private DistantCfg(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Distant Horizons' distant generation only (chunks it generates for LODs and throws away). Below clipBelowY",
                    "no terrain noise is computed: land columns are filled with stone, and an ocean column whose block at",
                    "clipBelowY is water keeps water down to a fake sand/gravel floor that rolls around a per-biome height.",
                    "Chunks players walk in are never affected."
            ).push("distant");
            clip = b.comment("Clip Distant Horizons' generation below clipBelowY.")
                    .define("clip", true);
            clipBelowY = b.comment("No noise below this y (rounded down to a multiple of 16).")
                    .defineInRange("clipBelowY", 48, -64, 320);
            oceanFloorY = b.comment("Fake floor height in ocean biomes (capped just under clipBelowY).")
                    .defineInRange("oceanFloorY", 48, -64, 320);
            deepOceanFloorY = b.comment("Fake floor height in deep ocean biomes (#minecraft:is_deep_ocean).")
                    .defineInRange("deepOceanFloorY", 8, -64, 320);
            floorVariation = b.comment("How far the fake floor rolls above and below its height, in blocks.")
                    .defineInRange("floorVariation", 4, 0, 32);
            floorScale = b.comment("Width of the fake floor's rolls, in blocks (seeded from the world seed).")
                    .defineInRange("floorScale", 24, 4, 256);
            skipFeatures = b.comment("Skip placed features (ores, geodes, cave decoration) that start below clipBelowY.")
                    .define("skipFeatures", true);
            b.pop();
        }
    }

    public static final class MetaStructures {
        public final ForgeConfigSpec.BooleanValue log;
        public final ForgeConfigSpec.BooleanValue avoidCoasts;
        public final ForgeConfigSpec.BooleanValue keepShortParts;

        private MetaStructures(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Meta structures: one structure start made of other structures (an anchor such as a village, plus",
                    "satellites such as towers around it) and placed features (driftwood on the sea). Every part keeps",
                    "within 7 chunks of the start so the whole thing generates; a part that can't fit (biome, room)",
                    "is retried elsewhere; a part short of its minimum count keeps what fitted (keepShortParts).",
                    "A meta with an anchor only takes a share of the slots its anchor already won, so no structure's rate",
                    "changes. /locate finds a meta by its id and a structure inside one by the structure's own id."
            ).push("meta_structures");
            log = b.comment(
                    "Log every meta structure start that is assembled: its anchor, each part and how many of each",
                    "fitted, or which part fell short (the slot is then the plain anchor)."
            ).define("log", false);
            avoidCoasts = b.comment(
                    "Keep meta structures out of coasts and rivers (the biome tag creativeworldgen:coast_river: beaches,",
                    "shores, rivers; kubejs biome_fits.json biome_tags). No part may start there or reach there with its box",
                    "(centre or a corner): a satellite tries another spot, an anchor there leaves the slot to the plain anchor,",
                    "which still spawns as it would alone. Metas in the structure tag creativeworldgen:meta/coast_ok (fleets,",
                    "flotillas, wrecks, sunken ruins) are not affected. New chunks only."
            ).define("avoidCoasts", true);
            keepShortParts = b.comment(
                    "A part that fits fewer times than its minimum count (a satellite near a coast, on a cliff) is kept with",
                    "what fitted instead of failing the whole meta; earth fill likewise drops an offending satellite and",
                    "prunes village parts instead of cancelling the meta. The meta only fails when no structure besides the",
                    "anchor is left (the slot is then the plain anchor). Off: a short part fails the meta (before 2026-10-30)."
            ).define("keepShortParts", true);
            b.pop(); // meta_structures
        }
    }

    /** Named mobs ({@code mobs.NamedMobs}, feature_writeup/mob_block_tweaks.md section 2.6). Read live. */
    public static final class NamedMobs {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue chance;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> mobs;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> spawnReasons;
        public final ForgeConfigSpec.BooleanValue announceDeaths;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> fallbackNames;

        private NamedMobs(ForgeConfigSpec.Builder b) {
            b.comment(
                    "A listed mob that spawns for one of the listed reasons has a small chance to get a random name",
                    "(Minecraft Comes Alive's citizen names, else fallbackNames). Its death is announced in chat.",
                    "Mobs already named are left alone; bred, egg, spawner and command mobs never roll."
            ).push("named_mobs");

            enabled = b.comment("Off: no new names. Mobs named earlier keep their name and announcement.")
                    .define("enabled", true);
            chance = b.comment("Chance per eligible spawn. 0.01 = 1 in 100.")
                    .defineInRange("chance", 0.01, 0.0, 1.0);
            mobs = b.comment("Entity ids, or #entity type tags, that can be named.")
                    .defineList("mobs", java.util.List.of("minecraft:cow", "minecraft:mooshroom", "minecraft:pig",
                            "minecraft:sheep", "minecraft:chicken", "minecraft:goat"), o -> o instanceof String s && !s.isBlank());
            spawnReasons = b.comment("Spawn reasons that roll: NATURAL, CHUNK_GENERATION, STRUCTURE (see MobSpawnType for the rest).")
                    .defineList("spawnReasons", java.util.List.of("NATURAL", "CHUNK_GENERATION", "STRUCTURE"), o -> o instanceof String);
            announceDeaths = b.comment("Every player sees a named mob's death message (\"Bessie the Cow was slain by Steve\").",
                    "Follows the showDeathMessages gamerule.")
                    .define("announceDeaths", true);
            fallbackNames = b.comment("Names used when Minecraft Comes Alive is missing or its name lists are not loaded.")
                    .defineList("fallbackNames", java.util.List.of("Bessie", "Clover", "Daisy", "Biscuit", "Maple", "Pepper",
                            "Hazel", "Barnaby", "Rosie", "Winston", "Mabel", "Otis", "Juniper", "Gus", "Poppy", "Waffles"),
                            o -> o instanceof String s && !s.isBlank());

            b.pop(); // named_mobs
        }
    }

    /**
     * The mob cap levels and spawn areas of {@code perf/MobCapLevels} (perf.md 17; mixin/MobCapMixin, MobCapLevelTickMixin,
     * MobCapSpawnAreaMixin). Moved from patcherMod's [perf] on 2026-11-02 with keys, comments and defaults unchanged; the
     * table keeps patcherMod's comment. Read live.
     */
    public static final class Perf {
        public final ForgeConfigSpec.BooleanValue mobCapLevels;
        public final ForgeConfigSpec.IntValue mobCapForceLevel;
        public final ForgeConfigSpec.IntValue mobCapLevel1Chunks;
        public final ForgeConfigSpec.IntValue mobCapLevel2Chunks;
        public final ForgeConfigSpec.IntValue mobCapHysteresisChunks;
        public final ForgeConfigSpec.IntValue mobCapLevel1Monster;
        public final ForgeConfigSpec.IntValue mobCapLevel2Monster;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> mobCapScaledCategories;
        public final ForgeConfigSpec.IntValue spawnChunksLevel1;
        public final ForgeConfigSpec.IntValue spawnChunksLevel2;
        public final ForgeConfigSpec.IntValue spawnBandLevel1;
        public final ForgeConfigSpec.IntValue spawnBandLevel2;
        public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> spawnBandCategories;

        private Perf(ForgeConfigSpec.Builder b) {
            b.comment("Performance throttles for other mods' per-tick work. See feature_writeup/perf.md section 7.")
                    .push("perf");

            mobCapLevels = b.comment(
                    "Mob cap levels (perf.md section 17). Vanilla's cap grows with the spawnable chunks (the chunks",
                    "within 8 chunks of any player, 289 for one player alone), so spread-out players each add a",
                    "whole monster cap. On: above the chunk counts below, the per-chunk caps drop to level 1 or 2.",
                    "Lowering a cap removes no mobs; it only stops new spawns until the count falls below it.",
                    "Off: vanilla caps."
            ).define("mobCapLevels", true);
            mobCapForceLevel = b.comment(
                    "Force a mob cap level in every dimension (0, 1 or 2) for testing. -1 = pick it from the",
                    "spawnable chunk count."
            ).defineInRange("mobCapForceLevel", -1, -1, 2);
            mobCapLevel1Chunks = b.comment(
                    "Level 1 starts above this many spawnable chunks in a dimension (430 = about 1.5 player areas)."
            ).defineInRange("mobCapLevel1Chunks", 430, 0, 100000);
            mobCapLevel2Chunks = b.comment(
                    "Level 2 starts above this many spawnable chunks (870 = about 3 player areas)."
            ).defineInRange("mobCapLevel2Chunks", 870, 0, 100000);
            mobCapHysteresisChunks = b.comment(
                    "A level is only left when the chunk count falls this far below its threshold, so it does not",
                    "flip back and forth while a player walks along the edge."
            ).defineInRange("mobCapHysteresisChunks", 30, 0, 10000);
            mobCapLevel1Monster = b.comment(
                    "Monster cap at level 1 (vanilla 70). The other scaled categories get the same fraction."
            ).defineInRange("mobCapLevel1Monster", 50, 1, 70);
            mobCapLevel2Monster = b.comment(
                    "Monster cap at level 2 (vanilla 70)."
            ).defineInRange("mobCapLevel2Monster", 25, 1, 70);
            mobCapScaledCategories = b.comment(
                    "Spawn categories whose cap is scaled at levels 1 and 2. creature is left out on purpose:",
                    "animals are persistent and their herds are placed at chunk generation."
            ).defineList("mobCapScaledCategories", java.util.List.of("monster", "ambient", "water_creature",
                    "water_ambient", "underground_water_creature", "axolotls"), o -> o instanceof String);
            spawnChunksLevel1 = b.comment(
                    "At level 1, the scaled categories above only spawn in chunks within this many chunks of a",
                    "player (vanilla 8), so mobs spawn closer in and fewer chunks are rolled. 0 = vanilla."
            ).defineInRange("spawnChunksLevel1", 6, 0, 8);
            spawnChunksLevel2 = b.comment(
                    "The same spawn radius at level 2. 0 = vanilla."
            ).defineInRange("spawnChunksLevel2", 5, 0, 8);
            spawnBandLevel1 = b.comment(
                    "At level 1, natural spawns of the categories below must be within this many blocks above or",
                    "below the nearest player, so caves far below a surface player do not fill the cap.",
                    "0 = no limit."
            ).defineInRange("spawnBandLevel1", 64, 0, 512);
            spawnBandLevel2 = b.comment(
                    "The same vertical band at level 2. 0 = no limit."
            ).defineInRange("spawnBandLevel2", 32, 0, 512);
            spawnBandCategories = b.comment(
                    "Spawn categories the vertical band applies to."
            ).defineList("spawnBandCategories", java.util.List.of("monster"), o -> o instanceof String);

            b.pop(); // perf
        }
    }
}
