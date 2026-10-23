package com.SaintAlfonzo.incubation_additions;

import net.minecraftforge.common.ForgeConfigSpec;

/** {@code config/incubation_additions-common.toml}. Registered only when Untitled Duck Mod is loaded. */
public final class IAConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GOLDEN_EGG_CHANCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Untitled Duck Mod geese.").push("goose");
        GOLDEN_EGG_CHANCE = b.comment("Chance, as 1 in N, that a goose lays a Golden Egg (Vintage Delight) instead of its",
                        "next goose egg. Rolled once per egg, before the goose looks for a nest; a golden egg is always",
                        "dropped on the ground near the goose, never laid in a nest. 0 = never.",
                        "Does nothing without Vintage Delight.")
                .defineInRange("goldenEggChance", 1000, 0, 1_000_000);
        b.pop();

        SPEC = b.build();
    }

    private IAConfig() {}
}
