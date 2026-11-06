package com.SaintAlfonzo.creativedelight.rustic.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.function.Supplier;

/**
 * Food values: Rustic Delight 1.7.0's, apart from two kept at 1.3.0 (feature_writeup/rustic_delight.md 5.3): Bell
 * Pepper Soup gives Comfort (the pack keeps Comfort on soups) and Honey and Chocolate Coffee last 900 ticks (the pack's
 * Caffeinated swap reads these durations). Syrup, Pumpkin and Cherry Blossom Coffee (3 beans, milk and a flavour, as
 * Honey and Chocolate) last the same 900 ticks at level I (owner 2026-11-01, TODO creativedelight/members #11; 1.7.0:
 * Syrup 1800, Pumpkin and Cherry Blossom 1200 with one of the two effects at level II).
 */
public final class RDFoods {
    private static final int BRIEF = 600;
    private static final int SHORT = 1200;
    private static final int MEDIUM = 3600;
    private static final int LONG = 6000;

    public static final FoodProperties BATTER = food(2, 0.2F).effect(() -> effect(MobEffects.HUNGER, BRIEF, 0), 0.3F).fast().build();
    public static final FoodProperties COOKING_OIL = food(2, 0.3F).effect(() -> effect(MobEffects.CONFUSION, 400, 0), 0.5F).fast().build();
    public static final FoodProperties SYRUP = food(2, 0.2F).effect(() -> effect(MobEffects.MOVEMENT_SPEED, 300, 0), 1.0F).fast().build();
    public static final FoodProperties BELL_PEPPER = food(2, 0.2F).build();
    public static final FoodProperties BELL_PEPPER_SLICE = food(1, 0.1F).build();
    public static final FoodProperties ROASTED_COFFEE_BEANS = food(2, 0.2F).build();
    public static final FoodProperties GOLDEN_COFFEE_BEANS = food(5, 1.2F).build();
    public static final FoodProperties ROASTED_BELL_PEPPER = food(5, 0.8F).build();
    public static final FoodProperties ROASTED_BELL_PEPPER_SLICE = food(3, 0.6F).build();
    public static final FoodProperties POTATO_SLICES = food(1, 0.3F).build();
    public static final FoodProperties BAKED_POTATO_SLICES = food(3, 0.6F).build();
    public static final FoodProperties CALAMARI = food(2, 0.2F).build();
    public static final FoodProperties CALAMARI_SLICE = food(1, 0.1F).build();
    public static final FoodProperties COOKED_CALAMARI = food(5, 0.6F).build();
    public static final FoodProperties COOKED_CALAMARI_SLICE = food(3, 0.6F).build();

    public static final FoodProperties COFFEE = coffee(0, 0.0F, 1800, 0, 1800, 0);
    public static final FoodProperties MILK_COFFEE = coffee(0, 0.0F, SHORT, 0, SHORT, 0);
    public static final FoodProperties CHOCOLATE_COFFEE = coffee(0, 0.0F, 900, 0, 900, 0);
    public static final FoodProperties HONEY_COFFEE = coffee(6, 0.1F, 900, 0, 900, 0);
    public static final FoodProperties SYRUP_COFFEE = coffee(0, 0.0F, 900, 0, 900, 0);
    public static final FoodProperties DARK_COFFEE = coffee(0, 0.0F, 900, 1, 900, 1);
    public static final FoodProperties PUMPKIN_COFFEE = coffee(0, 0.0F, 900, 0, 900, 0);
    public static final FoodProperties CHERRY_BLOSSOM_COFFEE = coffee(0, 0.0F, 900, 0, 900, 0);

