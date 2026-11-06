package com.SaintAlfonzo.more_mooblooms;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * {@code config/more_mooblooms-common.toml}. The mod defaults leave cow spawns alone (rareChance 0, no cow rule
 * files shipped): a modpack turns that on with its own rule files and its copy of this config. Witherblooms in
 * soul sand valleys are on by default.
 */
public final class MMConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue FEED_CHANCE;
    public static final ForgeConfigSpec.DoubleValue REGROW_CHANCE;
    public static final ForgeConfigSpec.DoubleValue RARE_CHANCE;
    public static final ForgeConfigSpec.BooleanValue SPAWN_SWAP;
    public static final ForgeConfigSpec.BooleanValue WITHERBLOOM_SPAWNS;
    public static final ForgeConfigSpec.BooleanValue SHEAR_BARE_TO_COW;
    public static final ForgeConfigSpec.BooleanValue BARE_SHEAR_DROPS;

    /** One switch per Buzzier Bees change; only read when buzzier_bees is installed. */
    public static final class BuzzierBees {
        public static ForgeConfigSpec.BooleanValue DISABLE_MOOBLOOM_SPAWNS;
        public static ForgeConfigSpec.BooleanValue RESTORE_COW_SPAWNS;
        public static ForgeConfigSpec.BooleanValue REMOVE_MOOBLOOMS;
        public static ForgeConfigSpec.BooleanValue EMPTY_HOSTILES_TAG;
        public static ForgeConfigSpec.BooleanValue HIDE_SPAWN_EGG;
        public static ForgeConfigSpec.BooleanValue REPLACE_TWO_BY_TWO;
        public static ForgeConfigSpec.BooleanValue REMOVE_SUNNY_FROM_ALL_EFFECTS;
        public static ForgeConfigSpec.BooleanValue REMOVE_BUTTERCUP_WORLDGEN;
        public static ForgeConfigSpec.BooleanValue REMOVE_BUTTERCUP_TRADE;
        public static ForgeConfigSpec.BooleanValue REMOVE_BUTTERCUP_RECIPES;
        public static ForgeConfigSpec.BooleanValue REMOVE_BUTTERCUP_TAGS;
        public static ForgeConfigSpec.BooleanValue REMOVE_BUTTERCUP_COMPOST;
        public static ForgeConfigSpec.BooleanValue HIDE_BUTTERCUP;
        public static ForgeConfigSpec.BooleanValue DISABLE_SUNNY;

        private BuzzierBees() {}
    }

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("moobloom");
        FEED_CHANCE = b.comment("Chance that a cow fed one of a variant's flowers turns into that moobloom.",
                        "The flower is used up whether or not it works.")
                .defineInRange("feedChance", 0.10, 0.0, 1.0);
        REGROW_CHANCE = b.comment("Chance that a sheared (bare) moobloom grows its flowers back each time it eats grass.")
                .defineInRange("regrowChance", 0.25, 0.0, 1.0);
        SHEAR_BARE_TO_COW = b.comment("Shearing a bare moobloom (flowers already sheared off) turns it back into a cow.",
                        "When false, a bare moobloom can't be sheared.")
                .define("shearBareToCow", true);
        BARE_SHEAR_DROPS = b.comment("Shearing a bare moobloom back into a cow also drops the variant's shear drops.")
                .define("bareShearDrops", true);
        b.pop();

        b.push("spawning");
        SPAWN_SWAP = b.comment("Master switch: natural cow spawns may be replaced by mooblooms (moobloom_spawns rules + rareChance).")
                .define("spawnSwap", true);
        RARE_CHANCE = b.comment("Chance that any naturally spawning cow, anywhere, becomes a random moobloom (weighted by",
                        "each variant's any_weight). Applies after the biome rules, also to the cows they leave.",
                        "0 = off (the mod default; vanilla's pink sheep is 0.00164).")
                .defineInRange("rareChance", 0.0, 0.0, 1.0);
        WITHERBLOOM_SPAWNS = b.comment("Witherblooms spawn, very rarely, in soul sand valleys (biome tag",
                        "#more_mooblooms:spawns_witherbloom). A built-in data pack: takes effect on the next world load.")
                .define("witherbloomSpawns", true);
        b.pop();

        b.comment("Only used when Buzzier Bees is installed. Each switch is one cleanup of Buzzier Bees' moobloom,",
                        "buttercup or Sunny effect. Data-pack switches take effect on the next world load / server start.")
                .push("buzzier_bees");
        BuzzierBees.DISABLE_MOOBLOOM_SPAWNS = b.comment("Turn off Buzzier Bees' moobloom spawns (sunflower plains).")
                .define("disableMoobloomSpawns", true);
        BuzzierBees.RESTORE_COW_SPAWNS = b.comment("Turn off Buzzier Bees' removal of cows from sunflower plains.")
                .define("restoreCowSpawns", true);
        BuzzierBees.REMOVE_MOOBLOOMS = b.comment("Buzzier Bees mooblooms never enter a world: existing ones are removed when",
                        "their chunk loads; eggs and /summon do nothing.")
                .define("removeMooblooms", true);
        BuzzierBees.EMPTY_HOSTILES_TAG = b.comment("Empty #buzzier_bees:moobloom_hostiles (zombies no longer hunt mooblooms).")
                .define("emptyHostilesTag", true);
        BuzzierBees.HIDE_SPAWN_EGG = b.comment("Hide Buzzier Bees' moobloom spawn egg from creative tabs and JEI.")
                .define("hideSpawnEgg", true);
        BuzzierBees.REPLACE_TWO_BY_TWO = b.comment("Two by Two asks for a bred More Mooblooms moobloom instead of Buzzier Bees' one.")
                .define("replaceTwoByTwo", true);
        BuzzierBees.REMOVE_SUNNY_FROM_ALL_EFFECTS = b.comment("How Did We Get Here no longer needs Sunny.")
                .define("removeSunnyFromAllEffects", true);
        BuzzierBees.REMOVE_BUTTERCUP_WORLDGEN = b.comment("No buttercup patches in new chunks.")
                .define("removeButtercupWorldgen", true);
        BuzzierBees.REMOVE_BUTTERCUP_TRADE = b.comment("The Wandering Trader stops selling buttercups.")
                .define("removeButtercupTrade", true);
        BuzzierBees.REMOVE_BUTTERCUP_RECIPES = b.comment("Remove the buttercup recipes (yellow dye, Create milling, Abnormals Delight cutting).")
                .define("removeButtercupRecipes", true);
        BuzzierBees.REMOVE_BUTTERCUP_TAGS = b.comment("Take the buttercup (and potted buttercup) out of every item and block tag,",
                        "so it can't make suspicious stew or go in flower boxes, planters and the like.")
                .define("removeButtercupTags", true);
        BuzzierBees.REMOVE_BUTTERCUP_COMPOST = b.comment("The composter no longer accepts buttercups.")
                .define("removeButtercupCompost", true);
        BuzzierBees.HIDE_BUTTERCUP = b.comment("Hide the buttercup from creative tabs and JEI.")
                .define("hideButtercup", true);
        BuzzierBees.DISABLE_SUNNY = b.comment("Turn the Sunny effect off completely: nothing can apply it (commands included),",
                        "it is removed from players and mobs when they join a world, and a mob with Sunny no",
                        "longer burns in daylight. Needs a restart (the burn check is a mixin).")
                .define("disableSunny", true);
        b.pop();

        SPEC = b.build();
    }

    /** A config value, or its default when the config isn't loaded yet (never throws). */
    public static boolean on(ForgeConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return value.getDefault();
        }
    }

    public static double num(ForgeConfigSpec.DoubleValue value) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return value.getDefault();
        }
    }

    private MMConfig() {}
}
