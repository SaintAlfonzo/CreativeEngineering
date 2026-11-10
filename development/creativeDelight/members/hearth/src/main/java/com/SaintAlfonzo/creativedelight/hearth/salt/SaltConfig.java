package com.SaintAlfonzo.creativedelight.hearth.salt;

import net.minecraftforge.common.ForgeConfigSpec;

/** Section [salt] of hearthandharvest-common.toml (plan_hearth_crow_salt.md S2 to S4, hearth_and_harvest.md 5.8 N24-10). */
public final class SaltConfig {
    public static ForgeConfigSpec.BooleanValue PLAYER_LICKING;
    public static ForgeConfigSpec.BooleanValue ANIMAL_LICKING;
    public static ForgeConfigSpec.DoubleValue PLAYER_LICK_CHANCE;
    public static ForgeConfigSpec.DoubleValue ANIMAL_STAGE_CHANCE;
    public static ForgeConfigSpec.IntValue ANIMAL_RADIUS;
    public static ForgeConfigSpec.IntValue SEARCH_INTERVAL;
    public static ForgeConfigSpec.IntValue LICK_INTERVAL;

    private SaltConfig() {
    }

    public static void define(ForgeConfigSpec.Builder b) {
        b.push("salt");
        PLAYER_LICKING = b.comment("Right-clicking a salt block with both hands empty licks it.").define("playerLicking", true);
        ANIMAL_LICKING = b.comment("Animals in the salt_lickers tag stay near salt blocks and lick them.").define("animalLicking", true);
        PLAYER_LICK_CHANCE = b.comment("Chance that a player's lick wears the block one stage.").defineInRange("saltPlayerLickChance", 0.05D, 0.0D, 1.0D);
        ANIMAL_STAGE_CHANCE = b.comment("Chance that an animal's lick wears the block one stage.").defineInRange("saltAnimalStageChance", 0.125D, 0.0D, 1.0D);
        ANIMAL_RADIUS = b.comment("Blocks an animal keeps within of a salt block. The search reaches 2 further.").defineInRange("saltAnimalRadius", 20, 1, 64);
        SEARCH_INTERVAL = b.comment("Ticks between searches for a salt block.").defineInRange("saltSearchInterval", 200, 20, 6000);
        LICK_INTERVAL = b.comment("Average ticks between an animal's licks, randomised by a quarter either way. The first lick comes after half to one and a half intervals.")
                .defineInRange("saltLickInterval", 1200, 200, 72000);
        b.pop();
    }
}
