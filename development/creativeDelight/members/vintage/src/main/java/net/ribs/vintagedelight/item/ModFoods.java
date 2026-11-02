package net.ribs.vintagedelight.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import vectorwing.farmersdelight.common.registry.ModEffects;

// Upstream's values, less the gearo foods, the pickled pitcher pod and every 0:30 Dehydrated (S8); plus KOTS's
// Cucumber Dough.
public class ModFoods {
    private static final int EXTRA_LONG_DURATION = 9600;
    public static final FoodProperties SALTED_COD = new Builder().nutrition(6).fast().saturationMod(0.7F).build();
    public static final FoodProperties SALTED_SALMON = new Builder().nutrition(7).fast().saturationMod(0.85F).build();
    public static final FoodProperties SURSTROMMING = new Builder().nutrition(9).fast().saturationMod(0.85F).build();
    public static final FoodProperties GHOST_PEPPER = new Builder().nutrition(3).saturationMod(0.2F).build();
    public static final FoodProperties PICKLED_PEPPER = new Builder()
       .fast()
       .nutrition(6)
       .saturationMod(0.3F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 0.6F)
       .build();
    public static final FoodProperties CUCUMBER = new Builder().nutrition(2).saturationMod(0.2F).build();
    // KOTS 0.3.1
    public static final FoodProperties CUCUMBER_DOUGH = new Builder().nutrition(3).saturationMod(0.2F).build();
    public static final FoodProperties CUCUMBER_NOODLES = new Builder().nutrition(2).saturationMod(0.2F).build();
    public static final FoodProperties OAT = new Builder().nutrition(2).saturationMod(0.2F).build();
    public static final FoodProperties OAT_DOUGH = new Builder().nutrition(3).saturationMod(0.2F).build();
    public static final FoodProperties PEANUT = new Builder().nutrition(2).fast().saturationMod(0.2F).build();
    public static final FoodProperties ROASTED_PEANUT = new Builder().nutrition(4).fast().saturationMod(0.2F).build();
    public static final FoodProperties HONEY_ROASTED_PEANUT = new Builder().nutrition(5).fast().saturationMod(0.3F).build();
    public static final FoodProperties PICKLE_SOUP = new Builder()
       .nutrition(13)
       .saturationMod(0.85F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 0.65F)
       .build();
    public static final FoodProperties PICKLE = new Builder()
       .fast()
       .nutrition(6)
       .saturationMod(0.3F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 0.6F)
       .build();
    public static final FoodProperties OATMEAL = new Builder()
       .nutrition(8)
       .saturationMod(0.75F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties OATMEAL_COOKIE = new Builder().nutrition(2).saturationMod(0.3F).build();
    public static final FoodProperties OVERNIGHT_OATS = new Builder()
       .nutrition(14)
       .saturationMod(0.8F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 9600, 0), 1.0F)
       .build();
    public static final FoodProperties CHOCOLATE_NUT_GRANOLA_BAR = new Builder()
       .fast()
       .nutrition(4)
       .saturationMod(0.6F)
       .alwaysEat()
       .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0), 1.0F)
       .build();
    public static final FoodProperties FRUITY_GRANOLA_BAR = new Builder()
       .fast()
       .nutrition(3)
       .saturationMod(0.6F)
       .alwaysEat()
       .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0), 1.0F)
       .build();
    public static final FoodProperties DELUXE_GRANOLA_BAR = new Builder()
       .fast()
       .nutrition(5)
       .saturationMod(0.65F)
       .alwaysEat()
       .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0), 1.0F)
       .build();
    public static final FoodProperties CHEESE_CURDS = new Builder().nutrition(2).saturationMod(0.8F).build();
    public static final FoodProperties CHEESE_SLICE = new Builder().nutrition(4).fast().saturationMod(0.8F).build();
    public static final FoodProperties APPLE_SAUCE = new Builder()
       .nutrition(8)
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties APPLE_SAUCE_BOTTLE = new Builder()
       .nutrition(3)
       .fast()
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 1200, 0), 1.0F)
       .build();
    public static final FoodProperties SWEET_BERRY_JAM = new Builder()
       .nutrition(7)
       .saturationMod(0.65F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    // Pack addition: the grape and cherry jam jars (sweet berry jam jar values)
    public static final FoodProperties FRUIT_JAM = new Builder()
       .nutrition(7)
       .saturationMod(0.65F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties SWEET_BERRY_JAM_BOTTLE = new Builder()
       .nutrition(2)
       .fast()
       .saturationMod(0.6F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 1200, 0), 1.0F)
       .build();
    public static final FoodProperties HONEY_JAR = new Builder().nutrition(10).saturationMod(0.1F).build();
    public static final FoodProperties GLOW_BERRY_JAM = new Builder()
       .nutrition(7)
       .saturationMod(0.65F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 600, 0), 1.0F)
       .build();
    public static final FoodProperties GLOW_BERRY_JAM_BOTTLE = new Builder()
       .nutrition(2)
       .fast()
       .saturationMod(0.6F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 1200, 0), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 600, 0), 1.0F)
       .build();
    public static final FoodProperties NUT_MASH = new Builder()
       .nutrition(8)
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 0), 1.0F)
       .build();
    public static final FoodProperties NUT_MASH_BOTTLE = new Builder()
       .nutrition(3)
       .fast()
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0F)
       .build();
    public static final FoodProperties NUT_MILK = new Builder().nutrition(3).saturationMod(0.2F).alwaysEat().build();
    public static final FoodProperties VINEGAR = new Builder()
       .nutrition(1)
       .saturationMod(0.2F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 1200, 0), 1.0F)
       .build();
    public static final FoodProperties VINEGAR_JAR = new Builder()
       .nutrition(3)
       .saturationMod(0.2F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
       .build();
    public static final FoodProperties PEPPER_JAM = new Builder()
       .nutrition(9)
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0), 1.0F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
       .build();
    public static final FoodProperties PEPPER_JAM_BOTTLE = new Builder()
       .nutrition(3)
       .fast()
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0), 1.0F)
       .build();
    public static final FoodProperties RELISH = new Builder()
       .nutrition(7)
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties RELISH_BOTTLE = new Builder()
       .nutrition(2)
       .fast()
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 1.0F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 600, 0), 1.0F)
       .build();
    public static final FoodProperties KIMCHI = new Builder()
       .nutrition(4)
       .fast()
       .saturationMod(0.3F)
       .build();
    public static final FoodProperties PICKLED_ONION = new Builder()
       .fast()
       .nutrition(6)
       .saturationMod(0.2F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 0.6F)
       .build();
    public static final FoodProperties PICKLED_BEETROOT = new Builder()
       .fast()
       .nutrition(5)
       .saturationMod(0.4F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 0.6F)
       .build();
    public static final FoodProperties PICKLED_EGG = new Builder()
       .fast()
       .nutrition(6)
       .saturationMod(0.65F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 0.6F)
       .build();
    public static final FoodProperties CENTURY_EGG = new Builder()
       .nutrition(11)
       .saturationMod(0.8F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 600, 2), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 2), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 2), 1.0F)
       .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
       .build();
    public static final FoodProperties PB_J = new Builder().nutrition(8).saturationMod(0.7F).build();
    public static final FoodProperties CHEESEBURGER = new Builder().nutrition(11).saturationMod(0.95F).build();
    public static final FoodProperties DELUXE_BURGER = new Builder()
       .nutrition(15)
       .saturationMod(0.95F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties STUFFED_BURRITO = new Builder()
       .nutrition(14)
       .saturationMod(0.9F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
       .build();
    public static final FoodProperties CHEESE_PASTA = new Builder()
       .nutrition(9)
       .saturationMod(0.75F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
       .build();
    public static final FoodProperties CHEESE_PIZZA_SLICE = new Builder()
       .nutrition(4)
       .saturationMod(0.75F)
       .fast()
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 1.0F)
       .build();
    public static final FoodProperties MEAT_PIZZA_SLICE = new Builder()
       .nutrition(5)
       .saturationMod(0.75F)
       .fast()
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600, 0), 1.0F)
       .build();
    public static final FoodProperties GHOSTLY_CHILI = new Builder()
       .nutrition(12)
       .saturationMod(0.8F)
       .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0), 1.0F)
       .effect(() -> new MobEffectInstance(ModEffects.COMFORT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties PAD_THAI = new Builder()
       .nutrition(12)
       .saturationMod(0.75F)
       .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
       .build();
    public static final FoodProperties CUCUMBER_SALAD = new Builder()
       .nutrition(8)
       .saturationMod(0.7F)
       .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 2), 1.0F)
       .build();
}
