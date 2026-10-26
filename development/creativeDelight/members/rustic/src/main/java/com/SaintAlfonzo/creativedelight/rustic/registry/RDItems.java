package com.SaintAlfonzo.creativedelight.rustic.registry;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.SaintAlfonzo.creativedelight.rustic.content.EffectClearingDrinkItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.DrinkableItem;
import vectorwing.farmersdelight.common.item.FuelItem;

import java.util.Map;
import java.util.function.Function;

/**
 * The items of Rustic Delight 1.7.0 less Fruit Beignet (removed from the pack), every 1.3.0 id among them. Block items
 * first, then the rest in upstream's order, which is also the creative tab's order.
 */
public final class RDItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RusticDelightMod.MODID);

    static {
        for (RegistryObject<Block> block : java.util.List.of(RDBlocks.COTTON_SEEDS_BAG, RDBlocks.BELL_PEPPER_SEEDS_BAG, RDBlocks.PALE_BELL_PEPPER_SEEDS_BAG,
                RDBlocks.DARK_BELL_PEPPER_SEEDS_BAG, RDBlocks.COFFEE_BEANS_BAG, RDBlocks.ROASTED_COFFEE_BEANS_BAG, RDBlocks.COTTON_BOLL_CRATE)) {
            block(block, props());
        }
        RDBlocks.PEPPER_CRATES.values().forEach(block -> block(block, props()));
        block(RDBlocks.CALAMARI_CRATE, props());
        RDBlocks.GIANT_PEPPERS.values().forEach(block -> block(block, props()));
        for (RegistryObject<Block> block : java.util.List.of(RDBlocks.WILD_COTTON, RDBlocks.WILD_BELL_PEPPERS, RDBlocks.WILD_PALE_BELL_PEPPERS,
                RDBlocks.WILD_DARK_BELL_PEPPERS, RDBlocks.WILD_COFFEE, RDBlocks.SYRUP_CHEESECAKE, RDBlocks.CHERRY_BLOSSOM_CHEESECAKE, RDBlocks.COFFEE_CHEESECAKE)) {
            block(block, props());
        }
        for (RegistryObject<Block> block : java.util.List.of(RDBlocks.PANCAKES, RDBlocks.HONEY_PANCAKES, RDBlocks.CHOCOLATE_PANCAKES,
                RDBlocks.CHERRY_BLOSSOM_PANCAKES, RDBlocks.VEGETABLE_PANCAKES, RDBlocks.PUMPKIN_PANCAKES, RDBlocks.COFFEE_PANCAKES)) {
            block(block, bowlProps());
        }
        for (RegistryObject<Block> block : java.util.List.of(RDBlocks.RICE_ROLL_ROYALE, RDBlocks.BELL_PEPPER_MEDLEY, RDBlocks.PALE_BELL_PEPPER_MEDLEY,
                RDBlocks.DARK_BELL_PEPPER_MEDLEY)) {
            block(block, props().craftRemainder(Items.BOWL).stacksTo(1));
        }
    }

    public static final RegistryObject<Item> COTTON_SEEDS = seeds("cotton_seeds", RDBlocks.COTTON_CROP);
    public static final RegistryObject<Item> BELL_PEPPER_SEEDS = seeds("bell_pepper_seeds", RDBlocks.BELL_PEPPER_CROP);
    public static final RegistryObject<Item> PALE_BELL_PEPPER_SEEDS = seeds("pale_bell_pepper_seeds", RDBlocks.PALE_BELL_PEPPER_CROP);
    public static final RegistryObject<Item> DARK_BELL_PEPPER_SEEDS = seeds("dark_bell_pepper_seeds", RDBlocks.DARK_BELL_PEPPER_CROP);
    public static final RegistryObject<Item> COTTON_BOLL = ITEMS.register("cotton_boll", () -> new FuelItem(props(), 100));

    /** {@code bell_pepper_<colour>}. */
    public static final Map<String, RegistryObject<Item>> PEPPERS = RDBlocks.byColour(colour -> food("bell_pepper_" + colour, RDFoods.BELL_PEPPER));
    public static final Map<String, RegistryObject<Item>> ROASTED_PEPPERS = RDBlocks.byColour(colour -> food("roasted_bell_pepper_" + colour, RDFoods.ROASTED_BELL_PEPPER));
    public static final RegistryObject<Item> CALAMARI = food("calamari", RDFoods.CALAMARI);
    public static final RegistryObject<Item> COOKED_CALAMARI = food("cooked_calamari", RDFoods.COOKED_CALAMARI);
    public static final RegistryObject<Item> COFFEE_BEANS = seeds("coffee_beans", RDBlocks.COFFEE_CROP);
    public static final RegistryObject<Item> ROASTED_COFFEE_BEANS = food("roasted_coffee_beans", RDFoods.ROASTED_COFFEE_BEANS);
    public static final RegistryObject<Item> GOLDEN_COFFEE_BEANS = food("golden_coffee_beans", RDFoods.GOLDEN_COFFEE_BEANS);

    public static final RegistryObject<Item> COFFEE = bottle("coffee", RDFoods.COFFEE, props -> new DrinkableItem(props, true));
    public static final RegistryObject<Item> DARK_COFFEE = bottle("dark_coffee", RDFoods.DARK_COFFEE, props -> new DrinkableItem(props, true));
    public static final RegistryObject<Item> MILK_COFFEE = bottle("milk_coffee", RDFoods.MILK_COFFEE, RDItems::clearsEffect);
    public static final RegistryObject<Item> CHOCOLATE_COFFEE = bottle("chocolate_coffee", RDFoods.CHOCOLATE_COFFEE,
            props -> new EffectClearingDrinkItem(props, true));
    public static final RegistryObject<Item> HONEY_COFFEE = bottle("honey_coffee", RDFoods.HONEY_COFFEE, RDItems::clearsEffect);
    public static final RegistryObject<Item> SYRUP_COFFEE = bottle("syrup_coffee", RDFoods.SYRUP_COFFEE, RDItems::clearsEffect);
    public static final RegistryObject<Item> PUMPKIN_COFFEE = bottle("pumpkin_coffee", RDFoods.PUMPKIN_COFFEE, RDItems::clearsEffect);
    public static final RegistryObject<Item> CHERRY_BLOSSOM_COFFEE = bottle("cherry_blossom_coffee", RDFoods.CHERRY_BLOSSOM_COFFEE, RDItems::clearsEffect);
    public static final RegistryObject<Item> COOKING_OIL = bottle("cooking_oil", RDFoods.COOKING_OIL, DrinkableItem::new);
    public static final RegistryObject<Item> SYRUP = bottle("syrup", RDFoods.SYRUP, props -> new DrinkableItem(props, true));

    public static final RegistryObject<Item> BATTER = ITEMS.register("batter", () -> new Item(bowlProps().food(RDFoods.BATTER)));
    public static final RegistryObject<Item> POTATO_SLICES = food("potato_slices", RDFoods.POTATO_SLICES);
    public static final RegistryObject<Item> BAKED_POTATO_SLICES = food("baked_potato_slices", RDFoods.BAKED_POTATO_SLICES);
    public static final Map<String, RegistryObject<Item>> PEPPER_SLICES = RDBlocks.byColour(colour -> food("bell_pepper_slice_" + colour, RDFoods.BELL_PEPPER_SLICE));
    public static final Map<String, RegistryObject<Item>> ROASTED_PEPPER_SLICES = RDBlocks.byColour(colour ->
            food("roasted_bell_pepper_slice_" + colour, RDFoods.ROASTED_BELL_PEPPER_SLICE));
    public static final RegistryObject<Item> CALAMARI_SLICE = food("calamari_slice", RDFoods.CALAMARI_SLICE);
    public static final RegistryObject<Item> COOKED_CALAMARI_SLICE = food("cooked_calamari_slice", RDFoods.COOKED_CALAMARI_SLICE);

    public static final RegistryObject<Item> SYRUP_CHEESECAKE_SLICE = consumable("syrup_cheesecake_slice", FoodValues.PIE_SLICE, false);
    public static final RegistryObject<Item> CHERRY_BLOSSOM_CHEESECAKE_SLICE = consumable("cherry_blossom_cheesecake_slice", FoodValues.PIE_SLICE, false);
    public static final RegistryObject<Item> COFFEE_CHEESECAKE_SLICE = consumable("coffee_cheesecake_slice", RDFoods.COFFEE_CHEESECAKE_SLICE, false);
    public static final RegistryObject<Item> SYRUP_COOKIE = food("syrup_cookie", FoodValues.COOKIES);
    public static final RegistryObject<Item> CHERRY_BLOSSOM_COOKIE = food("cherry_blossom_cookie", FoodValues.COOKIES);
    public static final RegistryObject<Item> COFFEE_COOKIE = food("coffee_cookie", FoodValues.COOKIES);
    public static final RegistryObject<Item> SYRUP_SANDWICH = food("syrup_sandwich", RDFoods.SYRUP_SANDWICH);

    public static final RegistryObject<Item> PANCAKE = food("pancake", RDFoods.PANCAKE);
    public static final RegistryObject<Item> HONEY_PANCAKE = consumable("honey_pancake", RDFoods.HONEY_PANCAKE, true);
    public static final RegistryObject<Item> CHOCOLATE_PANCAKE = consumable("chocolate_pancake", RDFoods.CHOCOLATE_PANCAKE, true);
    public static final RegistryObject<Item> CHERRY_BLOSSOM_PANCAKE = consumable("cherry_blossom_pancake", RDFoods.CHERRY_BLOSSOM_PANCAKE, true);
    public static final RegistryObject<Item> VEGETABLE_PANCAKE = consumable("vegetable_pancake", RDFoods.VEGETABLE_PANCAKE, true);
    public static final RegistryObject<Item> PUMPKIN_PANCAKE = consumable("pumpkin_pancake", RDFoods.PUMPKIN_PANCAKE, true);
    public static final RegistryObject<Item> COFFEE_PANCAKE = consumable("coffee_pancake", RDFoods.COFFEE_PANCAKE, true);

    public static final RegistryObject<Item> POTATO_SALAD = bowlFood("potato_salad", RDFoods.POTATO_SALAD);
    public static final RegistryObject<Item> SWEET_SALAD = bowlFood("sweet_salad", RDFoods.SWEET_SALAD);
    public static final Map<String, RegistryObject<Item>> STUFFED_PEPPERS = RDBlocks.byColour(colour ->
            food("stuffed_bell_pepper_" + colour, RDFoods.STUFFED_BELL_PEPPER));
    public static final RegistryObject<Item> FRIED_DOUGH = food("fried_dough", RDFoods.FRIED_DOUGH);
    public static final RegistryObject<Item> FRIED_DUMPLINGS = food("fried_dumplings", RDFoods.FRIED_DUMPLINGS);
    public static final RegistryObject<Item> SPRING_ROLLS = food("spring_rolls", RDFoods.SPRING_ROLLS);
    public static final RegistryObject<Item> FRIED_FISH = food("fried_fish", RDFoods.FRIED_FISH);
    public static final Map<String, RegistryObject<Item>> PEPPER_ROLLS = RDBlocks.byColour(colour -> food("bell_pepper_roll_" + colour, RDFoods.BELL_PEPPER_ROLL));
    public static final RegistryObject<Item> BELL_PEPPER_ROLL_GREEN = PEPPER_ROLLS.get("green");
    public static final RegistryObject<Item> BELL_PEPPER_ROLL_YELLOW = PEPPER_ROLLS.get("yellow");
    public static final RegistryObject<Item> BELL_PEPPER_ROLL_RED = PEPPER_ROLLS.get("red");
    public static final RegistryObject<Item> CALAMARI_ROLL = food("calamari_roll", RDFoods.CALAMARI_ROLL);
    public static final RegistryObject<Item> CHERRY_BLOSSOM_ROLL = food("cherry_blossom_roll", RDFoods.CHERRY_BLOSSOM_ROLL);

    public static final RegistryObject<Item> BELL_PEPPER_SOUP = bowlFood("bell_pepper_soup", RDFoods.BELL_PEPPER_SOUP);
    public static final RegistryObject<Item> CALAMARI_SOUP = bowlFood("calamari_soup", RDFoods.CALAMARI_SOUP);
    public static final RegistryObject<Item> BELL_PEPPER_PASTA = bowlFood("bell_pepper_pasta", RDFoods.BELL_PEPPER_PASTA);
    public static final RegistryObject<Item> FRIED_CALAMARI = bowlFood("fried_calamari", RDFoods.FRIED_CALAMARI);
    public static final RegistryObject<Item> FRIED_CHICKEN = bowlFood("fried_chicken", RDFoods.FRIED_CHICKEN);
    public static final RegistryObject<Item> FRIED_MUSHROOMS = bowlFood("fried_mushrooms", RDFoods.FRIED_MUSHROOMS);
    public static final RegistryObject<Item> COFFEE_BRAISED_BEEF = bowlFood("coffee_braised_beef", RDFoods.COFFEE_BRAISED_BEEF);

    private RDItems() {
    }

    private static Item.Properties props() {
        return new Item.Properties().tab(RDTab.TAB);
    }

    private static Item.Properties bowlProps() {
        return props().craftRemainder(Items.BOWL).stacksTo(16);
    }

    private static Item.Properties bottleProps() {
        return props().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16);
    }

    private static Item clearsEffect(Item.Properties properties) {
        return new EffectClearingDrinkItem(properties, false);
    }

    private static void block(RegistryObject<Block> block, Item.Properties properties) {
        ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }

    private static RegistryObject<Item> seeds(String name, RegistryObject<Block> crop) {
        return ITEMS.register(name, () -> new ItemNameBlockItem(crop.get(), props()));
    }

    private static RegistryObject<Item> food(String name, FoodProperties food) {
        return ITEMS.register(name, () -> new Item(props().food(food)));
    }

    private static RegistryObject<Item> consumable(String name, FoodProperties food, boolean effectTooltip) {
        return ITEMS.register(name, () -> new ConsumableItem(props().food(food), effectTooltip));
    }

    private static RegistryObject<Item> bowlFood(String name, FoodProperties food) {
        return ITEMS.register(name, () -> new ConsumableItem(bowlProps().food(food), true));
    }

    private static RegistryObject<Item> bottle(String name, FoodProperties food, Function<Item.Properties, Item> factory) {
        return ITEMS.register(name, () -> factory.apply(bottleProps().food(food)));
    }
}
