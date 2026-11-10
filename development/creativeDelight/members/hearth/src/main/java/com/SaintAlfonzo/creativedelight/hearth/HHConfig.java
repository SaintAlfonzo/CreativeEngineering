package com.SaintAlfonzo.creativedelight.hearth;

import net.minecraftforge.common.ForgeConfigSpec;

/** hearthandharvest-common.toml: switches and chances of the ported systems (hearth_and_harvest.md 4.4). */
public final class HHConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue BOTTLE_MILKING;
    public static final ForgeConfigSpec.BooleanValue CHICKEN_PLUCKING;
    public static final ForgeConfigSpec.BooleanValue LITTERS;
    public static final ForgeConfigSpec.DoubleValue LITTER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue TRUFFLE_LITTER_CHANCE;
    public static final ForgeConfigSpec.BooleanValue MANURE_FROM_FED_ANIMALS;
    public static final ForgeConfigSpec.DoubleValue TAPPER_CHANCE;
    public static final ForgeConfigSpec.DoubleValue CORN_HUSK_HARVEST_CHANCE;
    public static final ForgeConfigSpec.BooleanValue SALT_CAVES_ENABLED;
    public static final ForgeConfigSpec.IntValue SALT_CAVE_RARITY;
    public static final ForgeConfigSpec.IntValue SALT_CAVE_MIN_Y;
    public static final ForgeConfigSpec.IntValue SALT_CAVE_MAX_Y;
    public static final ForgeConfigSpec.BooleanValue CORN_MAZE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue HOPS_REQUIRE_FARMLAND;
    public static final ForgeConfigSpec.IntValue TROUGH_ANIMAL_CAP;
    public static final ForgeConfigSpec.IntValue SPRINKLER_RESCAN_TICKS;
    public static final ForgeConfigSpec.IntValue STOMPING_BASIN_SLOT_LIMIT;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("animals");
        BOTTLE_MILKING = b.comment("A glass bottle milks cows and goats.").define("bottleMilking", true);
        CHICKEN_PLUCKING = b.comment("Sneak-use on a chicken plucks a feather.").define("chickenPlucking", true);
        LITTERS = b.comment("Breeding pigs and rabbits can give one extra baby.").define("litters", true);
        LITTER_CHANCE = b.comment("Chance of an extra baby.").defineInRange("litterChance", 1.0D / 3.0D, 0.0D, 1.0D);
        TRUFFLE_LITTER_CHANCE = b.comment("Chance of that extra baby when pigs were bred with a truffle (on top of Fungi Delight's own extra piglets).")
                .defineInRange("truffleLitterChance", 2.0D / 3.0D, 0.0D, 1.0D);
        MANURE_FROM_FED_ANIMALS = b.comment("Animals drop manure 30 to 60 s after being fed or, for sheep, after grazing.").define("manureFromFedAnimals", true);
        b.pop();
        b.push("farming");
        TAPPER_CHANCE = b.comment("Chance every 2 s that a tree tapper collects 10 mB of sap.").defineInRange("tapperChance", 0.5D, 0.0D, 1.0D);
        CORN_HUSK_HARVEST_CHANCE = b.comment("Chance of a Corn Husk when harvesting corn.").defineInRange("cornHuskHarvestChance", 0.25D, 0.0D, 1.0D);
        HOPS_REQUIRE_FARMLAND = b.comment("Hops only grow when the bottom of their lattice column stands on farmland.").define("hopsRequireFarmland", true);
        b.pop();
        b.push("machines");
        TROUGH_ANIMAL_CAP = b.comment("Water Trough stops breeding when this many animals stand within 20x8x20 blocks of it.").defineInRange("troughBreedingCap", 16, 1, 256);
        SPRINKLER_RESCAN_TICKS = b.comment("Ticks between a Field Sprinkler's scans of its area for farmland, crops and fire (20 = 1 s).").defineInRange("sprinklerRescanTicks", 100, 20, 1200);
        STOMPING_BASIN_SLOT_LIMIT = b.comment("Items that fit in one Stomping Basin slot (one slot alone, four in a 2x2 group).").defineInRange("stompingBasinSlotLimit", 64, 1, 512);
        b.pop();
        b.push("worldgen");
        SALT_CAVES_ENABLED = b.comment("Generate salt caves.").define("saltCaves", true);
        SALT_CAVE_RARITY = b.comment("One salt cave attempt in this many chunks of a salt cave biome.").defineInRange("saltCaveRarity", 24, 1, 100000);
        SALT_CAVE_MIN_Y = b.comment("Lowest centre height of a salt cave.").defineInRange("saltCaveMinY", -40, -64, 300);
        SALT_CAVE_MAX_Y = b.comment("Highest centre height of a salt cave.").defineInRange("saltCaveMaxY", 30, -64, 300);
        CORN_MAZE_ENABLED = b.comment("Generate corn mazes (their rate is in the pack's structure tables).").define("cornMaze", true);
        b.pop();
        com.SaintAlfonzo.creativedelight.hearth.salt.SaltConfig.define(b);
        com.SaintAlfonzo.creativedelight.hearth.crow.CrowConfig.define(b);
        SPEC = b.build();
    }

    private HHConfig() {
    }
}
