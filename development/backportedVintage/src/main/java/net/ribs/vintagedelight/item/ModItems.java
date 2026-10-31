package net.ribs.vintagedelight.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.ModCreativeTab;
import net.ribs.vintagedelight.VintageDelight;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.item.custom.ChefHatItem;
import net.ribs.vintagedelight.item.custom.FuelItem;
import net.ribs.vintagedelight.item.custom.GhostPepperPlantingItem;
import net.ribs.vintagedelight.item.custom.MasonJarItem;
import net.ribs.vintagedelight.item.custom.OrganicMashItem;
import net.ribs.vintagedelight.item.custom.SaltDustItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.MilkBottleItem;

// Upstream's item list less the gearo items and the pickled pitcher pod. The jars follow KOTS 0.3.1: one item per jar,
// registered under its block's id (upstream's separate mason_jar / *_mason_jar items are gone; the empty jar is
// empty_mason_jar and is the container the filled jars leave behind).
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VintageDelight.MODID);

    public static final RegistryObject<Item> SALT_BUCKET = ITEMS.register("salt_bucket", () -> new Item(props().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> OAT = ITEMS.register("oat", () -> new Item(props()));
    public static final RegistryObject<Item> OAT_SEEDS = ITEMS.register("oat_seeds", () -> new ItemNameBlockItem(ModBlocks.OAT_CROP.get(), props()));
    public static final RegistryObject<Item> OAT_DOUGH = ITEMS.register("oat_dough", () -> new Item(props().food(ModFoods.OAT_DOUGH)));
    public static final RegistryObject<Item> RAW_OAT = ITEMS.register("raw_oats", () -> new Item(props().food(ModFoods.OAT)));
    public static final RegistryObject<Item> PEANUT = ITEMS.register("peanut", () -> new ItemNameBlockItem(ModBlocks.PEANUT_CROP.get(), props().food(ModFoods.PEANUT)));
    public static final RegistryObject<Item> ROASTED_PEANUT = ITEMS.register("roasted_peanut", () -> new Item(props().food(ModFoods.ROASTED_PEANUT)));
    public static final RegistryObject<Item> HONEY_ROASTED_PEANUT = ITEMS.register("honey_roasted_peanut", () -> new Item(props().food(ModFoods.HONEY_ROASTED_PEANUT)));
    public static final RegistryObject<Item> GHOST_PEPPER = ITEMS.register("ghost_pepper", () -> new ItemNameBlockItem(ModBlocks.GHOST_PEPPER_CROP.get(), props().food(ModFoods.GHOST_PEPPER)));
    public static final RegistryObject<Item> GHOST_PEPPER_SEEDS = ITEMS.register("ghost_pepper_seeds", () -> new GhostPepperPlantingItem(ModBlocks.GHOST_PEPPER_CROP.get(), props()));
    public static final RegistryObject<Item> PICKLED_PEPPER = ITEMS.register("pickled_pepper", () -> new ConsumableItem(props().food(ModFoods.PICKLED_PEPPER), true));
    public static final RegistryObject<Item> CUCUMBER = ITEMS.register("cucumber", () -> new Item(props().food(ModFoods.CUCUMBER)));
    public static final RegistryObject<Item> CUCUMBER_DOUGH = ITEMS.register("cucumber_dough", () -> new Item(props().food(ModFoods.CUCUMBER_DOUGH)));
    public static final RegistryObject<Item> CUCUMBER_NOODLES = ITEMS.register("cucumber_noodles", () -> new Item(props().food(ModFoods.CUCUMBER_NOODLES)));
    public static final RegistryObject<Item> CUCUMBER_SEEDS = ITEMS.register("cucumber_seeds", () -> new ItemNameBlockItem(ModBlocks.CUCUMBER_CROP.get(), props()));
    public static final RegistryObject<Item> PICKLE = ITEMS.register("pickle", () -> new ConsumableItem(props().food(ModFoods.PICKLE), true));
    public static final RegistryObject<Item> KIMCHI = ITEMS.register("kimchi", () -> new ConsumableItem(props().food(ModFoods.KIMCHI)));
    public static final RegistryObject<Item> PICKLED_ONION = ITEMS.register("pickled_onion", () -> new ConsumableItem(props().food(ModFoods.PICKLED_ONION), true));
    public static final RegistryObject<Item> PICKLED_EGG = ITEMS.register("pickled_egg", () -> new ConsumableItem(props().food(ModFoods.PICKLED_EGG), true));
    public static final RegistryObject<Item> CENTURY_EGG = ITEMS.register("century_egg", () -> new ConsumableItem(props().food(ModFoods.CENTURY_EGG), true));
    public static final RegistryObject<Item> PICKLED_BEETROOT = ITEMS.register("pickled_beetroot", () -> new ConsumableItem(props().food(ModFoods.PICKLED_BEETROOT), true));
    public static final RegistryObject<Item> OVERNIGHT_OATS = ITEMS.register("overnight_oats", () -> new ConsumableItem(bowlFoodItem(ModFoods.OVERNIGHT_OATS), true));
    public static final RegistryObject<Item> OATMEAL = ITEMS.register("oatmeal", () -> new ConsumableItem(bowlFoodItem(ModFoods.OATMEAL), true));
    public static final RegistryObject<Item> OATMEAL_COOKIE = ITEMS.register("oatmeal_cookie", () -> new Item(props().food(ModFoods.OATMEAL_COOKIE)));
    public static final RegistryObject<Item> CHOCOLATE_NUT_GRANOLA_BAR = ITEMS.register("chocolate_nut_granola_bar", () -> new ConsumableItem(basicItem(ModFoods.CHOCOLATE_NUT_GRANOLA_BAR), true));
    public static final RegistryObject<Item> FRUITY_GRANOLA_BAR = ITEMS.register("fruity_granola_bar", () -> new ConsumableItem(basicItem(ModFoods.FRUITY_GRANOLA_BAR), true));
    public static final RegistryObject<Item> DELUXE_GRANOLA_BAR = ITEMS.register("deluxe_granola_bar", () -> new ConsumableItem(basicItem(ModFoods.DELUXE_GRANOLA_BAR), true));

    // Mason jars (placeable, 1 to 4 per block)
    public static final RegistryObject<Item> EMPTY_MASON_JAR = ITEMS.register("empty_mason_jar", () -> new MasonJarItem(ModBlocks.EMPTY_MASON_JAR.get(), props()));
    public static final RegistryObject<Item> RELISH_JAR = ITEMS.register("relish_jar", () -> new MasonJarItem(ModBlocks.RELISH_JAR.get(), jarItem(ModFoods.RELISH)));
    public static final RegistryObject<Item> RELISH_BOTTLE = ITEMS.register("relish_bottle", () -> new ConsumableItem(bottleItem(ModFoods.RELISH_BOTTLE), true));
    public static final RegistryObject<Item> APPLE_SAUCE_JAR = ITEMS.register("apple_sauce_jar", () -> new MasonJarItem(ModBlocks.APPLE_SAUCE_JAR.get(), jarItem(ModFoods.APPLE_SAUCE)));
    public static final RegistryObject<Item> HONEY_JAR = ITEMS.register("honey_jar", () -> new MasonJarItem(ModBlocks.HONEY_JAR.get(), jarItem(ModFoods.HONEY_JAR)));
    public static final RegistryObject<Item> APPLE_SAUCE_BOTTLE = ITEMS.register("apple_sauce_bottle", () -> new ConsumableItem(bottleItem(ModFoods.APPLE_SAUCE_BOTTLE), true));
    public static final RegistryObject<Item> SWEET_BERRY_JAM_JAR = ITEMS.register("sweet_berry_jam_jar", () -> new MasonJarItem(ModBlocks.SWEET_BERRY_JAM_JAR.get(), jarItem(ModFoods.SWEET_BERRY_JAM)));
    public static final RegistryObject<Item> SWEET_BERRY_JAM_BOTTLE = ITEMS.register("sweet_berry_jam_bottle", () -> new ConsumableItem(bottleItem(ModFoods.SWEET_BERRY_JAM_BOTTLE), true));
    public static final RegistryObject<Item> GLOW_BERRY_JAM_JAR = ITEMS.register("glow_berry_jam_jar", () -> new MasonJarItem(ModBlocks.GLOW_BERRY_JAM_JAR.get(), jarItem(ModFoods.GLOW_BERRY_JAM)));
    public static final RegistryObject<Item> GLOW_BERRY_JAM_BOTTLE = ITEMS.register("glow_berry_jam_bottle", () -> new ConsumableItem(bottleItem(ModFoods.GLOW_BERRY_JAM_BOTTLE), true));
    public static final RegistryObject<Item> PEPPER_JAM_JAR = ITEMS.register("pepper_jam_jar", () -> new MasonJarItem(ModBlocks.PEPPER_JAM_JAR.get(), jarItem(ModFoods.PEPPER_JAM)));
    public static final RegistryObject<Item> PEPPER_JAM_BOTTLE = ITEMS.register("pepper_jam_bottle", () -> new ConsumableItem(bottleItem(ModFoods.PEPPER_JAM_BOTTLE), true));
    public static final RegistryObject<Item> NUT_MASH_JAR = ITEMS.register("nut_mash_jar", () -> new MasonJarItem(ModBlocks.NUT_MASH_JAR.get(), jarItem(ModFoods.NUT_MASH)));
    public static final RegistryObject<Item> NUT_MASH_BOTTLE = ITEMS.register("nut_mash_bottle", () -> new ConsumableItem(bottleItem(ModFoods.NUT_MASH_BOTTLE), true));
    public static final RegistryObject<Item> NUT_MILK = ITEMS.register("nut_milk_bottle", () -> new MilkBottleItem(drinkItem(ModFoods.NUT_MILK)));
    public static final RegistryObject<Item> SALT_DUST = ITEMS.register("salt_dust", () -> new SaltDustItem(props()));
    // Pack additions (CreativeEngineering2 dishes/jams #4): mason jar jams, values as the sweet berry jam jar
    public static final RegistryObject<Item> GREEN_GRAPE_JAM_JAR = ITEMS.register("green_grape_jam_jar", () -> new MasonJarItem(ModBlocks.GREEN_GRAPE_JAM_JAR.get(), jarItem(ModFoods.FRUIT_JAM)));
    public static final RegistryObject<Item> RED_GRAPE_JAM_JAR = ITEMS.register("red_grape_jam_jar", () -> new MasonJarItem(ModBlocks.RED_GRAPE_JAM_JAR.get(), jarItem(ModFoods.FRUIT_JAM)));
    public static final RegistryObject<Item> CHERRY_JAM_JAR = ITEMS.register("cherry_jam_jar", () -> new MasonJarItem(ModBlocks.CHERRY_JAM_JAR.get(), jarItem(ModFoods.FRUIT_JAM)));
    public static final RegistryObject<Item> VINEGAR_JAR = ITEMS.register("vinegar_jar", () -> new MasonJarItem(ModBlocks.VINEGAR_JAR.get(), jarItem(ModFoods.VINEGAR_JAR)));
    public static final RegistryObject<Item> VINEGAR_BOTTLE = ITEMS.register("vinegar_bottle", () -> new ConsumableItem(bottleItem(ModFoods.VINEGAR), true));
    public static final RegistryObject<Item> ORGANIC_MASH = ITEMS.register("organic_mash", () -> new OrganicMashItem(props()));
    public static final RegistryObject<Item> CHEESE_CURDS = ITEMS.register("cheese_curds", () -> new Item(props().food(ModFoods.CHEESE_CURDS)));
    public static final RegistryObject<Item> CHEESE_WHEEL = ITEMS.register("cheese_wheel", () -> new BlockItem(ModBlocks.CHEESE_WHEEL.get(), props()));
    public static final RegistryObject<Item> CHEESE_SLICE = ITEMS.register("cheese_slice", () -> new Item(props().food(ModFoods.CHEESE_SLICE)));
    public static final RegistryObject<Item> PICKLE_SOUP = ITEMS.register("pickle_soup", () -> new ConsumableItem(bowlFoodItem(ModFoods.PICKLE_SOUP), true));
    public static final RegistryObject<Item> PB_J = ITEMS.register("pb_j", () -> new Item(props().food(ModFoods.PB_J)));
    public static final RegistryObject<Item> CHEESE_BURGER = ITEMS.register("cheese_burger", () -> new Item(props().food(ModFoods.CHEESEBURGER)));
    public static final RegistryObject<Item> DELUXE_BURGER = ITEMS.register("deluxe_burger", () -> new ConsumableItem(props().food(ModFoods.DELUXE_BURGER), true));
    public static final RegistryObject<Item> CHEESE_PASTA = ITEMS.register("cheese_pasta", () -> new ConsumableItem(bowlFoodItem(ModFoods.CHEESE_PASTA), true));
    public static final RegistryObject<Item> GHOSTLY_CHILI = ITEMS.register("ghostly_chili", () -> new ConsumableItem(bowlFoodItem(ModFoods.GHOSTLY_CHILI), true));
    public static final RegistryObject<Item> PAD_THAI = ITEMS.register("pad_thai", () -> new ConsumableItem(bowlFoodItem(ModFoods.PAD_THAI), true));
    public static final RegistryObject<Item> CUCUMBER_SALAD = ITEMS.register("cucumber_salad", () -> new ConsumableItem(bowlFoodItem(ModFoods.CUCUMBER_SALAD), true));
    public static final RegistryObject<Item> SALTED_COD = ITEMS.register("salted_cod", () -> new Item(props().food(ModFoods.SALTED_COD)));
    public static final RegistryObject<Item> SALTED_SALMON = ITEMS.register("salted_salmon", () -> new Item(props().food(ModFoods.SALTED_SALMON)));
    public static final RegistryObject<Item> SURSTROMMING = ITEMS.register("surstromming", () -> new Item(props().food(ModFoods.SURSTROMMING)));
    public static final RegistryObject<Item> STUFFED_BURRITO = ITEMS.register("stuffed_burrito", () -> new ConsumableItem(props().food(ModFoods.STUFFED_BURRITO), true));
    public static final RegistryObject<Item> CHEESE_PIZZA_SLICE = ITEMS.register("cheese_pizza_slice", () -> new ConsumableItem(props().food(ModFoods.CHEESE_PIZZA_SLICE), true));
    public static final RegistryObject<Item> MEAT_PIZZA_SLICE = ITEMS.register("meat_pizza_slice", () -> new ConsumableItem(props().food(ModFoods.MEAT_PIZZA_SLICE), true));
    public static final RegistryObject<Item> CHEESE_PIZZA = ITEMS.register("cheese_pizza", () -> new BlockItem(ModBlocks.CHEESE_PIZZA.get(), props()));
    public static final RegistryObject<Item> MEAT_PIZZA = ITEMS.register("meat_pizza", () -> new BlockItem(ModBlocks.MEAT_PIZZA.get(), props()));

    public static final RegistryObject<Item> BLACK_CHEF_HAT = hat("black");
    public static final RegistryObject<Item> BLUE_CHEF_HAT = hat("blue");
    public static final RegistryObject<Item> BROWN_CHEF_HAT = hat("brown");
    public static final RegistryObject<Item> CYAN_CHEF_HAT = hat("cyan");
    public static final RegistryObject<Item> GRAY_CHEF_HAT = hat("gray");
    public static final RegistryObject<Item> GREEN_CHEF_HAT = hat("green");
    public static final RegistryObject<Item> LIGHT_BLUE_CHEF_HAT = hat("light_blue");
    public static final RegistryObject<Item> LIGHT_GRAY_CHEF_HAT = hat("light_gray");
    public static final RegistryObject<Item> LIME_CHEF_HAT = hat("lime");
    public static final RegistryObject<Item> MAGENTA_CHEF_HAT = hat("magenta");
    public static final RegistryObject<Item> ORANGE_CHEF_HAT = hat("orange");
    public static final RegistryObject<Item> PINK_CHEF_HAT = hat("pink");
    public static final RegistryObject<Item> PURPLE_CHEF_HAT = hat("purple");
    public static final RegistryObject<Item> RED_CHEF_HAT = hat("red");
    public static final RegistryObject<Item> WHITE_CHEF_HAT = hat("white");
    public static final RegistryObject<Item> YELLOW_CHEF_HAT = hat("yellow");

    public static final RegistryObject<Item> GHOST_CHARCOAL = ITEMS.register("ghost_charcoal", () -> new FuelItem(props(), 1200));

    private static RegistryObject<Item> hat(String colour) {
        return ITEMS.register(colour + "_chefs_hat", () -> new ChefHatItem(props().stacksTo(1)));
    }

    /** Every item of the mod sits in the mod's creative tab (1.19.2 sets the tab on the item). */
    public static Properties props() {
        return new Properties().tab(ModCreativeTab.TAB);
    }

    public static Properties bowlFoodItem(FoodProperties food) {
        return props().food(food).craftRemainder(Items.BOWL).stacksTo(16);
    }

    public static Properties basicItem(FoodProperties food) {
        return props().food(food).stacksTo(64);
    }

    public static Properties drinkItem(FoodProperties food) {
        return props().food(food).craftRemainder(Items.GLASS_BOTTLE).stacksTo(16);
    }

    public static Properties bottleItem(FoodProperties food) {
        return props().food(food).craftRemainder(Items.GLASS_BOTTLE).stacksTo(32);
    }

    public static Properties jarItem(FoodProperties food) {
        // EMPTY_MASON_JAR is registered before the filled jars (same DeferredRegister, declared first), as upstream
        return props().food(food).craftRemainder(EMPTY_MASON_JAR.get()).stacksTo(16);
    }

    /** Upstream's creative tab order (less the purged items); ModCreativeTab adds the rest after it. */
    public static List<Supplier<? extends ItemLike>> tabOrder() {
        List<Supplier<? extends ItemLike>> l = new ArrayList<>();
        l.add(ModBlocks.FERMENTING_JAR); l.add(ModBlocks.CHEESE_MOLD); l.add(ModBlocks.LUSH_GRASS_BLOCK);
        l.add(ModBlocks.MAGIC_VINE); l.add(ModBlocks.STRIPPED_MAGIC_VINE); l.add(ModBlocks.MAGIC_VINE_BLOCK);
        l.add(ModBlocks.STRIPPED_MAGIC_VINE_BLOCK); l.add(ModBlocks.VINE_TILE); l.add(ModBlocks.VINE_TILE_STAIRS);
        l.add(ModBlocks.VINE_TILE_SLAB); l.add(ModBlocks.VINE_NET); l.add(ModBlocks.GOLDEN_EGG); l.add(ModBlocks.OAT_BAG);
        l.add(ModBlocks.OAT_BALE); l.add(ModBlocks.GHOST_PEPPER_CRATE); l.add(ModBlocks.CUCUMBER_CRATE); l.add(ModBlocks.PEANUT_CRATE);
        l.add(ModBlocks.EVAPORATOR); l.add(ModBlocks.SALT_BLOCK); l.add(ModBlocks.SMOOTH_SALT_BLOCK); l.add(ModBlocks.SALT_PILLAR);
        l.add(ModBlocks.SALT_BRICKS); l.add(ModBlocks.SALT_BRICK_STAIRS); l.add(ModBlocks.SALT_BRICK_SLAB); l.add(ModBlocks.SALT_BRICK_WALL);
        l.add(ModBlocks.MIXED_SALT_BRICKS); l.add(ModBlocks.MIXED_SALT_BRICK_STAIRS); l.add(ModBlocks.MIXED_SALT_BRICK_SLAB);
        l.add(ModBlocks.MIXED_SALT_BRICK_WALL); l.add(ModBlocks.WILD_PEANUTS); l.add(ModBlocks.WILD_GHOST_PEPPERS);
        l.add(ModBlocks.WILD_CUCUMBERS); l.add(ModBlocks.WILD_OATS);
        l.add(ModBlocks.DEFAULT_SALT_LAMP); l.add(ModBlocks.BLACK_SALT_LAMP); l.add(ModBlocks.BLUE_SALT_LAMP); l.add(ModBlocks.BROWN_SALT_LAMP);
        l.add(ModBlocks.CYAN_SALT_LAMP); l.add(ModBlocks.GRAY_SALT_LAMP); l.add(ModBlocks.GREEN_SALT_LAMP); l.add(ModBlocks.LIGHT_BLUE_SALT_LAMP);
        l.add(ModBlocks.LIGHT_GRAY_SALT_LAMP); l.add(ModBlocks.LIME_SALT_LAMP); l.add(ModBlocks.MAGENTA_SALT_LAMP); l.add(ModBlocks.ORANGE_SALT_LAMP);
        l.add(ModBlocks.PINK_SALT_LAMP); l.add(ModBlocks.PURPLE_SALT_LAMP); l.add(ModBlocks.RED_SALT_LAMP); l.add(ModBlocks.WHITE_SALT_LAMP);
        l.add(ModBlocks.YELLOW_SALT_LAMP);
        l.add(SALT_DUST); l.add(SALT_BUCKET); l.add(OAT); l.add(RAW_OAT); l.add(PEANUT); l.add(GHOST_PEPPER); l.add(CUCUMBER);
        l.add(OAT_SEEDS); l.add(GHOST_PEPPER_SEEDS); l.add(CUCUMBER_SEEDS); l.add(ModBlocks.MAGIC_PEANUT); l.add(ROASTED_PEANUT);
        l.add(HONEY_ROASTED_PEANUT); l.add(PICKLED_PEPPER); l.add(GHOST_CHARCOAL); l.add(PICKLE); l.add(CUCUMBER_DOUGH);
        l.add(CUCUMBER_NOODLES); l.add(KIMCHI); l.add(PICKLED_ONION); l.add(PICKLED_BEETROOT); l.add(PICKLED_EGG); l.add(CENTURY_EGG);
        l.add(SALTED_COD); l.add(SALTED_SALMON); l.add(SURSTROMMING); l.add(OAT_DOUGH); l.add(OATMEAL); l.add(OATMEAL_COOKIE);
        l.add(OVERNIGHT_OATS); l.add(CHOCOLATE_NUT_GRANOLA_BAR); l.add(FRUITY_GRANOLA_BAR); l.add(DELUXE_GRANOLA_BAR);
        l.add(EMPTY_MASON_JAR); l.add(APPLE_SAUCE_JAR); l.add(SWEET_BERRY_JAM_JAR); l.add(GLOW_BERRY_JAM_JAR); l.add(PEPPER_JAM_JAR);
        l.add(GREEN_GRAPE_JAM_JAR); l.add(RED_GRAPE_JAM_JAR); l.add(CHERRY_JAM_JAR);
        l.add(RELISH_JAR); l.add(NUT_MASH_JAR); l.add(HONEY_JAR); l.add(VINEGAR_JAR); l.add(NUT_MILK); l.add(APPLE_SAUCE_BOTTLE);
        l.add(SWEET_BERRY_JAM_BOTTLE); l.add(GLOW_BERRY_JAM_BOTTLE); l.add(PEPPER_JAM_BOTTLE); l.add(RELISH_BOTTLE);
        l.add(NUT_MASH_BOTTLE); l.add(VINEGAR_BOTTLE); l.add(ORGANIC_MASH); l.add(CHEESE_CURDS); l.add(CHEESE_WHEEL);
        l.add(CHEESE_SLICE); l.add(CHEESE_PIZZA); l.add(CHEESE_PIZZA_SLICE); l.add(MEAT_PIZZA); l.add(MEAT_PIZZA_SLICE);
        l.add(PICKLE_SOUP); l.add(GHOSTLY_CHILI); l.add(PAD_THAI); l.add(CHEESE_PASTA); l.add(CUCUMBER_SALAD); l.add(PB_J);
        l.add(CHEESE_BURGER); l.add(DELUXE_BURGER); l.add(STUFFED_BURRITO);
        return l;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
