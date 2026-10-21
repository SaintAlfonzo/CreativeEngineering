package com.SaintAlfonzo.baby_mobs;

import net.minecraftforge.common.ForgeConfigSpec;

/** {@code config/baby_mobs-common.toml}. Defaults are vanilla 1.21.2 / 26.1 values. */
public final class BMConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue SQUID_BABY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue GLOW_SQUID_BABY_CHANCE;
    public static final ForgeConfigSpec.DoubleValue DOLPHIN_BABY_CHANCE;
    public static final ForgeConfigSpec.IntValue GROW_TICKS;
    public static final ForgeConfigSpec.BooleanValue DOLPHIN_FISH_FEEDING;
    public static final ForgeConfigSpec.BooleanValue BABY_MODELS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Which natural spawns are babies. The first mob of a spawn group is always an adult; each further",
                "member rolls this chance (vanilla's AgeableMob rule). Spawn eggs and /summon give adults unless",
                "the egg's or command's NBT says Age:-24000.").push("spawns");
        SQUID_BABY_CHANCE = b.comment("Squid (vanilla 0.05)").defineInRange("squidBabyChance", 0.05, 0.0, 1.0);
        GLOW_SQUID_BABY_CHANCE = b.comment("Glow squid (vanilla 0.05)").defineInRange("glowSquidBabyChance", 0.05, 0.0, 1.0);
        DOLPHIN_BABY_CHANCE = b.comment("Dolphin (vanilla 0.10)").defineInRange("dolphinBabyChance", 0.10, 0.0, 1.0);
        b.pop();

        b.comment("Growing up.").push("growth");
        GROW_TICKS = b.comment("Ticks a baby takes to grow up (vanilla 24000 = 20 minutes)").defineInRange("growTicks", 24000, 1, 1_000_000);
        DOLPHIN_FISH_FEEDING = b.comment("Feeding a baby dolphin any #minecraft:fishes item ages it up by 10% of the time left (vanilla)")
                .define("dolphinFishFeeding", true);
        b.pop();

        b.comment("Client.").push("client");
        BABY_MODELS = b.comment("Use the 26.1 baby models and textures for young squid, glow squid and dolphins;",
                "false = vanilla's adult model at half size").define("babyModels", true);
        b.pop();

        SPEC = b.build();
    }

    private BMConfig() {}
}
