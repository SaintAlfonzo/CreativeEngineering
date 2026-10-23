package net.mehdinoui.fungidelight.common.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class ModFoods {
   public static final FoodProperties CLEANED_CAPS = new Builder().nutrition(4).saturationMod(0.4F).build();
   public static final FoodProperties TRUFFLE = new Builder().nutrition(6).saturationMod(0.6F).build();
   public static final FoodProperties TRUFFLE_SLICE = new Builder().nutrition(2).saturationMod(0.2F).build();
   public static final FoodProperties INKY_CAPS_SALAD = new Builder()
      .nutrition(8)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance(MobEffects.LUCK, 600, 0), 1.0F)
      .build();
   public static final FoodProperties COOKED_CLEANED_CAPS = new Builder().nutrition(6).saturationMod(0.6F).build();
   public static final FoodProperties CREAMY_MOREL_SOUP = new Builder()
      .nutrition(10)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties TRUFFLE_ICE_CREAM = new Builder()
      .nutrition(7)
      .saturationMod(0.5F)
      .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 200, 0), 1.0F)
      .build();
   public static final FoodProperties INKY_CAP_SCRAMBLED_EGGS = new Builder()
      .nutrition(10)
      .saturationMod(0.9F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties TRUFFLE_PASTA = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties STUFFED_MORELS = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties STEAK_WITH_MUSHROOMS = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties MUTTON_CHOPS_WITH_TRUFFLE = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties RABBIT_WITH_MORELS = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties PORK_MARSALA_WITH_MUSHROOMS = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
}
