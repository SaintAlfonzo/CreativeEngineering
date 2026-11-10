package com.SaintAlfonzo.creativedelight.hearth.crow;

import net.minecraftforge.common.ForgeConfigSpec;

/** Section [crow] of hearthandharvest-common.toml (plan_hearth_crow_salt.md section 4, hearth_and_harvest.md 5.8 N24-6 to N24-8). */
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
    public static ForgeConfigSpec.BooleanValue SPAWN_NEAR_NESTS;
    public static ForgeConfigSpec.BooleanValue DENY_SPAWNS_ELSEWHERE;
    public static ForgeConfigSpec.IntValue SCARE_RADIUS;
    public static ForgeConfigSpec.IntValue CARRY_TICKS;
    public static ForgeConfigSpec.IntValue SPAWN_RADIUS;
    public static ForgeConfigSpec.IntValue CROP_REQUIREMENT;
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
        STASH_IN_NESTS = b.comment("A crow carrying loot leaves it in a nest within 16 blocks. For the crow eval.").define("stashInNests", true);
        DROP_ON_HURT = b.comment("A hit makes a crow drop its loot. Off, it keeps the loot until it dies or the carry time ends.").define("dropOnHurt", true);
        CARRY_TICKS = b.comment("Ticks a crow carries loot before dropping it.").defineInRange("carryTicks", 1200, 20, 72000);
        SCARE_RADIUS = b.comment("Blocks at which players, villagers and repelling blocks scare wild crows. 0 stops them being scared.").defineInRange("scareRadius", 6, 0, 32);
        GENERATE_NESTS = b.comment("Ground nests generate in new chunks. For the crow eval.").define("generateNests", true);
        SPAWN_NEAR_NESTS = b.comment("Crows may spawn near world-generated nests.").define("spawnNearNests", true);
        SPAWN_RADIUS = b.comment("Radius checked for nests and crops when a crow spawns. 0 skips the check.").defineInRange("spawnRadius", 8, 0, 16);
        CROP_REQUIREMENT = b.comment("Crops needed within the radius for a crow to spawn near them. 0 skips the crop check.").defineInRange("cropRequirement", 8, 0, 256);
        DENY_SPAWNS_ELSEWHERE = b.comment("Crows spawn only near nests or crops (the upstream rule). Off keeps every other spawn.").define("denySpawnsElsewhere", false);
        b.pop();
    }
}
