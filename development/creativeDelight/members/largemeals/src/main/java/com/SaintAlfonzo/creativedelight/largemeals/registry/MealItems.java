package com.SaintAlfonzo.creativedelight.largemeals.registry;

import com.SaintAlfonzo.creativedelight.largemeals.LargeMealsMod;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.registry.ModEffects;
import vectorwing.farmersdelight.common.registry.ModItems;

/**
 * Every Large Meals 1.3.0 id (18 items). Food values are upstream's (large_meals.md 3.1, D4); the feast serving items are
 * named for their bowl (D6, lang in LargeMealsTranslator). Bowl foods are Farmer's Delight's: stack 16, a bowl back.
 */
public final class MealItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LargeMealsMod.MODID);

    public static final RegistryObject<Item> RAW_MUTTON_RACK = ITEMS.register("raw_mutton_rack",
            () -> new Item(ModItems.foodItem(new FoodProperties.Builder().nutrition(5).saturationMod(0.3F).meat()
                .build()).tab(MealTab.TAB)));
    public static final RegistryObject<Item> COOKED_MUTTON_RACK = ITEMS.register("cooked_mutton_rack",
            () -> new Item(ModItems.foodItem(new FoodProperties.Builder().nutrition(10).saturationMod(0.8F).meat()
                .build()).tab(MealTab.TAB)));
    public static final RegistryObject<Item> PUFFERFISH_BROTH = ITEMS.register("pufferfish_broth",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(8).saturationMod(0.7F)
                .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 1200, 0), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> POTATO_SOUP = ITEMS.register("potato_soup",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(10).saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> RED_SOUP = ITEMS.register("red_soup",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(10).saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> TOMATO_EGG_SOUP = ITEMS.register("tomato_egg_soup",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(10).saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> COD_DELUXE = ITEMS.register("cod_deluxe",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> HEARTY_LUNCH = ITEMS.register("hearty_lunch",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> CHICKEN_CURRY = ITEMS.register("chicken_curry",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> PASTA_WITH_MUSHROOM_SAUCE = ITEMS.register("pasta_with_mushroom_sauce",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> OMURICE_BLOCK = ITEMS.register("omurice_block",
            () -> new BlockItem(MealBlocks.OMURICE_BLOCK.get(), basic().stacksTo(1)));
    public static final RegistryObject<Item> OMURICE = ITEMS.register("omurice",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> MUSHROOM_POT_PIE_BLOCK = ITEMS.register("mushroom_pot_pie_block",
            () -> new BlockItem(MealBlocks.MUSHROOM_POT_PIE_BLOCK.get(), basic().stacksTo(1)));
    public static final RegistryObject<Item> MUSHROOM_POT_PIE = ITEMS.register("mushroom_pot_pie",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(12).saturationMod(0.8F)
                .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
                .build()), true));
    public static final RegistryObject<Item> ROASTED_MUTTON_RACK_BLOCK = ITEMS.register("roasted_mutton_rack_block",
            () -> new BlockItem(MealBlocks.ROASTED_MUTTON_RACK_BLOCK.get(), basic().stacksTo(1)));
    public static final RegistryObject<Item> ROASTED_MUTTON_RACK = ITEMS.register("roasted_mutton_rack",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(14).saturationMod(0.75F)
                .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
                .build()), true));
    /** Always edible, a glass bottle back, stack 16 as upstream. */
    public static final RegistryObject<Item> SWEET_BERRY_CUSTARD = ITEMS.register("sweet_berry_custard",
            () -> new ConsumableItem(ModItems.foodItem(new FoodProperties.Builder().nutrition(7).saturationMod(0.6F).alwaysEat()
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                .build())
                .craftRemainder(Items.GLASS_BOTTLE).stacksTo(16).tab(MealTab.TAB), true));
    public static final RegistryObject<Item> RICE_PUDDING = ITEMS.register("rice_pudding",
            () -> new ConsumableItem(bowl(new FoodProperties.Builder().nutrition(7).saturationMod(0.6F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
                .build()), true));

    private MealItems() {
    }

    private static Item.Properties basic() {
        return ModItems.basicItem().tab(MealTab.TAB);
    }

    private static Item.Properties bowl(FoodProperties food) {
        return ModItems.bowlFoodItem(food).tab(MealTab.TAB);
    }
}
