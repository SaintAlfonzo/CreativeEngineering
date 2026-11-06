package com.SaintAlfonzo.creativedelight.enders.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

/** Food values of the five 1.1.4 dishes (1.1.4 ModFoods, unchanged). */
public final class EAFoods {
    public static final FoodProperties AMBERVEIL_STEW = new FoodProperties.Builder().nutrition(4).saturationMod(0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 0), 1.0F).build();
    public static final FoodProperties AMBERVEILED_CURRY = new FoodProperties.Builder().nutrition(5).saturationMod(0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 3600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 0), 1.0F).build();
    public static final FoodProperties RISOTTO = new FoodProperties.Builder().nutrition(7).saturationMod(0.9F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 0), 1.0F).build();
    public static final FoodProperties CHICKEN_CURRY = new FoodProperties.Builder().nutrition(7).saturationMod(0.9F)
            .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F).build();
    public static final FoodProperties STEAK_FRIES = new FoodProperties.Builder().nutrition(6).saturationMod(0.7F).build();

    private EAFoods() {
    }
}
