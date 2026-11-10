package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.effect.HHEffects;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Food values. Upstream's except where hearth_and_harvest.md 3.1 (D18, N22, B12) aligns them to the pack's food types.
 * Alcohol's Intoxication and Nausea come from global.alcohol (Creative Brewing), so the wines carry only their own two
 * effects here. Durations in ticks.
 */
final class HHFoods {
    private static FoodProperties.Builder food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation);
    }

    private static Supplier<MobEffectInstance> vanilla(MobEffect effect, int ticks, int level) {
        return () -> new MobEffectInstance(effect, ticks, level);
    }

    private static Supplier<MobEffectInstance> nourishment(int ticks) {
        return () -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), ticks, 0);
    }

    private static Supplier<MobEffectInstance> prickly(int ticks) {
        return () -> new MobEffectInstance(HHEffects.PRICKLY.get(), ticks, 0);
    }

    private static Supplier<MobEffectInstance> tempting(int ticks) {
        return () -> new MobEffectInstance(HHEffects.TEMPTING.get(), ticks, 0);
    }

    private static Supplier<MobEffectInstance> clarity(int ticks) {
        return () -> new MobEffectInstance(HHEffects.CLARITY.get(), ticks, 0);
    }

    private static FoodProperties.Builder drink() {
        return food(0, 0.0F).alwaysEat();
    }

    private static FoodProperties.Builder wine() {
        return food(1, 0.0F).alwaysEat();
    }

    static final FoodProperties SUNFLOWER_SEEDS = food(1, 0.3F).fast().build();
    static final FoodProperties RAISINS = food(2, 0.3F).fast().build();
    static final FoodProperties RAISIN_COOKIE = food(2, 0.2F).fast().build();
    static final FoodProperties PEANUT_BUTTER_COOKIE = food(3, 0.3F).fast().build();
    static final FoodProperties CROISSANT = food(4, 0.5F).build();
    /** Upstream copies the Blueberry Muffin (B12). */
    static final FoodProperties RASPBERRY_SCONE = food(6, 0.3F).build();
    static final FoodProperties CIDER_DONUT = food(5, 0.5F).effect(nourishment(200), 1.0F).build();
    /** cooked/fruit (B12; upstream copies the donut). */
    static final FoodProperties BAKED_APPLE = food(4, 0.3F).build();
    /** Saturation at the sweet norm (D18). */
    static final FoodProperties CANDY_CORN = food(4, 0.35F).fast().build();
    static final FoodProperties PIE_SLICE = food(3, 0.3F).build();
    static final FoodProperties WAFFLE = food(6, 0.3F).build();
    static final FoodProperties CHARRED_MARSHMALLOW = food(1, 0.1F).effect(vanilla(MobEffects.POISON, 200, 0), 1.0F).alwaysEat().fast().build();

    /** dairy type, so it can stand in #forge:cheese (D17, D18). */
    static final FoodProperties GOAT_CHEESE_SLICE = food(4, 0.8F).build();

    static final FoodProperties RAW_SAUSAGE = food(2, 0.3F).build();
    static final FoodProperties COOKED_SAUSAGE = food(3, 0.5F).build();
    static final FoodProperties HOT_DOG = food(10, 0.7F).build();
    /** Nourishment at level I, the pack's level (D18). */
    static final FoodProperties BISCUITS_AND_GRAVY = food(10, 0.75F).effect(nourishment(3600), 1.0F).build();
    /** salad norm: 0.6 saturation, Regeneration 5 s (D18). */
    static final FoodProperties HOPS_SALAD = food(6, 0.6F).effect(vanilla(MobEffects.REGENERATION, 100, 0), 1.0F).build();
    static final FoodProperties TAMALE = food(7, 0.6F).effect(nourishment(3600), 1.0F).build();
    /** Haste II cut from 180 s to the 30 s the pack's strongest Haste food gives (D18). */
    static final FoodProperties STREET_CORN = food(10, 0.8F).effect(nourishment(1200), 1.0F).effect(vanilla(MobEffects.DIG_SPEED, 600, 1), 1.0F).build();
    static final FoodProperties TRAIL_MIX = food(5, 0.5F).effect(vanilla(MobEffects.MOVEMENT_SPEED, 3600, 0), 1.0F)
            .effect(vanilla(MobEffects.REGENERATION, 100, 0), 1.0F).fast().build();
    static final FoodProperties MACARONI_AND_CHEESE = food(11, 0.75F).effect(nourishment(1200), 1.0F).build();
    static final FoodProperties GLAZED_CARROTS = food(10, 0.75F).build();
    static final FoodProperties PICKLED_CARROTS = food(4, 0.5F).effect(nourishment(1200), 1.0F).build();
    static final FoodProperties PICKLED_POTATOES = food(4, 0.5F).effect(vanilla(MobEffects.HEALTH_BOOST, 1200, 0), 1.0F).build();

    static final FoodProperties CHOCOLATE_MILK = food(0, 0.0F).alwaysEat().effect(vanilla(MobEffects.MOVEMENT_SPEED, 600, 0), 1.0F).build();
    static final FoodProperties GOAT_MILK = food(0, 0.0F).alwaysEat().build();
    static final FoodProperties BLUEBERRY_JUICE = drink().effect(vanilla(MobEffects.LUCK, 600, 0), 1.0F).build();
    static final FoodProperties RASPBERRY_JUICE = drink().effect(prickly(600), 1.0F).build();
    static final FoodProperties CHERRY_JUICE = drink().effect(vanilla(MobEffects.DIG_SPEED, 600, 0), 1.0F).build();
    static final FoodProperties SWEET_BERRY_JUICE = drink().effect(tempting(1200), 1.0F).build();
    static final FoodProperties GLOW_BERRY_JUICE = drink().effect(vanilla(MobEffects.NIGHT_VISION, 600, 0), 1.0F).build();
    static final FoodProperties ROOT_BEER = drink().effect(vanilla(MobEffects.NIGHT_VISION, 600, 0), 1.0F).effect(vanilla(MobEffects.DIG_SPEED, 600, 0), 1.0F).build();

    static final FoodProperties BLUEBERRY_WINE = wine().effect(vanilla(MobEffects.REGENERATION, 300, 0), 1.0F).effect(vanilla(MobEffects.LUCK, 600, 1), 0.75F).build();
    static final FoodProperties RASPBERRY_WINE = wine().effect(prickly(1200), 1.0F).effect(vanilla(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 0.5F).build();
    static final FoodProperties SWEET_BERRY_WINE = wine().effect(vanilla(MobEffects.HEAL, 1, 0), 1.0F).effect(tempting(3600), 1.0F).build();
    static final FoodProperties GLOW_BERRY_WINE = wine().effect(clarity(1200), 1.0F).effect(vanilla(MobEffects.NIGHT_VISION, 1200, 0), 0.5F).build();
    static final FoodProperties MELON_WINE = wine().effect(vanilla(MobEffects.FIRE_RESISTANCE, 1200, 0), 1.0F).effect(vanilla(MobEffects.REGENERATION, 1200, 0), 1.0F).build();
    /** Pack addition with the five-wine pattern (N23-14; no upstream values). */
    static final FoodProperties LEMON_WINE = wine().effect(vanilla(MobEffects.DIG_SPEED, 1200, 0), 1.0F).effect(vanilla(MobEffects.MOVEMENT_SPEED, 1200, 0), 0.5F).build();
    /** Like Vodka in global.alcohol; Strength II and Slowness II kept, Saturation II dropped (D16). */
    static final FoodProperties MOONSHINE = wine().effect(vanilla(MobEffects.DAMAGE_BOOST, 1200, 1), 1.0F).effect(vanilla(MobEffects.MOVEMENT_SLOWDOWN, 1200, 1), 1.0F).build();

    private HHFoods() {
    }
}
