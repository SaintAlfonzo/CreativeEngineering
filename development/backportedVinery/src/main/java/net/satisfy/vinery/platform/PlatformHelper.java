package net.satisfy.vinery.platform;

import net.satisfy.vinery.api.VineryPlugin;
import net.satisfy.vinery.core.util.InfoOverlayMode;
import net.satisfy.vinery.forge.VineryConfig;
import net.satisfy.vinery.forge.VineryPlugins;

import java.util.List;

/**
 * Port: upstream's {@code @ExpectPlatform} facade, answered from {@link VineryConfig}. Kept as one class so the ported
 * code calls the same methods as upstream.
 */
public final class PlatformHelper {
    private PlatformHelper() {
    }

    public static int getTotalFermentationTime() {
        return VineryConfig.TOTAL_FERMENTATION_TIME.get();
    }

    public static int getMaxFluidLevel() {
        return VineryConfig.MAX_FLUID_LEVEL.get();
    }

    public static int getMaxFluidIncrease() {
        return VineryConfig.MAX_FLUID_INCREASE.get();
    }

    public static int getApplePressMashingTime() {
        return VineryConfig.APPLE_PRESS_MASHING_TIME.get();
    }

    public static int getApplePressFermentationTime() {
        return VineryConfig.APPLE_PRESS_FERMENTATION_TIME.get();
    }

    public static double getCherryGrowthChance() {
        return VineryConfig.CHERRY_GROWTH_CHANCE.get();
    }

    public static double getAppleGrowthChance() {
        return VineryConfig.APPLE_GROWTH_CHANCE.get();
    }

    public static double getRottenCherryChance() {
        return VineryConfig.ROTTEN_CHERRY_CHANCE.get();
    }

    public static double getGrapeGrowthChance() {
        return VineryConfig.GRAPE_GROWTH_CHANCE.get();
    }

    public static double getGrapeGrowthMultiplier() {
        return VineryConfig.GRAPE_GROWTH_MULTIPLIER.get();
    }

    public static double getCellarAgingMultiplier() {
        return VineryConfig.CELLAR_AGING_MULTIPLIER.get();
    }

    public static int getGrapevinePotStomps() {
        return VineryConfig.GRAPEVINE_POT_STOMPS.get();
    }

    public static int getGrapevinePotHeavyArmorBonus() {
        return VineryConfig.GRAPEVINE_POT_HEAVY_ARMOR_BONUS.get();
    }

    public static boolean shouldShowGrapevinePotSplash() {
        return VineryConfig.GRAPEVINE_POT_SPLASH_PARTICLES.get();
    }

    public static boolean shouldGrapevineLeavesGrow() {
        return VineryConfig.GRAPEVINE_LEAVES_ENABLED.get();
    }

    public static int getWineMaxLevel() {
        return VineryConfig.MAX_LEVEL.get();
    }

    public static List<? extends String> getWineEffects() {
        return VineryConfig.EFFECTS.get();
    }

    public static boolean isWineAgingEnabled() {
        return VineryConfig.AGING_ENABLED.get();
    }

    public static boolean shouldWineAgeOnlyInStorage() {
        return VineryConfig.AGE_ONLY_IN_STORAGE.get();
    }

    public static InfoOverlayMode getInfoOverlayMode() {
        return VineryConfig.INFO_OVERLAY_MODE.get();
    }

    public static int getWineDurationPerYear() {
        return VineryConfig.DURATION_PER_YEAR.get();
    }

    public static int getWineDaysPerYear() {
        return VineryConfig.DAYS_PER_YEAR.get();
    }

    public static int getWineYearsPerEffectLevel() {
        return VineryConfig.YEARS_PER_EFFECT_LEVEL.get();
    }

    public static int getWineMaxDuration() {
        return VineryConfig.MAX_DURATION.get();
    }

    public static boolean shouldGiveEffect() {
        return VineryConfig.BANNER_GIVE_EFFECT.get();
    }

    public static boolean shouldShowTooltip() {
        return VineryConfig.BANNER_GIVE_EFFECT.get() && VineryConfig.BANNER_SHOW_TOOLTIP.get();
    }

    public static double getTraderSpawnChance() {
        return VineryConfig.TRADER_SPAWN_CHANCE.get();
    }

    public static boolean shouldSpawnWithMules() {
        return VineryConfig.TRADER_SPAWN_WITH_MULES.get();
    }

    public static int getTraderSpawnDelay() {
        return VineryConfig.TRADER_SPAWN_DELAY.get();
    }

    /** Forge reloads config files on its own when they change. */
    public static boolean reloadConfig() {
        return false;
    }

    public static List<VineryPlugin> getPlugins() {
        return VineryPlugins.find();
    }
}