    public static final FoodProperties SYRUP_SANDWICH = food(8, 0.7F).build();
    public static final FoodProperties HONEY_PANCAKE = withEffect(4, 0.6F, MobEffects.MOVEMENT_SPEED, 900);
    public static final FoodProperties CHOCOLATE_PANCAKE = withEffect(4, 0.6F, MobEffects.MOVEMENT_SPEED, 900);
    public static final FoodProperties CHERRY_BLOSSOM_PANCAKE = withEffect(4, 0.6F, MobEffects.HEALTH_BOOST, BRIEF);
    public static final FoodProperties VEGETABLE_PANCAKE = withEffect(4, 0.6F, MobEffects.DIG_SPEED, BRIEF);
    public static final FoodProperties PUMPKIN_PANCAKE = withEffect(4, 0.6F, MobEffects.HEALTH_BOOST, BRIEF);
    public static final FoodProperties COFFEE_PANCAKE = withEffect(4, 0.6F, MobEffects.DIG_SPEED, BRIEF);
    public static final FoodProperties COFFEE_CHEESECAKE_SLICE = food(3, 0.3F).fast()
            .effect(() -> effect(MobEffects.MOVEMENT_SPEED, BRIEF, 0), 1.0F).effect(() -> effect(MobEffects.DIG_SPEED, BRIEF, 0), 1.0F).build();
    public static final FoodProperties CALAMARI_ROLL = food(7, 0.6F).build();
    public static final FoodProperties CHERRY_BLOSSOM_ROLL = food(6, 0.5F).build();
    public static final FoodProperties BELL_PEPPER_ROLL = food(7, 0.8F).build();
    public static final FoodProperties STUFFED_BELL_PEPPER = food(10, 0.7F).build();
    public static final FoodProperties SPRING_ROLLS = food(6, 0.6F).build();
    public static final FoodProperties FRIED_FISH = food(8, 0.7F).build();
    public static final FoodProperties FRIED_DUMPLINGS = food(10, 0.8F).build();
    public static final FoodProperties POTATO_SALAD = withEffect(8, 0.7F, MobEffects.REGENERATION, 100);
    public static final FoodProperties SWEET_SALAD = withEffect(8, 0.7F, MobEffects.REGENERATION, 100);
    public static final FoodProperties BELL_PEPPER_SOUP = withEffect(6, 0.6F, ModEffects.COMFORT, MEDIUM);
    public static final FoodProperties CALAMARI_SOUP = withEffect(10, 0.8F, ModEffects.NOURISHMENT, MEDIUM);
    public static final FoodProperties BELL_PEPPER_PASTA = withEffect(12, 0.8F, ModEffects.NOURISHMENT, MEDIUM);
    public static final FoodProperties FRIED_CHICKEN = withEffect(12, 0.8F, ModEffects.NOURISHMENT, MEDIUM);
    public static final FoodProperties FRIED_CALAMARI = withEffect(12, 0.8F, ModEffects.NOURISHMENT, MEDIUM);
    public static final FoodProperties FRIED_MUSHROOMS = withEffect(12, 0.8F, ModEffects.NOURISHMENT, MEDIUM);
    public static final FoodProperties COFFEE_BRAISED_BEEF = withEffect(14, 0.75F, ModEffects.NOURISHMENT, LONG);

    private RDFoods() {
    }

    private static FoodProperties.Builder food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
    }

    private static MobEffectInstance effect(MobEffect effect, int duration, int amplifier) {
        return new MobEffectInstance(effect, duration, amplifier);
    }

    private static FoodProperties withEffect(int nutrition, float saturation, MobEffect effect, int duration) {
        return food(nutrition, saturation).effect(() -> effect(effect, duration, 0), 1.0F).build();
    }

    private static FoodProperties withEffect(int nutrition, float saturation, Supplier<MobEffect> effect, int duration) {
        return food(nutrition, saturation).effect(() -> effect(effect.get(), duration, 0), 1.0F).build();
    }

    /** Coffees: Speed then Haste, drinkable when full. */
    private static FoodProperties coffee(int nutrition, float saturation, int speed, int speedLevel, int haste, int hasteLevel) {
        return food(nutrition, saturation).alwaysEat().fast()
                .effect(() -> effect(MobEffects.MOVEMENT_SPEED, speed, speedLevel), 1.0F)
                .effect(() -> effect(MobEffects.DIG_SPEED, haste, hasteLevel), 1.0F).build();
    }
}
