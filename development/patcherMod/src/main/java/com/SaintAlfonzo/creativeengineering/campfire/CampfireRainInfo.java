package com.SaintAlfonzo.creativeengineering.campfire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * What the WTHIT campfire lines ({@code compat.WthitCampfire}) need from the rain rule, which lives in Creative Compat
 * ({@code creativecompat}: CampfireRain, mixin CampfireRainMixin, config [campfire] in creativecompat-common.toml).
 * Compat fills the suppliers at construction; without Compat rain never douses, so the lines stay off. The per-campfire
 * tick count is {@link CampfireRainTicks}, implemented on CampfireBlockEntity by Compat's mixin.
 */
public final class CampfireRainInfo {

    /** Campfires rain puts out (filled by KubeJS); Compat's CampfireRain uses these same keys. */
    public static final TagKey<Block> RAIN_DOUSES =
            BlockTags.create(new ResourceLocation("creativeengineering", "campfire/rain_douses"));
    /** Blocks under a campfire that keep it lit in rain. */
    public static final TagKey<Block> RAIN_PROOF_FUEL =
            BlockTags.create(new ResourceLocation("creativeengineering", "campfire/rain_proof_fuel"));

    private static volatile BooleanSupplier douses = () -> false;
    private static volatile IntSupplier outTicks = () -> 160;

    private CampfireRainInfo() {}

    /** Called by Creative Compat with its [campfire] rainDouses / rainOutTicks readers. */
    public static void set(BooleanSupplier rainDouses, IntSupplier rainOutTicks) {
        douses = rainDouses;
        outTicks = rainOutTicks;
    }

    public static boolean rainDouses() {
        return douses.getAsBoolean();
    }

    public static int rainOutTicks() {
        return outTicks.getAsInt();
    }
}
