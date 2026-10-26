package com.SaintAlfonzo.creativedelight.enders;

import net.minecraftforge.common.ForgeConfigSpec;

/** Common config (enders_additions-common.toml): growth chances of the End plants per soil tier. */
public final class EAConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue CHORUSFLAME_GROWTH_SLOW;
    public static final ForgeConfigSpec.DoubleValue CHORUSFLAME_GROWTH_FAST;
    public static final ForgeConfigSpec.DoubleValue VOIDPEPPER_TICK_SLOW;
    public static final ForgeConfigSpec.DoubleValue VOIDPEPPER_TICK_FAST;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Growth of the End plants. Each plant only grows on its soil: blocks in the block tag",
                "creative_delight:end_plant_soil_fast use the Fast chance, blocks in creative_delight:end_plant_soil_slow",
                "the Slow chance. Off those blocks the plants cannot be placed and break.").push("growth");
        CHORUSFLAME_GROWTH_SLOW = b.comment("Chance per random tick that a Chorusflame on slow soil grows one stage (3 stages).")
                .defineInRange("chorusflameGrowthSlow", 0.1, 0.0, 1.0);
        CHORUSFLAME_GROWTH_FAST = b.comment("Chance per random tick that a Chorusflame on fast soil grows one stage. Ender's Delight 1.1.4: 0.2.")
                .defineInRange("chorusflameGrowthFast", 0.2, 0.0, 1.0);
        VOIDPEPPER_TICK_SLOW = b.comment("Chance per random tick that a spreading Void Pepper Sprout attached to slow soil acts:",
                        "then 2 in 3 it spreads to a free spot near soil, 1 in 3 it fruits and stops spreading.")
                .defineInRange("voidpepperTickSlow", 0.5, 0.0, 1.0);
        VOIDPEPPER_TICK_FAST = b.comment("The same for a sprout attached to fast soil. Ender's Delight 1.1.4: 1.0 (acts on every random tick).")
                .defineInRange("voidpepperTickFast", 1.0, 0.0, 1.0);
        b.pop();
        SPEC = b.build();
    }

    private EAConfig() {
    }
}
