package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Every Vinery id and hardcoded number the JEI pages use, in one place. Vinery is not a compile
 * dependency: items and the recipe type are looked up by id at runtime, and the aging config is
 * read reflectively, so a missing or renamed Vinery entry drops a page instead of crashing.
 *
 * <p>Numbers are from {@code letsdo-vinery-forge-1.3.10} (decompiled); re-check them on a Vinery update.
 */
final class VineryIds {
    static final String MODID = "vinery";

    static final ResourceLocation FERMENTATION_TYPE = id("wine_fermentation");

    static final ResourceLocation FERMENTATION_BARREL = id("fermentation_barrel");
    static final ResourceLocation WINE_PRESS = id("wine_press");
    static final ResourceLocation GRAPEVINE_POT = id("grapevine_pot");
    static final ResourceLocation WINE_BOTTLE = id("wine_bottle");
    static final ResourceLocation APPLE_MASH = id("apple_mash");

    /** FermentationBarrelBlockEntity.COOKING_TIME_IN_TICKS (also REI's arrow duration). */
    static final int FERMENTATION_TICKS = 1800;
    /** WinePressBlockEntity.maxProgress (also REI's arrow duration). */
    static final int PRESS_TICKS = 72;

    /** GrapevinePotBlock: storage tops out at 6 grapes, which is also when stage reaches 3 (smashable). */
    static final int POT_GRAPES = 6;
    /** GrapevinePotBlock.fallOn: each landing raises stage 3 -> 6. */
    static final int POT_JUMPS = 3;
    /** GrapevinePotBlock.DECREMENT_PER_WINE_BOTTLE: grapes used per juice bottle. */
    static final int GRAPES_PER_BOTTLE = 3;

    /** WineYears.MAX_LEVEL: highest effect amplifier aging can reach. */
    static final int MAX_EFFECT_LEVEL = 5;

    /**
     * GrapevinePotBlock's GrapevineType -> juice switch, as grape item -> juice item. The pot's
     * type is taken from the first grape put in.
     */
    static final Map<ResourceLocation, ResourceLocation> GRAPE_JUICES = new LinkedHashMap<>();

    static {
        GRAPE_JUICES.put(id("red_grape"), id("red_grapejuice_wine_bottle"));
        GRAPE_JUICES.put(id("white_grape"), id("white_grapejuice_wine_bottle"));
        GRAPE_JUICES.put(id("savanna_grapes_red"), id("savanna_red_grapejuice_bottle"));
        GRAPE_JUICES.put(id("savanna_grapes_white"), id("savanna_white_grapejuice_bottle"));
        GRAPE_JUICES.put(id("taiga_grapes_red"), id("taiga_red_grapejuice_bottle"));
        GRAPE_JUICES.put(id("taiga_grapes_white"), id("taiga_white_grapejuice_bottle"));
        GRAPE_JUICES.put(id("jungle_grapes_red"), id("jungle_red_grapejuice_bottle"));
        GRAPE_JUICES.put(id("jungle_grapes_white"), id("jungle_white_grapejuice_bottle"));
    }

    private VineryIds() {
    }

    static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    /** The registered item as a stack, or EMPTY when the id is not registered. */
    static ItemStack stack(ResourceLocation id, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    static ItemStack stack(ResourceLocation id) {
        return stack(id, 1);
    }

    // ---- aging config (config/vinery/config.json5), read the same way WineYears does ----

    private static final int DEFAULT_YEAR_LENGTH_DAYS = 16;
    private static final int DEFAULT_YEARS_PER_EFFECT_LEVEL = 4;

    private static Object configDefault;
    private static Method getConfig;
    private static Method yearLengthInDays;
    private static Method yearsPerEffectLevel;
    private static boolean configFailed;

    /** VineryConfig.yearLengthInDays: in-game days per wine year. */
    static int yearLengthDays() {
        return readConfigInt(true, DEFAULT_YEAR_LENGTH_DAYS);
    }

    /** VineryConfig.yearsPerEffectLevel: years of age per +1 effect level. */
    static int yearsPerEffectLevel() {
        return readConfigInt(false, DEFAULT_YEARS_PER_EFFECT_LEVEL);
    }

    private static int readConfigInt(boolean yearLength, int fallback) {
        if (configFailed) {
            return fallback;
        }
        try {
            if (getConfig == null) {
                Class<?> cls = Class.forName("satisfyu.vinery.config.VineryConfig");
                configDefault = cls.getField("DEFAULT").get(null);
                getConfig = configDefault.getClass().getMethod("getConfig");
                yearLengthInDays = cls.getMethod("yearLengthInDays");
                yearsPerEffectLevel = cls.getMethod("yearsPerEffectLevel");
            }
            Object cfg = getConfig.invoke(configDefault);
            int value = (Integer) (yearLength ? yearLengthInDays : yearsPerEffectLevel).invoke(cfg);
            return value > 0 ? value : fallback;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            configFailed = true;
            return fallback;
        }
    }
}
