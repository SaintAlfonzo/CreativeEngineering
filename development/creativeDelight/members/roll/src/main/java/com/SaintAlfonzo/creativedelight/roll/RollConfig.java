package com.SaintAlfonzo.creativedelight.roll;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common config (roll_delight-common.toml). {@code [chopsticks]}: how much longer a food's effects last (roll_delight.md 3.3, D17). */
public final class RollConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue LENGTHEN_FACTOR;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("chopsticks");
        LENGTHEN_FACTOR = b.comment("Duration multiplier for the effects a food gives when eaten from the off hand with chopsticks in the main hand",
                "(or from a roll medley with chopsticks). Instant and ambient effects are left alone; the amplifier never changes.")
                .defineInRange("lengthenFactor", 1.5D, 1.0D, 10.0D);
        b.pop();
        SPEC = b.build();
    }

    private RollConfig() {
    }
}
