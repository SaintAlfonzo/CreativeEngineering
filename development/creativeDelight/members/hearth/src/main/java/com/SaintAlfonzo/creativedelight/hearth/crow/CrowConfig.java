package com.SaintAlfonzo.creativedelight.hearth.crow;

import net.minecraftforge.common.ForgeConfigSpec;

/** Section [crow] of hearthandharvest-common.toml (plan_hearth_crow_salt.md section 4, plan_hearth_crow_swap.md 2.2 and 5). */
public final class CrowConfig {
    public static ForgeConfigSpec.BooleanValue FLEEING;
    public static ForgeConfigSpec.BooleanValue FLOCK_ALARM;
    public static ForgeConfigSpec.BooleanValue TEMPTING;
    public static ForgeConfigSpec.BooleanValue HAND_FEEDING;
    public static ForgeConfigSpec.BooleanValue EAT_DROPPED_FOOD;
    public static ForgeConfigSpec.BooleanValue CORN_BREEDING;
    public static ForgeConfigSpec.BooleanValue EAT_CROPS;
    public static ForgeConfigSpec.BooleanValue STEAL_SHINY_ITEMS;
    public static ForgeConfigSpec.BooleanValue STASH_IN_NESTS;
    public static ForgeConfigSpec.BooleanValue DROP_ON_HURT;
    public static ForgeConfigSpec.BooleanValue GENERATE_NESTS;
    public static ForgeConfigSpec.BooleanValue FETCH_ITEMS;
    public static ForgeConfigSpec.BooleanValue FETCH_THROWN_ITEMS;
    public static ForgeConfigSpec.BooleanValue LEAVE_SHOULDER_TO_FETCH;
    public static ForgeConfigSpec.BooleanValue PLAYER_NESTS_SPAWN_CROWS;
    public static ForgeConfigSpec.BooleanValue VILLAGE_ROOF_NESTS;
    public static ForgeConfigSpec.DoubleValue VILLAGE_ROOF_NEST_CHANCE;
    public static ForgeConfigSpec.IntValue NEST_FLOCK_MIN;
    public static ForgeConfigSpec.IntValue NEST_FLOCK_MAX;
    public static ForgeConfigSpec.IntValue NEST_CROW_CAP;
    public static ForgeConfigSpec.IntValue NEST_CROW_RANGE;
    public static ForgeConfigSpec.IntValue NEST_RESPAWN_MINUTES;
    public static ForgeConfigSpec.IntValue NEST_PLAYER_RANGE;
    public static ForgeConfigSpec.IntValue SCARE_RADIUS;
    public static ForgeConfigSpec.IntValue CARRY_TICKS;
    public static ForgeConfigSpec.DoubleValue STEAL_CHANCE;

    private CrowConfig() {
    }

    public static void define(ForgeConfigSpec.Builder b) {
        b.push("crow");
        FLEEING = b.comment("Wild crows flee players, villagers and whoever hurt them.").define("fleeing", true);
        FLOCK_ALARM = b.comment("Hurting a wild crow alarms the wild crows within 16 blocks.").define("flockAlarm", true);
        TEMPTING = b.comment("Wild crows come to a player holding crow food and slowly let them closer.").define("tempting", true);
        HAND_FEEDING = b.comment("Feeding crow food by hand can tame a wild crow, 1 in 5.").define("handFeeding", true);
        EAT_DROPPED_FOOD = b.comment("Wild crows eat crow food on the ground, 1 in 8 to tame the thrower.").define("eatDroppedFood", true);
        CORN_BREEDING = b.comment("Tame crows also breed with corn and corn seeds.").define("cornBreeding", true);
        EAT_CROPS = b.comment("Wild crows peck crops back to age 0. Also needs mobGriefing.").define("eatCrops", true);
        STEAL_SHINY_ITEMS = b.comment("Wild crows take one shiny item from a player's hotbar.").define("stealShinyItems", true);
        STEAL_CHANCE = b.comment("Chance per second that a wild crow within 10 blocks starts a theft.").defineInRange("stealChance", 0.1D, 0.0D, 1.0D);
        STASH_IN_NESTS = b.comment("A crow carrying loot leaves it in a nest within 16 blocks.").define("stashInNests", true);
        FETCH_ITEMS = b.comment("Tame crows pick up items near their owner and bring them over.").define("fetchItems", true);
        FETCH_THROWN_ITEMS = b.comment("Tame crows also fetch items their owner threw away.").define("fetchThrownItems", true);
        LEAVE_SHOULDER_TO_FETCH = b.comment("Tame crows hop off their owner's shoulder to fetch an item.").define("leaveShoulderToFetch", true);
        DROP_ON_HURT = b.comment("A hit makes a crow drop its loot. Off, it keeps the loot until it dies or the carry time ends.").define("dropOnHurt", true);
        CARRY_TICKS = b.comment("Ticks a crow carries loot before dropping it.").defineInRange("carryTicks", 1200, 20, 72000);
        SCARE_RADIUS = b.comment("Blocks at which players, villagers and repelling blocks scare wild crows. 0 stops them being scared.").defineInRange("scareRadius", 6, 0, 32);
        GENERATE_NESTS = b.comment("Nests generate on tree tops in temperate forests in new chunks.").define("generateNests", true);
        VILLAGE_ROOF_NESTS = b.comment("Nests generate on village house roofs in new chunks.").define("villageRoofNests", true);
        VILLAGE_ROOF_NEST_CHANCE = b.comment("Chance per village house of a roof nest.").defineInRange("villageRoofNestChance", 0.25D, 0.0D, 1.0D);
        NEST_FLOCK_MIN = b.comment("Fewest crows a nest generates with. Crows have no other natural spawn.").defineInRange("nestFlockMin", 2, 0, 8);
        NEST_FLOCK_MAX = b.comment("Most crows a nest generates with.").defineInRange("nestFlockMax", 3, 0, 8);
        NEST_CROW_CAP = b.comment("A generated nest stops refilling once this many crows are within nestCrowRange.").defineInRange("nestCrowCap", 3, 0, 16);
        NEST_CROW_RANGE = b.comment("Blocks around a nest in which crows count towards nestCrowCap.").defineInRange("nestCrowRange", 24, 4, 64);
        NEST_RESPAWN_MINUTES = b.comment("Minutes of game time between two crows from the same nest.").defineInRange("nestRespawnMinutes", 5, 1, 120);
        NEST_PLAYER_RANGE = b.comment("A nest refills only with a player within this many blocks.").defineInRange("nestPlayerRange", 48, 8, 128);
        PLAYER_NESTS_SPAWN_CROWS = b.comment("Player-placed nests also make crows. Off stops crow farms.").define("playerNestsSpawnCrows", false);
        b.pop();
    }
}
