// Salt (mortuusars), MIT: a modified copy of the upstream io.github.mortuusars.salt.configuration.Configuration for the
// Creative Delight repack (members/salt/NOTICE-upstream-MIT.txt). The edit is GNU GPL3 like the rest of the pack's code.
// Change against upstream 1.2.4.1 (feature_writeup/salt.md 6 S10, TODO creativedelight/members #52): the COMMON
// settings live in Creative Delight's config, section [salt] of config/creativedelight-common.toml, same sub-sections,
// keys and comments as upstream's salt-common.toml (which Creative Delight carries over once and renames to .moved).
// Creative Delight's CDConfig calls define() while it builds its spec and bind() after, through its
// compat.SaltConfigBridge, and refresh() when its file loads (the config events reach Creative Delight's mod bus, not
// Salt's). Defaults are the pack's: rain dissolving off, Salt's butcher trades off (the pack sells salt itself).
// Without Creative Delight the class builds and registers its own salt-common.toml as upstream did.
// CLIENT (salt-client.toml: the salted overlay, JEI categories) is unchanged: Creative Delight has no client config.
package io.github.mortuusars.salt.configuration;

import com.mojang.logging.LogUtils;
import io.github.mortuusars.salt.Salting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class Configuration {
    /** Creative Delight's spec once bound (or Salt's own without Creative Delight). */
    public static ForgeConfigSpec COMMON;
    public static final ForgeConfigSpec CLIENT;

    public static ForgeConfigSpec.IntValue SALTING_ADDITIONAL_NUTRITION;
    public static ForgeConfigSpec.DoubleValue SALTING_ADDITIONAL_SATURATION_MODIFIER;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SALTING_INDIVIDUAL_VALUES;
    public static Map<String, Salting.FoodValue> FOOD_VALUES = new HashMap<>();
    public static ForgeConfigSpec.BooleanValue DISSOLVING_ENABLED;
    public static ForgeConfigSpec.DoubleValue DISSOLVING_CHANCE;
    public static ForgeConfigSpec.BooleanValue DISSOLVING_FLUID_SOURCE_CONVERSION;
    public static ForgeConfigSpec.BooleanValue DISSOLVING_IN_RAIN;
    public static ForgeConfigSpec.DoubleValue DISSOLVING_IN_RAIN_CHANCE;
    public static ForgeConfigSpec.BooleanValue MELTING_ITEM_ENABLED;
    public static ForgeConfigSpec.BooleanValue MELTING_BY_BLOCK_ENABLED;
    public static ForgeConfigSpec.DoubleValue MELTING_BLOCK_CHANCE;
    public static ForgeConfigSpec.BooleanValue MELTING_PLACES_WATER;
    public static ForgeConfigSpec.BooleanValue EVAPORATION_ENABLED;
    public static ForgeConfigSpec.DoubleValue EVAPORATION_CHANCE;
    public static ForgeConfigSpec.BooleanValue SALT_CLUSTER_GROWING_ENABLED;
    public static ForgeConfigSpec.DoubleValue SALT_CLUSTER_GROWING_CHANCE;
    public static ForgeConfigSpec.IntValue ROCK_SALT_SIZE;
    public static ForgeConfigSpec.DoubleValue ROCK_SALT_CLUSTER_CHANCE;
    public static ForgeConfigSpec.BooleanValue BUTCHER_SALT_TRADES_ENABLED;
    public static ForgeConfigSpec.BooleanValue SALTED_OVERLAY_ENABLED;
    public static ForgeConfigSpec.BooleanValue JEI_SALT_EVAPORATION_ENABLED;
    public static ForgeConfigSpec.BooleanValue JEI_SALT_CRYSTAL_GROWING_ENABLED;

    /** True when Creative Delight is absent and this class registers salt-common.toml itself. */
    private static boolean standalone;

    public static void init() {
        if (standalone) ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT);
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        updateIndividualFoodValues();
    }

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        updateIndividualFoodValues();
    }

    /** Creative Delight: its file loaded or reloaded, so reread SaltingFoodValues. */
    public static void refresh() {
        updateIndividualFoodValues();
    }

    /** Creative Delight: the spec the values were defined in, once built. */
    public static void bind(ForgeConfigSpec spec) {
        COMMON = spec;
    }

    private static void updateIndividualFoodValues() {
        FOOD_VALUES.clear();
        if (COMMON == null || !COMMON.isLoaded()) return;
        for (String entry : SALTING_INDIVIDUAL_VALUES.get()) {
            try {
                String[] parts = entry.split(",");
                String itemPath = parts.length >= 1 ? parts[0] : null;
                Integer nutrition = parts.length >= 2 ? Integer.parseInt(parts[1].trim()) : null;
                Float saturationMod = parts.length >= 3 ? Float.parseFloat(parts[2].trim()) : null;
                if (itemPath != null && nutrition != null) {
                    FOOD_VALUES.put(itemPath, new Salting.FoodValue(nutrition,
                            saturationMod != null ? saturationMod : SALTING_ADDITIONAL_SATURATION_MODIFIER.get().floatValue()));
                }
            } catch (Throwable t) {
                LogUtils.getLogger().error("Failed to parse food value '" + entry + "': " + t);
            }
        }
    }

    /**
     * The COMMON values, upstream's sub-sections and keys, into {@code builder} at its current level (Creative Delight
     * pushes {@code salt} first).
     */
    public static void define(ForgeConfigSpec.Builder builder) {
        builder.comment("Salting foods to add extra nutrition. Can be disabled by removing 'salt:salting' recipe or removing all items from 'salt/tags/items/can_be_salted' tag.")
                .push("Salting");
        SALTING_ADDITIONAL_NUTRITION = builder.comment(
                        "Amount of additional nutrition that salted food provides. 1 nutrition = half of hunger shank.",
                        "Negative values will decrease hunger restored.")
                .defineInRange("SaltingNutrition", 2, -20, 20);
        SALTING_ADDITIONAL_SATURATION_MODIFIER = builder.comment("Amount of additional saturation modifier to apply when food is eaten.")
                .defineInRange("SaltingSaturationModifier", 0.1F, -10.0, 10.0);
        SALTING_INDIVIDUAL_VALUES = builder.comment(
                        "Additional nutrition and saturation values per food. Overrides default values (defined above).",
                        "Format: itemRegistryName,nutrition,[saturationModifier].",
                        "Separated by commas. Saturation is optional (will use default value if not specified)")
                .defineList("SaltingFoodValues", List.of("minecraft:rotten_flesh,1,0.05"), o -> true);
        builder.pop();

        builder.push("Dissolving");
        DISSOLVING_ENABLED = builder.comment("Salt blocks will dissolve if adjacent to blocks defined in the tag 'salt/tags/blocks/salt_dissolvables'")
                .define("SaltDissolvingEnabled", true);
        DISSOLVING_CHANCE = builder.comment("Chance of water dissolving in fluid on random tick. 1.0 = first random tick. 0.0 = never.")
                .defineInRange("SaltDissolvingChance", 0.35, 0.0, 1.0);
        DISSOLVING_FLUID_SOURCE_CONVERSION = builder.comment("If dissolved by a fluid source block - salt will convert to a fluid source block instead of air.")
                .define("SaltDissolvesIntoSourceBlocks", true);
        DISSOLVING_IN_RAIN = builder.comment("Salt blocks will dissolve when exposed to rain. Pack default: off.")
                .define("SaltDissolvingInRain", false);
        DISSOLVING_IN_RAIN_CHANCE = builder.comment("Chance of salt blocks dissolving in rain on random tick. 1.0 = first random tick. 0.0 = never.")
                .defineInRange("SaltDissolvingInRainChance", 0.15, 0.0, 1.0);
        builder.pop();

        builder.push("Melting");
        MELTING_ITEM_ENABLED = builder.comment("Salt (item) will melt clicked blocks defined in tag 'salt/tags/blocks/meltables'.")
                .define("SaltItemMeltingEnabled", true);
        MELTING_BY_BLOCK_ENABLED = builder.comment("Salt blocks will melt adjacent blocks defined in tag 'salt/tags/blocks/meltables' on random tick.")
                .define("SaltBlockMeltingEnabled", true);
        MELTING_BLOCK_CHANCE = builder.comment("Chance of block melting on random tick. 1.0 = first random tick. 0.0 = never.")
                .defineInRange("SaltBlockMeltingChance", 0.4, 0.0, 1.0);
        MELTING_PLACES_WATER = builder.comment("Melted ice is replaced with a water source block outside ultra-warm dimensions (the Nether).",
                        "Off: always air. Snow always leaves air.")
                .define("SaltMeltingPlaceWater", true);
        builder.pop();

        builder.push("Evaporation");
        EVAPORATION_ENABLED = builder.comment("Water in a cauldron with a heat source beneath (defined in tag 'salt/tags/blocks/heaters') will evaporate and salt will be formed in the cauldron")
                .define("EvaporationEnabled", true);
        EVAPORATION_CHANCE = builder.comment("Chance of water evaporating on random tick. 1.0 = first random tick. 0.0 = never.")
                .defineInRange("EvaporationChance", 0.3, 0.0, 1.0);
        builder.pop();

        builder.push("GrowingSaltClusters");
        SALT_CLUSTER_GROWING_ENABLED = builder.comment("Water dripping from a Pointed Dripstone on a blocks tagged as 'salt/tags/blocks/salt_cluster_growables' will grow Salt Clusters")
                .define("SaltClusterGrowingEnabled", true);
        SALT_CLUSTER_GROWING_CHANCE = builder.comment("Chance of cluster growing by one stage on random tick. 1.0 = first random tick. 0.0 = never.")
                .defineInRange("SaltClusterGrowingChance", 0.1, 0.0, 1.0);
        builder.pop();

        builder.comment("Rock Salt Deposits will generate in biomes defined in tag 'salt/tags/worldgen/biome/has_rock_salt_deposits'",
                        "Since 1.19 - parts of the Rock Salt generation is defined in jsons.",
                        "To enable/disable the generation or configure the generation chances you'll need to create a datapack.",
                        "Enable/disable generation - 'salt/worldgen/biome_modifier/add_rock_salt_deposit.json' - {\"type\": \"forge:none\"} will disable the generation.",
                        "Changing generation chances - 'salt/worldgen/placed_feature/mineral_rock_salt.json'")
                .push("RockSalt");
        ROCK_SALT_SIZE = builder.comment("Size of the Rock Salt deposit").defineInRange("RockSaltSize", 24, 1, 64);
        ROCK_SALT_CLUSTER_CHANCE = builder.comment("Chance of the Salt Clusters generating on the deposits (per side)")
                .defineInRange("RockSaltClusterChance", 0.15F, 0.0, 1.0);
        builder.pop();

        builder.push("VillagerTrades");
        BUTCHER_SALT_TRADES_ENABLED = builder.comment("Butcher will buy and sell Salt (Salt's own trades). Pack default: off, the",
                        "pack's own butcher trade sells salt.")
                .define("ButcherSaltTradesEnabled", false);
        builder.pop();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("JEI/REI");
        JEI_SALT_EVAPORATION_ENABLED = builder.comment("Water Evaporation category will be shown in JEI/REI. ",
                        "*Will be disabled automatically if evaporation is disabled or salt:heaters tag has no blocks.")
                .define("JeiWaterEvaporationEnabled", true);
        JEI_SALT_CRYSTAL_GROWING_ENABLED = builder.comment("Growing Salt Crystals category will be shown in JEI/REI.",
                        "*Will be disabled automatically if Growing Crystals is disabled.")
                .define("JeiGrowingSaltCrystalsEnabled", true);
        builder.pop();
        builder.push("Compatibility");
        SALTED_OVERLAY_ENABLED = builder.comment("Overlay texture will be drawn over salted foods. Disable if there's an issue with rendering items.")
                .define("SaltingOverlayEnabled", true);
        builder.pop();
        CLIENT = builder.build();

        // Last: loading Creative Delight's config class defines the COMMON values (re-entrant if CDConfig is the one
        // loading this class: then it returns at once and CDConfig calls define() itself).
        try {
            Class.forName("com.SaintAlfonzo.creativedelight.CDConfig", true, Configuration.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            standalone = true;
            ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
            define(common);
            COMMON = common.build();
        }
    }
}
