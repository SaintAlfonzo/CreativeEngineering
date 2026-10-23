package net.ribs.vintagedelight.config;

import net.minecraftforge.common.ForgeConfigSpec;

// From Vintage Delight KOTS 0.3.1: the magic vine tree's ground conversion can be switched off.
public final class VintageDelightCommonConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS;

    private VintageDelightCommonConfig() {
    }

    public static boolean magicVineConvertsDirtToLushGrass() {
        return MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS.get();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("worldgen");
        MAGIC_VINE_CONVERTS_DIRT_TO_LUSH_GRASS = builder
            .comment("Whether magic vine trees convert nearby dirt-like blocks into lush grass during generation.")
            .define("magicVineConvertsDirtToLushGrass", true);
        builder.pop();
        SPEC = builder.build();
    }
}
