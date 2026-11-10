package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import com.SaintAlfonzo.creativedelight.hearth.fluid.HHFluids;
import com.SaintAlfonzo.creativedelight.hearth.gear.FarmersHatItem;
import com.SaintAlfonzo.creativedelight.hearth.gear.FertilizerBagItem;
import com.SaintAlfonzo.creativedelight.hearth.gear.ForeignPlantItem;
import com.SaintAlfonzo.creativedelight.hearth.gear.HorseshoeItem;
import com.SaintAlfonzo.creativedelight.hearth.gear.SugarHorseTreatItem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.DrinkableItem;
import vectorwing.farmersdelight.common.item.FuelBlockItem;
import vectorwing.farmersdelight.common.item.MilkBottleItem;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModMaterials;

/**
 * Items, ids as 1.4.1, in Farmer's Delight's creative tab (ModItems.basicItem(), as the other members). With Berry Good
 * loaded 1.4.1 makes the Raspberry plain food and the Raspberry Pips the bush's planting item; the pack always has
 * Berry Good, so only that form exists here.
 */
public final class HHItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HearthAndHarvestMod.MODID);

    /** 1.4.1 HHFoodValues.RASPBERRY: 2 hunger, 0.4 saturation modifier, eaten fast. */
    public static final FoodProperties RASPBERRY_FOOD = new FoodProperties.Builder().nutrition(2).saturationMod(0.4F).fast().build();

    public static final RegistryObject<Item> RASPBERRY = ITEMS.register("raspberry",
            () -> new Item(ModItems.basicItem().food(RASPBERRY_FOOD)));
    public static final RegistryObject<Item> RASPBERRY_PIPS = ITEMS.register("raspberry_pips",
            () -> new ItemNameBlockItem(HHBlocks.RASPBERRY_BUSH.get(), ModItems.basicItem()));

    // Foods (hearth_and_harvest.md 3.1; values in HHFoods).
    public static final RegistryObject<Item> SUNFLOWER_SEEDS = ITEMS.register("sunflower_seeds",
            () -> new SunflowerSeedItem(ModItems.foodItem(HHFoods.SUNFLOWER_SEEDS)));
    public static final RegistryObject<Item> RAISINS = consumable("raisins", HHFoods.RAISINS);
    public static final RegistryObject<Item> RAISIN_COOKIE = consumable("raisin_cookie", HHFoods.RAISIN_COOKIE);
    public static final RegistryObject<Item> PEANUT_BUTTER_COOKIE = consumable("peanut_butter_cookie", HHFoods.PEANUT_BUTTER_COOKIE);
    public static final RegistryObject<Item> CROISSANT = consumable("croissant", HHFoods.CROISSANT);
    public static final RegistryObject<Item> RASPBERRY_SCONE = consumable("raspberry_scone", HHFoods.RASPBERRY_SCONE);
    public static final RegistryObject<Item> CIDER_DONUT = consumable("cider_donut", HHFoods.CIDER_DONUT);
    public static final RegistryObject<Item> BAKED_APPLE = consumable("baked_apple", HHFoods.BAKED_APPLE);
    public static final RegistryObject<Item> CANDY_CORN = consumable("candy_corn", HHFoods.CANDY_CORN);
    public static final RegistryObject<Item> RASPBERRY_PIE_SLICE = consumable("raspberry_pie_slice", HHFoods.PIE_SLICE);
    public static final RegistryObject<Item> GRAPE_PIE_SLICE = consumable("grape_pie_slice", HHFoods.PIE_SLICE);
    public static final RegistryObject<Item> PEANUT_BUTTER_PIE_SLICE = consumable("peanut_butter_pie_slice", HHFoods.PIE_SLICE);
    public static final RegistryObject<Item> GOAT_CHEESE_SLICE = consumable("goat_cheese_slice", HHFoods.GOAT_CHEESE_SLICE);
    public static final RegistryObject<Item> RAW_SAUSAGE = consumable("raw_sausage", HHFoods.RAW_SAUSAGE);
    public static final RegistryObject<Item> COOKED_SAUSAGE = consumable("cooked_sausage", HHFoods.COOKED_SAUSAGE);
    public static final RegistryObject<Item> HOT_DOG = consumable("hot_dog", HHFoods.HOT_DOG);
    public static final RegistryObject<Item> TAMALE = consumable("tamale", HHFoods.TAMALE);
    public static final RegistryObject<Item> STREET_CORN = consumable("street_corn", HHFoods.STREET_CORN);
    public static final RegistryObject<Item> TRAIL_MIX = consumable("trail_mix", HHFoods.TRAIL_MIX);
    public static final RegistryObject<Item> CHARRED_MARSHMALLOW_STICK = ITEMS.register("charred_marshmallow_stick",
            () -> new ConsumableItem(ModItems.foodItem(HHFoods.CHARRED_MARSHMALLOW).craftRemainder(Items.STICK)));
    public static final RegistryObject<Item> BISCUITS_AND_GRAVY = bowl("biscuits_and_gravy", HHFoods.BISCUITS_AND_GRAVY);
    public static final RegistryObject<Item> MACARONI_AND_CHEESE = bowl("macaroni_and_cheese", HHFoods.MACARONI_AND_CHEESE);
    public static final RegistryObject<Item> GLAZED_CARROTS = bowl("glazed_carrots", HHFoods.GLAZED_CARROTS);
    public static final RegistryObject<Item> HOPS_SALAD = bowl("hops_salad", HHFoods.HOPS_SALAD);
    public static final RegistryObject<Item> PICKLED_CARROTS = pickle("pickled_carrots", HHFoods.PICKLED_CARROTS);
    public static final RegistryObject<Item> PICKLED_POTATOES = pickle("pickled_potatoes", HHFoods.PICKLED_POTATOES);

    // Ingredients and crops
    public static final RegistryObject<Item> HOPS = plain("hops");
    public static final RegistryObject<Item> CORN_HUSK = plain("corn_husk");
    public static final RegistryObject<Item> CROW_FEATHER = plain("crow_feather");
    public static final RegistryObject<Item> BUTTER = plain("butter");
    public static final RegistryObject<Item> MANURE = ITEMS.register("manure", () -> new ManureItem(ModItems.basicItem()));
    public static final RegistryObject<Item> WET_MANURE_BRICK = plain("wet_manure_brick");
    public static final RegistryObject<Item> MANURE_BRICK = plain("manure_brick");
    public static final RegistryObject<Item> BRINE_BOTTLE = ITEMS.register("brine_bottle",
            () -> new Item(ModItems.basicItem().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));

    // Drinks: one glass per bottle, upstream's four glasses are dropped (D16).
    public static final RegistryObject<Item> CHOCOLATE_MILK_BOTTLE = ITEMS.register("chocolate_milk_bottle",
            () -> new MilkBottleItem(ModItems.drinkItem().food(HHFoods.CHOCOLATE_MILK)));
    public static final RegistryObject<Item> GOAT_MILK_BOTTLE = ITEMS.register("goat_milk_bottle",
            () -> new MilkBottleItem(ModItems.drinkItem().food(HHFoods.GOAT_MILK)));
    public static final RegistryObject<Item> BLUEBERRY_JUICE = drink("blueberry_juice", HHFoods.BLUEBERRY_JUICE);
    public static final RegistryObject<Item> RASPBERRY_JUICE = drink("raspberry_juice", HHFoods.RASPBERRY_JUICE);
    public static final RegistryObject<Item> CHERRY_JUICE = drink("cherry_juice", HHFoods.CHERRY_JUICE);
    public static final RegistryObject<Item> SWEET_BERRY_JUICE = drink("sweet_berry_juice", HHFoods.SWEET_BERRY_JUICE);
    public static final RegistryObject<Item> GLOW_BERRY_JUICE = drink("glow_berry_juice", HHFoods.GLOW_BERRY_JUICE);
    public static final RegistryObject<Item> BLUEBERRY_WINE = drink("blueberry_wine", HHFoods.BLUEBERRY_WINE);
    public static final RegistryObject<Item> RASPBERRY_WINE = drink("raspberry_wine", HHFoods.RASPBERRY_WINE);
    public static final RegistryObject<Item> SWEET_BERRY_WINE = drink("sweet_berry_wine", HHFoods.SWEET_BERRY_WINE);
    public static final RegistryObject<Item> GLOW_BERRY_WINE = drink("glow_berry_wine", HHFoods.GLOW_BERRY_WINE);
    public static final RegistryObject<Item> MELON_WINE = drink("melon_wine", HHFoods.MELON_WINE);
    public static final RegistryObject<Item> LEMON_WINE = drink("lemon_wine", HHFoods.LEMON_WINE);
    public static final RegistryObject<Item> ROOT_BEER = drink("root_beer", HHFoods.ROOT_BEER);
    public static final RegistryObject<Item> MOONSHINE = drink("moonshine", HHFoods.MOONSHINE);

    // Buckets. The fluids are referenced lazily: HHFluids reads these fields in its own static init.
    public static final RegistryObject<Item> GOAT_MILK_BUCKET = ITEMS.register("goat_milk_bucket",
            () -> new GoatMilkBucketItem(() -> HHFluids.GOAT_MILK.source.get(), bucketProps()));
    public static final RegistryObject<Item> BRINE_BUCKET = ITEMS.register("brine_bucket",
            () -> new CarrierBucketItem(() -> HHFluids.BRINE.source.get(), bucketProps()));
    public static final RegistryObject<Item> SAP_BUCKET = ITEMS.register("sap_bucket",
            () -> new CarrierBucketItem(() -> HHFluids.SAP.source.get(), bucketProps()));

    // Block items
    public static final RegistryObject<Item> RASPBERRY_PIE = block("raspberry_pie", HHBlocks.RASPBERRY_PIE);
    public static final RegistryObject<Item> GRAPE_PIE = block("grape_pie", HHBlocks.GRAPE_PIE);
    public static final RegistryObject<Item> PEANUT_BUTTER_PIE = block("peanut_butter_pie", HHBlocks.PEANUT_BUTTER_PIE);
    public static final RegistryObject<Item> WAFFLE = ITEMS.register("waffle",
            () -> new BlockItem(HHBlocks.WAFFLE.get(), ModItems.foodItem(HHFoods.WAFFLE)));
    public static final RegistryObject<Item> GOAT_CHEESE_WHEEL = block("goat_cheese_wheel", HHBlocks.GOAT_CHEESE_WHEEL);
    public static final RegistryObject<Item> UNRIPE_GOAT_CHEESE_WHEEL = block("unripe_goat_cheese_wheel", HHBlocks.UNRIPE_GOAT_CHEESE_WHEEL);
    public static final RegistryObject<Item> HAY_RUG = block("hay_rug", HHBlocks.HAY_RUG);
    public static final RegistryObject<Item> STRAW_RUG = block("straw_rug", HHBlocks.STRAW_RUG);
    public static final RegistryObject<Item> HOPS_BAG = block("hops_bag", HHBlocks.HOPS_BAG);
    public static final RegistryObject<Item> MANURE_BAG = block("manure_bag", HHBlocks.MANURE_BAG);
    public static final RegistryObject<Item> MANURE_BLOCK = block("manure_block", HHBlocks.MANURE_BLOCK);
    public static final RegistryObject<Item> MANURE_BRICKS = block("manure_bricks", HHBlocks.MANURE_BRICKS);
    public static final RegistryObject<Item> MANURE_BRICK_SLAB = block("manure_brick_slab", HHBlocks.MANURE_BRICK_SLAB);
    public static final RegistryObject<Item> MANURE_BRICK_STAIRS = block("manure_brick_stairs", HHBlocks.MANURE_BRICK_STAIRS);
    public static final RegistryObject<Item> MANURE_BRICK_WALL = block("manure_brick_wall", HHBlocks.MANURE_BRICK_WALL);
    public static final RegistryObject<Item> POLISHED_MANURE = block("polished_manure", HHBlocks.POLISHED_MANURE);
    public static final RegistryObject<Item> POLISHED_MANURE_SLAB = block("polished_manure_slab", HHBlocks.POLISHED_MANURE_SLAB);
    public static final RegistryObject<Item> POLISHED_MANURE_STAIRS = block("polished_manure_stairs", HHBlocks.POLISHED_MANURE_STAIRS);
    public static final RegistryObject<Item> POLISHED_MANURE_WALL = block("polished_manure_wall", HHBlocks.POLISHED_MANURE_WALL);
    public static final RegistryObject<Item> CORN_HUSK_BUNDLE = ITEMS.register("corn_husk_bundle",
            () -> new FuelBlockItem(HHBlocks.CORN_HUSK_BUNDLE.get(), ModItems.basicItem(), 4000));
    public static final RegistryObject<Item> ROPE_COIL = block("rope_coil", HHBlocks.ROPE_COIL);
    public static final RegistryObject<Item> SPOOL = block("spool", HHBlocks.SPOOL);
    public static final RegistryObject<Item> CRIMSON_FUNGUS_CRATE = block("crimson_fungus_crate", HHBlocks.CRIMSON_FUNGUS_CRATE);
    public static final RegistryObject<Item> WARPED_FUNGUS_CRATE = block("warped_fungus_crate", HHBlocks.WARPED_FUNGUS_CRATE);
    public static final RegistryObject<Item> GLISTERING_MELON_CRATE = block("glistering_melon_crate", HHBlocks.GLISTERING_MELON_CRATE);
    public static final RegistryObject<Item> POISONOUS_POTATO_CRATE = block("poisonous_potato_crate", HHBlocks.POISONOUS_POTATO_CRATE);
    public static final RegistryObject<Item> ROTTEN_TOMATO_CRATE = block("rotten_tomato_crate", HHBlocks.ROTTEN_TOMATO_CRATE);
    public static final RegistryObject<Item> WILD_HOPS = block("wild_hops", HHBlocks.WILD_HOPS);
    public static final RegistryObject<Item> TREE_TAPPER = block("tree_tapper", HHBlocks.TREE_TAPPER);

    // Tools (3.1). Cleavers are FD knives; the netherite one is a smithing upgrade.
    public static final RegistryObject<Item> FLINT_CLEAVER = cleaver("flint_cleaver", ModMaterials.FLINT, false);
    public static final RegistryObject<Item> IRON_CLEAVER = cleaver("iron_cleaver", Tiers.IRON, false);
    public static final RegistryObject<Item> GOLDEN_CLEAVER = cleaver("golden_cleaver", Tiers.GOLD, false);
    public static final RegistryObject<Item> DIAMOND_CLEAVER = cleaver("diamond_cleaver", Tiers.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_CLEAVER = cleaver("netherite_cleaver", Tiers.NETHERITE, true);
    public static final RegistryObject<Item> PITCHFORK = ITEMS.register("pitchfork", () -> new PitchforkItem(ModItems.basicItem().durability(250)));

    public static final Map<String, RegistryObject<Item>> MUMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> HALF_CABINETS = new LinkedHashMap<>();

    static {
        HHBlocks.MUMS.forEach((id, mum) -> MUMS.put(id, block(id, mum)));
        HHBlocks.HALF_CABINETS.forEach((id, cabinet) -> HALF_CABINETS.put(id, id.startsWith("crimson") || id.startsWith("warped")
                ? block(id, cabinet)
                : ITEMS.register(id, () -> new FuelBlockItem(cabinet.get(), ModItems.basicItem(), 300))));
    }

    // Gear tranche (4.6)
    public static final RegistryObject<Item> HORSESHOE = ITEMS.register("horseshoe", () -> new HorseshoeItem(ModItems.basicItem().stacksTo(16)));
    public static final RegistryObject<Item> SUGAR_HORSE_TREAT = ITEMS.register("sugar_horse_treat", () -> new SugarHorseTreatItem(ModItems.basicItem()));
    public static final RegistryObject<Item> FERTILIZER_BAG = ITEMS.register("fertilizer_bag", () -> new FertilizerBagItem(ModItems.basicItem().durability(8)));
    public static final RegistryObject<Item> FARMERS_HAT = ITEMS.register("farmers_hat", () -> new FarmersHatItem(ModItems.basicItem()));
    public static final RegistryObject<Item> BLUEBERRY_PIPS = ITEMS.register("blueberry_pips", () -> new ForeignPlantItem(new net.minecraft.resources.ResourceLocation("fruitsdelight", "blueberry_bush"), ModItems.basicItem()));
    public static final RegistryObject<Item> TILLED_END_STONE = block("tilled_end_stone", HHBlocks.TILLED_END_STONE);

    private static RegistryObject<Item> cleaver(String id, Tier tier, boolean fireResistant) {
        return ITEMS.register(id, () -> {
            Item.Properties props = ModItems.basicItem();
            if (fireResistant) props.fireResistant();
            return new CleaverItem(tier, props);
        });
    }

    private static RegistryObject<Item> plain(String id) {
        return ITEMS.register(id, () -> new Item(ModItems.basicItem()));
    }

    private static RegistryObject<Item> consumable(String id, FoodProperties food) {
        return ITEMS.register(id, () -> new ConsumableItem(ModItems.foodItem(food)));
    }

    private static RegistryObject<Item> bowl(String id, FoodProperties food) {
        return ITEMS.register(id, () -> new ConsumableItem(ModItems.bowlFoodItem(food)));
    }

    private static RegistryObject<Item> pickle(String id, FoodProperties food) {
        return ITEMS.register(id, () -> new LazyJarFoodItem(ModItems.foodItem(food)));
    }

    private static RegistryObject<Item> drink(String id, FoodProperties food) {
        return ITEMS.register(id, () -> new DrinkableItem(ModItems.drinkItem().food(food), true, false));
    }

    private static Item.Properties bucketProps() {
        return ModItems.basicItem().craftRemainder(Items.BUCKET).stacksTo(1);
    }

    private static RegistryObject<Item> block(String id, Supplier<Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), ModItems.basicItem()));
    }

    // Watering cans (hearth_and_harvest.md 4.0): copper keeps 1.4.1's id; the higher tiers are pack additions. Tools tab.
    public static final RegistryObject<Item> WATERING_CAN = can("watering_can", WateringCanTier.COPPER, false);
    public static final RegistryObject<Item> IRON_WATERING_CAN = can("iron_watering_can", WateringCanTier.IRON, false);
    public static final RegistryObject<Item> GOLDEN_WATERING_CAN = can("golden_watering_can", WateringCanTier.GOLD, false);
    public static final RegistryObject<Item> DIAMOND_WATERING_CAN = can("diamond_watering_can", WateringCanTier.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_WATERING_CAN = can("netherite_watering_can", WateringCanTier.NETHERITE, true);
    public static final RegistryObject<Item> DRAGON_WATERING_CAN = can("dragon_watering_can", WateringCanTier.DRAGON, true);

    private static RegistryObject<Item> can(String id, WateringCanTier tier, boolean fireResistant) {
        return ITEMS.register(id, () -> {
            Item.Properties props = new Item.Properties().tab(CreativeModeTab.TAB_TOOLS);
            if (fireResistant) props.fireResistant();
            return new WateringCanItem(tier, props);
        });
    }

    private HHItems() {
    }
}
