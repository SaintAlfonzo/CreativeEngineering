package com.sammy.minersdelight.registry;

import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.item.HoneyBarItem;
import com.sammy.minersdelight.content.item.SilverfishEggsItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.function.Function;

/**
 * The items of Miner's Delight 1.4.5, in its creative tab order, without the Copper Cup and every cup (TODO dishes/meals
 * #53, 2026-11-02; 1.1.2's hot cocoa cup was never ported, D3). Bowl foods stack to 16 and return a bowl.
 */
public final class MDItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MinersDelightMod.MODID);

    // Blocks
    public static final RegistryObject<Item> COPPER_POT = block(MDBlocks.COPPER_POT, props().stacksTo(1));
    public static final RegistryObject<Item> STICKY_BASKET = block(MDBlocks.STICKY_BASKET, props());
    public static final RegistryObject<Item> STUFFED_SQUID = block(MDBlocks.STUFFED_SQUID, props().stacksTo(1));
    public static final RegistryObject<Item> FAKE_MEATLOAF = block(MDBlocks.FAKE_MEATLOAF, props().stacksTo(1));
    public static final RegistryObject<Item> GLAZED_ARACHNID_LIMBS = block(MDBlocks.GLAZED_ARACHNID_LIMBS, props().stacksTo(1));
    public static final RegistryObject<Item> WILD_CAVE_CARROTS = block(MDBlocks.WILD_CAVE_CARROTS, props());
    public static final RegistryObject<Item> GOSSYPIUM = block(MDBlocks.GOSSYPIUM, props());
    public static final RegistryObject<Item> CAVE_CARROT_CRATE = block(MDBlocks.CAVE_CARROT_CRATE, props());

    // Food
    public static final RegistryObject<Item> CAVE_CARROT = ITEMS.register("cave_carrot",
            () -> new ItemNameBlockItem(MDBlocks.CAVE_CARROTS.get(), props().food(MDFoods.CAVE_CARROT)));
    public static final RegistryObject<Item> BAKED_CAVE_CARROT = food("baked_cave_carrot", MDFoods.BAKED_CAVE_CARROT);
    public static final RegistryObject<Item> COPPER_CARROT = food("copper_carrot", MDFoods.COPPER_CARROT);
    public static final RegistryObject<Item> PASTA_WITH_VEGGIEBALLS = bowlFood("pasta_with_veggieballs", MDFoods.PASTA_WITH_VEGGIEBALLS);
    public static final RegistryObject<Item> CAVE_SOUP = bowlFood("cave_soup", MDFoods.CAVE_SOUP);
    public static final RegistryObject<Item> VEGAN_PATTY = food("vegan_patty", MDFoods.VEGAN_PATTY);
    public static final RegistryObject<Item> VEGAN_HAMBURGER = food("vegan_hamburger", MDFoods.VEGAN_HAMBURGER);
    public static final RegistryObject<Item> VEGAN_WRAP = food("vegan_wrap", MDFoods.VEGAN_WRAP);
    public static final RegistryObject<Item> VEGAN_STEAK_AND_POTATOES = bowlFood("vegan_steak_and_potatoes", MDFoods.VEGAN_STEAK_AND_POTATOES);
    public static final RegistryObject<Item> MOSS = food("moss", MDFoods.MOSS);
    public static final RegistryObject<Item> BAT_WING = food("bat_wing", MDFoods.BAT_WING);
    public static final RegistryObject<Item> SMOKED_BAT_WING = food("smoked_bat_wing", MDFoods.SMOKED_BAT_WING);
    public static final RegistryObject<Item> BAT_ROLLS = food("bat_rolls", MDFoods.BAT_ROLLS);
    public static final RegistryObject<Item> BAT_COOKIE = food("bat_cookie", MDFoods.BAT_COOKIE);
    public static final RegistryObject<Item> IMPROVISED_BARBECUE_STICK = food("improvised_barbecue_stick", MDFoods.IMPROVISED_BARBECUE_STICK);
    public static final RegistryObject<Item> BAT_SOUP = bowlFood("bat_soup", MDFoods.BAT_SOUP);
    public static final RegistryObject<Item> SILVERFISH_EGGS = food("silverfish_eggs", MDFoods.SILVERFISH_EGGS, SilverfishEggsItem::new);
    public static final RegistryObject<Item> WEIRD_CAVIAR = bowlFood("weird_caviar", MDFoods.WEIRD_CAVIAR);
    public static final RegistryObject<Item> ARTHROPOD = food("arthropod", MDFoods.ARTHROPOD);
    public static final RegistryObject<Item> COOKED_ARTHROPOD = food("cooked_arthropod", MDFoods.COOKED_ARTHROPOD);
    public static final RegistryObject<Item> INSECT_SANDWICH = food("insect_sandwich", MDFoods.INSECT_SANDWICH);
    public static final RegistryObject<Item> INSECT_WRAP = food("insect_wrap", MDFoods.INSECT_WRAP);
    public static final RegistryObject<Item> INSECT_STEW = bowlFood("insect_stew", MDFoods.INSECT_STEW);
    public static final RegistryObject<Item> SEASONED_ARTHROPODS = bowlFood("seasoned_arthropods", MDFoods.SEASONED_ARTHROPODS);
    public static final RegistryObject<Item> SPIDER_LEG = food("spider_leg", MDFoods.SPIDER_LEG);
    public static final RegistryObject<Item> BAKED_SPIDER_LEG = food("baked_spider_leg", MDFoods.BAKED_SPIDER_LEG);
    public static final RegistryObject<Item> PLATE_OF_GLAZED_ARACHNID_LIMBS = bowlFood("plate_of_glazed_arachnid_limbs", MDFoods.PLATE_OF_GLAZED_ARACHNID_LIMBS);
    public static final RegistryObject<Item> PLATE_OF_FAKE_MEATLOAF = bowlFood("plate_of_fake_meatloaf", MDFoods.PLATE_OF_FAKE_MEATLOAF);
    public static final RegistryObject<Item> CAVE_HAMBURGER = food("cave_hamburger", MDFoods.CAVE_HAMBURGER);
    public static final RegistryObject<Item> CRUNCHY_BAR = food("crunchy_bar", MDFoods.CRUNCHY_BAR);
    public static final RegistryObject<Item> NUTRITIONAL_BAR = food("nutritional_bar", MDFoods.NUTRITIONAL_BAR, HoneyBarItem::new);
    public static final RegistryObject<Item> GOLDEN_NUTRITIONAL_BAR = food("golden_nutritional_bar", MDFoods.GOLDEN_NUTRITIONAL_BAR, HoneyBarItem::new);
    public static final RegistryObject<Item> TENTACLES_ON_A_STICK = food("tentacles_on_a_stick", MDFoods.TENTACLES_ON_A_STICK);
    public static final RegistryObject<Item> SQUID = food("squid", MDFoods.SQUID);
    public static final RegistryObject<Item> GLOW_SQUID = food("glow_squid", MDFoods.GLOW_SQUID);
    public static final RegistryObject<Item> BAKED_SQUID = food("baked_squid", MDFoods.BAKED_SQUID);
    public static final RegistryObject<Item> TENTACLES = food("tentacles", MDFoods.TENTACLES);
    public static final RegistryObject<Item> BAKED_TENTACLES = food("baked_tentacles", MDFoods.BAKED_TENTACLES);
    public static final RegistryObject<Item> SQUID_SANDWICH = food("squid_sandwich", MDFoods.SQUID_SANDWICH);
    public static final RegistryObject<Item> TAKOYAKI = bowlFood("takoyaki", MDFoods.TAKOYAKI);
    public static final RegistryObject<Item> BOWL_OF_STUFFED_SQUID = bowlFood("bowl_of_stuffed_squid", MDFoods.BOWL_OF_STUFFED_SQUID);
    public static final RegistryObject<Item> GLOW_INK_PASTA = bowlFood("glow_ink_pasta", MDFoods.GLOW_INK_PASTA);

    private MDItems() {
    }

    private static Item.Properties props() {
        return new Item.Properties().tab(MDTab.TAB);
    }

    private static RegistryObject<Item> block(RegistryObject<? extends Block> block, Item.Properties properties) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }

    private static RegistryObject<Item> food(String name, FoodProperties food) {
        return ITEMS.register(name, () -> new Item(props().food(food)));
    }

    private static RegistryObject<Item> food(String name, FoodProperties food, Function<Item.Properties, Item> factory) {
        return ITEMS.register(name, () -> factory.apply(props().food(food)));
    }

    private static RegistryObject<Item> bowlFood(String name, FoodProperties food) {
        return ITEMS.register(name, () -> new ConsumableItem(props().food(food).craftRemainder(Items.BOWL).stacksTo(16), MDFoods.hasEffects(food)));
    }
}
