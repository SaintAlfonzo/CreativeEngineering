package com.SaintAlfonzo.creativedelight.oceanic.registry;

import com.SaintAlfonzo.creativedelight.oceanic.OceanicDelightMod;
import com.SaintAlfonzo.creativedelight.oceanic.content.FishingNetItem;
import com.SaintAlfonzo.creativedelight.oceanic.content.OceanicFood;
import com.SaintAlfonzo.creativedelight.oceanic.content.SeaGrapeItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The 47 Oceanic Delight items, upstream ids, in upstream order (the creative tab lists them in registration order).
 * Food values, stack sizes, rarities and eating times are upstream's (ocean_delight.md 5.6); the pack's own tuning
 * (stack-size policy, food tuning) applies on top as it did to the repack.
 */
public final class OceanicItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, OceanicDelightMod.MODID);

    private static final Supplier<Item> BOWL = () -> Items.BOWL;
    private static final Supplier<MobEffect> COMFORT = OceanicFood.farmers("comfort");
    private static final Supplier<MobEffect> NOURISHMENT = OceanicFood.farmers("nourishment");
    private static final Supplier<MobEffect> BREATH = OceanicEffects.INSTANT_BREATH;
    private static final int THREE_MIN = 3600;
    private static final int FIVE_MIN = 6000;

    public static final RegistryObject<Item> SALMON_EGGS = food("salmon_eggs", 64, Rarity.COMMON, 3, 0.8F, f -> f, b -> b);
    public static final RegistryObject<Item> SEA_PICKLE_CRATE = block("sea_pickle_crate", OceanicBlocks.SEA_PICKLE_CRATE, 64, true);
    public static final RegistryObject<Item> SPONGE_CAKE = block("sponge_cake", OceanicBlocks.SPONGE_CAKE, 64, true);
    public static final RegistryObject<Item> SPONGE_CAKE_SLICE = food("sponge_cake_slice", 64, Rarity.COMMON, 3, 0.1F,
            f -> f.effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0), 1.0F), b -> b.useTicks(16));
    public static final RegistryObject<Item> CAVIAR_TOAST = food("caviar_toast", 64, Rarity.COMMON, 4, 0.9F, f -> f, b -> b);
    public static final RegistryObject<Item> FISH_EGG_ROLL = food("fish_egg_roll", 64, Rarity.COMMON, 11, 0.9F, f -> f, b -> b.useTicks(64));
    public static final RegistryObject<Item> FISH_EGG_ROLL_SLICE = food("fish_egg_roll_slice", 64, Rarity.COMMON, 5, 0.8F, f -> f, b -> b.useTicks(16));
    public static final RegistryObject<Item> SEA_PICKLE_SLICES = food("sea_pickle_slices", 64, Rarity.COMMON, 3, 0.2F, f -> f, b -> b.useTicks(16));
    public static final RegistryObject<Item> FISH_EGG_NOODLE_SOUP = food("fish_egg_noodle_soup", 16, Rarity.COMMON, 12, 0.9F, f -> f,
            b -> b.container(BOWL).bonus(COMFORT, FIVE_MIN));
    public static final RegistryObject<Item> ELDER_GUARDIAN_EYE = food("elder_guardian_eye", 64, Rarity.UNCOMMON, 10, 0.3F, f -> f, b -> b);
    public static final RegistryObject<Item> GLOBULAR_RICE = food("globular_rice", 16, Rarity.UNCOMMON, 15, 0.9F, f -> f,
            b -> b.container(BOWL).bonus(COMFORT, FIVE_MIN));
    public static final RegistryObject<Item> PASTA_WITH_EYEBALL = food("pasta_with_eyeball", 16, Rarity.UNCOMMON, 15, 1.1F, f -> f,
            b -> b.container(BOWL).bonus(NOURISHMENT, FIVE_MIN));
    public static final RegistryObject<Item> NAUTILUS_JUICE = food("nautilus_juice", 16, Rarity.COMMON, 0, 0.0F,
            f -> f.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 1.0F),
            b -> b.container(() -> Items.NAUTILUS_SHELL).drink().shows(() -> MobEffects.DAMAGE_RESISTANCE, 1200));
    public static final RegistryObject<Item> STEAMED_BEEF = food("steamed_beef", 64, Rarity.COMMON, 7, 0.5F, f -> f, b -> b);
    public static final RegistryObject<Item> EGG_ROLL = food("egg_roll", 64, Rarity.COMMON, 8, 0.5F, f -> f, b -> b);
    public static final RegistryObject<Item> POTATO_SALAD = food("potato_salad", 16, Rarity.COMMON, 8, 0.6F, f -> f, b -> b.container(BOWL));
    public static final RegistryObject<Item> CAESAR_SALAD = food("caesar_salad", 16, Rarity.COMMON, 9, 0.8F, f -> f, b -> b.container(BOWL));
    public static final RegistryObject<Item> SEA_SALAD = food("sea_salad", 16, Rarity.COMMON, 7, 0.2F, f -> f, b -> b.container(BOWL));
    public static final RegistryObject<Item> SQUID_TENTACLES = food("squid_tentacles", 64, Rarity.COMMON, 3, 0.5F, f -> f, b -> b);
    public static final RegistryObject<Item> GLOW_SQUID_TENTACLES = food("glow_squid_tentacles", 64, Rarity.COMMON, 3, 0.5F, f -> f, b -> b);
    public static final RegistryObject<Item> GRILLED_SQUID_TENTACLES = food("grilled_squid_tentacles", 64, Rarity.COMMON, 7, 0.7F, f -> f, b -> b);
    public static final RegistryObject<Item> SQUID_AND_PICKLES = food("squid_and_pickles", 16, Rarity.COMMON, 11, 0.7F, f -> f,
            b -> b.container(BOWL).bonus(NOURISHMENT, THREE_MIN));
    public static final RegistryObject<Item> TAKOYAKI = food("takoyaki", 64, Rarity.COMMON, 9, 0.5F, f -> f, b -> b);
    public static final RegistryObject<Item> GLOW_SQUID_STEW = food("glow_squid_stew", 16, Rarity.COMMON, 9, 0.7F, f -> f,
            b -> b.container(BOWL).bonus(COMFORT, THREE_MIN));
    public static final RegistryObject<Item> FISHING_NET = ITEMS.register("fishing_net", () -> new FishingNetItem(new Item.Properties().tab(OceanicTab.TAB)));
    public static final RegistryObject<Item> SHRIMP = food("shrimp", 64, Rarity.COMMON, 4, 0.1F,
            f -> f.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 0.9F), b -> b.useTicks(64));
    public static final RegistryObject<Item> SHRIMP_SLICES = food("shrimp_slices", 64, Rarity.COMMON, 4, 0.5F, f -> f, b -> b.useTicks(16));
    public static final RegistryObject<Item> FRIED_SHRIMP = food("fried_shrimp", 64, Rarity.COMMON, 7, 0.7F, f -> f, b -> b);
    public static final RegistryObject<Item> SEAFOOD_SKEWER = food("seafood_skewer", 64, Rarity.COMMON, 11, 0.7F, f -> f, b -> b.container(() -> Items.STICK));
    public static final RegistryObject<Item> SHRIMP_RICE = food("shrimp_rice", 16, Rarity.COMMON, 14, 0.75F, f -> f,
            b -> b.container(BOWL).bonus(NOURISHMENT, THREE_MIN));
    public static final RegistryObject<Item> SHRIMP_CHIPS = food("shrimp_chips", 64, Rarity.COMMON, 4, 0.3F, f -> f, b -> b.useTicks(16));
    public static final RegistryObject<Item> SHRIMP_ROLL = food("shrimp_roll", 64, Rarity.COMMON, 11, 0.8F, f -> f, b -> b);
    public static final RegistryObject<Item> SHRIMP_STEW = food("shrimp_stew", 16, Rarity.COMMON, 15, 0.9F, f -> f,
            b -> b.container(BOWL).bonus(COMFORT, FIVE_MIN));
    public static final RegistryObject<Item> ANCIENT_FISH_EGGS = food("ancient_fish_eggs", 64, Rarity.RARE, 7, 3.0F,
            f -> f.alwaysEat().effect(() -> new MobEffectInstance(MobEffects.CONDUIT_POWER, 900, 0), 1.0F),
            b -> b.shows(() -> MobEffects.CONDUIT_POWER, 900));
    public static final RegistryObject<Item> PAELLA_PLATE = block("paella_plate", OceanicBlocks.PAELLA_PLATE, 1, true);
    public static final RegistryObject<Item> BOWL_OF_PAELLA = food("bowl_of_paella", 16, Rarity.COMMON, 14, 0.75F, f -> f,
            b -> b.container(BOWL).bonus(NOURISHMENT, FIVE_MIN));
    public static final RegistryObject<Item> STUFFED_PUFFERFISH = block("stuffed_pufferfish", OceanicBlocks.STUFFED_PUFFERFISH, 1, true);
    public static final RegistryObject<Item> PLATE_OF_STUFFED_PUFFERFISH = food("plate_of_stuffed_pufferfish", 16, Rarity.COMMON, 14, 0.75F,
            f -> f.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.3F),
            b -> b.container(BOWL).bonus(NOURISHMENT, FIVE_MIN));
    public static final RegistryObject<Item> SEA_GRAPE = ITEMS.register("sea_grape", () -> new SeaGrapeItem(
            new OceanicFood.Builder(props(64, Rarity.COMMON).food(new FoodProperties.Builder().nutrition(2).saturationMod(0.6F).alwaysEat().build()))
                    .useTicks(16).bonus(BREATH, 1)));
    public static final RegistryObject<Item> SEA_GRAPE_JUICE = food("sea_grape_juice", 16, Rarity.COMMON, 0, 0.0F,
            f -> f.alwaysEat().effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0), 1.0F),
            b -> b.container(() -> Items.GLASS_BOTTLE).drink().bonus(BREATH, 1).shows(() -> MobEffects.WATER_BREATHING, 1200));
    public static final RegistryObject<Item> SQUID_SALAD = food("squid_salad", 16, Rarity.COMMON, 7, 0.9F, f -> f,
            b -> b.container(BOWL).bonus(BREATH, 1));
    public static final RegistryObject<Item> SEA_GRAPE_PLANT = block("sea_grape_plant", OceanicBlocks.SEA_GRAPE_PLANT, 64, false);
    public static final RegistryObject<Item> SEA_GRAPE_FRUIT = block("sea_grape_fruit", OceanicBlocks.SEA_GRAPE_FRUIT, 64, false);
    public static final RegistryObject<Item> SEA_GRAPE_CRATE = block("sea_grape_crate", OceanicBlocks.SEA_GRAPE_CRATE, 64, true);
    public static final RegistryObject<Item> SEA_PICKLE_ROLL = food("sea_pickle_roll", 64, Rarity.COMMON, 13, 0.5F, f -> f, b -> b.useTicks(64));
    public static final RegistryObject<Item> SEA_PICKLE_ROLL_SLICE = food("sea_pickle_roll_slice", 64, Rarity.COMMON, 7, 0.4F, f -> f, b -> b.useTicks(16));
    public static final RegistryObject<Item> WILD_SEA_GRAPE = block("wild_sea_grape", OceanicBlocks.WILD_SEA_GRAPE, 64, true);

    private OceanicItems() {
    }

    private static Item.Properties props(int stack, Rarity rarity) {
        return new Item.Properties().tab(OceanicTab.TAB).stacksTo(stack).rarity(rarity);
    }

    private static RegistryObject<Item> food(String id, int stack, Rarity rarity, int nutrition, float saturation,
                                             Function<FoodProperties.Builder, FoodProperties.Builder> food,
                                             Function<OceanicFood.Builder, OceanicFood.Builder> extra) {
        return ITEMS.register(id, () -> new OceanicFood(extra.apply(new OceanicFood.Builder(props(stack, rarity)
                .food(food.apply(new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation)).build())))));
    }

    /** The two crop blocks have no tab, as upstream. */
    private static RegistryObject<Item> block(String id, RegistryObject<Block> block, int stack, boolean inTab) {
        return ITEMS.register(id, () -> {
            Item.Properties properties = new Item.Properties().stacksTo(stack);
            if (inTab) properties.tab(OceanicTab.TAB);
            return new BlockItem(block.get(), properties);
        });
    }
}
