package com.SaintAlfonzo.creativedelight.blockheads;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common config (blockheads_additions-common.toml). {@code [kitchen]}: the counter-top offsets of
 * {@code kitchen.CounterToppers}, moved from creativeengineering-common.toml {@code [kitchen]} 2026-10-31 with the same
 * keys and defaults (TODO cooking/kitchen #8, #20).
 */
public final class BAConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue CUTTING_BOARD_OFFSET_X;
    public static final ForgeConfigSpec.DoubleValue CUTTING_BOARD_OFFSET_Y;
    public static final ForgeConfigSpec.DoubleValue CUTTING_BOARD_OFFSET_Z;
    public static final ForgeConfigSpec.DoubleValue PLATE_OFFSET_X;
    public static final ForgeConfigSpec.DoubleValue PLATE_OFFSET_Y;
    public static final ForgeConfigSpec.DoubleValue PLATE_OFFSET_Z;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment(
                "Small blocks on a Cooking for Blockheads counter or corner (#creativeengineering:counter_supports):",
                "Farmer's Delight's cutting board, Display Delight's plates and plated foods, Brewin' and Chewin's",
                "coaster (#creativeengineering:counter_toppers). The counter's worktop is 1 px lower than a full",
                "block, so they and what lies on them are drawn shifted by these offsets, in pixels (1/16 block):",
                "cuttingBoardOffset* for the board, plateOffset* for the plates and the coaster. X and Z are as",
                "seen on a counter facing north (door to the north, back edge to the south: +X = east, +Z = toward",
                "the back) and turn with the counter. Visual only; F3+A redraws."
        ).push("kitchen");

        CUTTING_BOARD_OFFSET_X = b.comment("Sideways shift.").defineInRange("cuttingBoardOffsetX", 0.0, -8.0, 8.0);
        CUTTING_BOARD_OFFSET_Y = b.comment("Height shift; -1 puts the board on the worktop.")
                .defineInRange("cuttingBoardOffsetY", -1.0, -8.0, 8.0);
        CUTTING_BOARD_OFFSET_Z = b.comment("Shift toward the counter's back (negative = toward the door).")
                .defineInRange("cuttingBoardOffsetZ", 0.0, -8.0, 8.0);
        PLATE_OFFSET_X = b.comment("Plates and coasters: sideways shift.").defineInRange("plateOffsetX", 0.0, -8.0, 8.0);
        PLATE_OFFSET_Y = b.comment("Plates and coasters: height shift; -1 puts them on the worktop.")
                .defineInRange("plateOffsetY", -1.0, -8.0, 8.0);
        PLATE_OFFSET_Z = b.comment("Plates and coasters: shift toward the counter's back (negative = toward the door).")
                .defineInRange("plateOffsetZ", 0.0, -8.0, 8.0);

        b.pop();
        SPEC = b.build();
    }

    private BAConfig() {
    }
}
