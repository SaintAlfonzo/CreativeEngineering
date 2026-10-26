package net.ribs.vintagedelight.config;

import net.minecraftforge.common.ForgeConfigSpec;

// From Vintage Delight KOTS 0.3.1: the magic vine tree's ground conversion can be switched off.
// Port: the Pickling Jar's leftover slot (remainder_buffer.md, as the keg's).
public final class VintageDelightCommonConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS;
    private static final ForgeConfigSpec.BooleanValue REMAINDER_BUFFER;
    private static final ForgeConfigSpec.BooleanValue STALL_WHEN_FULL;

    private VintageDelightCommonConfig() {
    }

    public static boolean magicVineConvertsDirtToLushGrass() {
        return MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS.get();
    }

    public static boolean remainderBuffer() {
        return REMAINDER_BUFFER.get();
    }

    public static boolean stallWhenFull() {
        return STALL_WHEN_FULL.get();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("worldgen");
        MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS = builder
            .comment("Whether magic vine trees convert nearby dirt-like blocks into lush grass during generation.")
            .define("magicVineConvertsDirtToLushGrass", true);
        builder.pop();
        builder.push("pickling_jar");
        REMAINDER_BUFFER = builder
            .comment("Port setting: keep ingredient leftovers (glass bottles, bowls, buckets) in the Pickling Jar's leftover slot instead of throwing them out?")
            .define("remainderBuffer", true);
        STALL_WHEN_FULL = builder
            .comment("Port setting: with remainderBuffer on, should the jar wait instead of pickling when a batch's leftovers would not fit?",
                "False pickles anyway and throws what does not fit.")
            .define("stallWhenFull", true);
        builder.pop();
        SPEC = builder.build();
    }
}
