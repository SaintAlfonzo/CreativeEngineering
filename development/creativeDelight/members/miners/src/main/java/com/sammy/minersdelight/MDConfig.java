package com.sammy.minersdelight;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Port settings ({@code config/miners_delight-common.toml}). Upstream has no config; everything here is the pack's
 * (feature_writeup/miners_delight.md I8).
 */
public final class MDConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue REMAINDER_BUFFER;
    private static final ForgeConfigSpec.BooleanValue STALL_WHEN_FULL;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("copper_pot");
        REMAINDER_BUFFER = builder
                .comment("Port setting: keep the ingredients' leftover containers (empty buckets, bottles, bowls) in a take-only",
                        "leftover slot right of the output instead of throwing them out of the pot. The bottom face extracts it.")
                .define("remainderBuffer", true);
        STALL_WHEN_FULL = builder
                .comment("Port setting: with remainderBuffer on, should the pot wait instead of cooking when a meal's leftovers would",
                        "not fit in the leftover slot? False cooks anyway and throws what does not fit.")
                .define("stallWhenFull", true);
        builder.pop();
        SPEC = builder.build();
    }

    private MDConfig() {
    }

    public static boolean remainderBuffer() {
        return REMAINDER_BUFFER.get();
    }

    public static boolean stallWhenFull() {
        return STALL_WHEN_FULL.get();
    }
}
