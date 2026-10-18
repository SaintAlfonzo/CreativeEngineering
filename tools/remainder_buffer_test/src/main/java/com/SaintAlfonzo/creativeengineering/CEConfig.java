package com.SaintAlfonzo.creativeengineering;

import net.minecraftforge.common.ForgeConfigSpec;

/** Test stand-in: only the [cooking] section of the real CEConfig (copied verbatim below). */
public final class CEConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Cooking COOKING;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COOKING = new Cooking(builder);
        COMMON_SPEC = builder.build();
    }
    public static final class Cooking {
        public final ForgeConfigSpec.BooleanValue remainderBuffer;
        public final ForgeConfigSpec.BooleanValue stallWhenFull;

        private Cooking(ForgeConfigSpec.Builder b) {
            b.comment(
                    "Leftover containers from ingredients (glass bottles, bowls, buckets) on the FD cooking pot, the",
                    "Farmer's Respite kettle and the oven. A leftover the waiting meal is served in goes to the",
                    "container slot; the rest goes to two leftover slots right of the output, which hoppers and",
                    "funnels on the output faces can empty."
            ).push("cooking");

            remainderBuffer = b.comment("Off = leftovers are thrown out of the station as in the original mods.")
                    .define("remainderBuffer", true);

            stallWhenFull = b.comment(
                    "On = a station whose leftover slots can't take a craft's leftovers waits (like a full output).",
                    "Off = it crafts anyway and throws what doesn't fit."
            ).define("stallWhenFull", true);

            b.pop(); // cooking
        }
}
}
