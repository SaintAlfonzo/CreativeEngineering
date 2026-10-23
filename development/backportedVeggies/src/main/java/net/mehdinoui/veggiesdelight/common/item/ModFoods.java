package net.mehdinoui.veggiesdelight.common.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import vectorwing.farmersdelight.common.registry.ModEffects;

// Upstream food values, unchanged (the pack sets its own values by food type, feature_writeup/veggies_delight.md 4.6).
// The bell pepper and smoked bell pepper are not ported (Rustic Delight peppers instead).
public class ModFoods {
   public static final FoodProperties DANDELION_LEAF = new Builder()
      .nutrition(1)
      .saturationMod(0.1F)
      .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
      .fast()
      .build();
   public static final FoodProperties BROCCOLI = new Builder().nutrition(2).saturationMod(0.4F).build();
   public static final FoodProperties CAULIFLOWER = new Builder().nutrition(4).saturationMod(0.4F).build();
   public static final FoodProperties GARLIC_CLOVE = new Builder().nutrition(1).saturationMod(0.1F).fast().build();
   public static final FoodProperties SWEET_POTATO = new Builder().nutrition(2).saturationMod(0.4F).build();
   public static final FoodProperties TURNIP = new Builder().nutrition(2).saturationMod(0.4F).build();
   public static final FoodProperties ZUCCHINI = new Builder().nutrition(3).saturationMod(0.4F).build();
   public static final FoodProperties CARROT_JUICE = new Builder().effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0), 1.0F).alwaysEat().build();
   public static final FoodProperties DANDELION_JUICE = new Builder()
      .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties TURNIP_WATER = new Builder().effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0), 1.0F).alwaysEat().build();
   public static final FoodProperties FERMENTED_GARLIC_HONEY = new Builder()
      .nutrition(7)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 100, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties BAKED_SWEET_POTATO = new Builder().nutrition(4).saturationMod(0.5F).build();
   public static final FoodProperties CAULIFLOWER_FLORET = new Builder().nutrition(1).saturationMod(0.1F).fast().build();
   public static final FoodProperties ROASTED_CAULIFLOWER_FLORET = new Builder().nutrition(2).saturationMod(0.3F).fast().build();
   public static final FoodProperties ROASTED_GARLIC_CLOVE = new Builder().nutrition(2).saturationMod(0.2F).fast().build();
   public static final FoodProperties ZUCCHINI_SLICE = new Builder().nutrition(2).saturationMod(0.1F).build();
   public static final FoodProperties ROASTED_ZUCCHINI = new Builder().nutrition(4).saturationMod(0.4F).build();
   public static final FoodProperties POTATO_NOODLE = new Builder().nutrition(4).saturationMod(0.2F).build();
   public static final FoodProperties SWEET_POTATO_DOUGH = new Builder()
      .nutrition(3)
      .saturationMod(0.2F)
      .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 300, 0), 0.3F)
      .build();
   public static final FoodProperties RAW_VEGETARIAN_PATTY = new Builder().nutrition(3).saturationMod(0.3F).build();
   public static final FoodProperties COOKED_VEGETARIAN_PATTY = new Builder().nutrition(6).saturationMod(0.8F).build();
   public static final FoodProperties BEETROOT_BROWNIE = new Builder()
      .nutrition(6)
      .saturationMod(0.5F)
      .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), 1.0F)
      .build();
   public static final FoodProperties CARROT_CAKE_SLICE = new Builder().nutrition(3).saturationMod(0.2F).build();
   public static final FoodProperties SWEET_POTATO_PIE_SLICE = new Builder().nutrition(5).saturationMod(0.3F).build();
   public static final FoodProperties SWEET_POTATO_CUPCAKE = new Builder()
      .nutrition(6)
      .saturationMod(0.5F)
      .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), 1.0F)
      .build();
   public static final FoodProperties SWEET_POTATO_PANCAKES = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), 1.0F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties BROCCOLI_SALAD = new Builder()
      .nutrition(6)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0), 1.0F)
      .build();
   public static final FoodProperties CESAR_SALAD = new Builder()
      .nutrition(10)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0), 1.0F)
      .build();
   public static final FoodProperties COLESLAW = new Builder()
      .nutrition(8)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties TURNIP_SALAD = new Builder()
      .nutrition(6)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 100, 0), 1.0F)
      .build();
   public static final FoodProperties CAULIFLOWER_BREAD = new Builder().nutrition(6).saturationMod(0.6F).build();
   public static final FoodProperties GARLIC_BREAD = new Builder().nutrition(5).saturationMod(0.4F).fast().build();
   public static final FoodProperties GARLIC_STUFFED_MUSHROOMS = new Builder().nutrition(7).saturationMod(0.6F).build();
   public static final FoodProperties TURNIP_MUTTON_SKEWER = new Builder().nutrition(7).saturationMod(0.7F).build();
   public static final FoodProperties CHICKEN_FAJITAS_WRAP = new Builder().nutrition(10).saturationMod(0.7F).build();
   public static final FoodProperties VEGETABLES_WRAP = new Builder().nutrition(12).saturationMod(0.8F).build();
   public static final FoodProperties VEGETARIAN_BURGER = new Builder().nutrition(12).saturationMod(0.8F).build();
   public static final FoodProperties ZUCCHINI_SANDWICH = new Builder().nutrition(10).saturationMod(0.7F).build();
   public static final FoodProperties UNCOOKED_MHADJEB = new Builder().nutrition(6).saturationMod(0.4F).build();
   public static final FoodProperties MHADJEB = new Builder().nutrition(12).saturationMod(0.8F).build();
   public static final FoodProperties VEGAN_PIZZA_SLICE = new Builder().nutrition(5).saturationMod(0.5F).build();
   public static final FoodProperties ZUCCHINI_QUICHE_SLICE = new Builder().nutrition(6).saturationMod(0.5F).build();
   public static final FoodProperties STUFFED_ZUCCHINI_BOAT = new Builder().nutrition(10).saturationMod(0.7F).build();
   public static final FoodProperties BROCCOLI_SOUP = new Builder()
      .nutrition(10)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties CAULIFLOWER_SOUP = new Builder()
      .nutrition(10)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties GARLIC_CHICKEN_STEW = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties TURNIP_BEEF_STEW = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties CACCIATORE = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties MASHED_POTATOES = new Builder()
      .nutrition(10)
      .saturationMod(0.7F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties POTATO_NOODLES = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0), 1.0F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.COMFORT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties DANDELION_WITH_EGGS = new Builder()
      .nutrition(10)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties CAULIFLOWER_KUKU = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties TURNIP_CAKE = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties RICE_AND_VEGETABLES = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties ROASTED_VEGETABLES = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties SHAKSHOUKA = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties PASTA_WITH_BROCCOLI = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties GARLIC_RICE_WITH_CAULIFLOWER = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties STEAK_AND_BROCCOLI = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties FISH_AND_CHIPS = new Builder()
      .nutrition(10)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 600, 0), 1.0F)
      .build();
   public static final FoodProperties GARLIC_BAKED_COD = new Builder()
      .nutrition(10)
      .saturationMod(0.6F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties STUFFED_ZUCCHINIS = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
   public static final FoodProperties LASAGNA_SLICE = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
      .build();
   public static final FoodProperties STUFFED_BELLPEPPER = new Builder()
      .nutrition(8)
      .saturationMod(0.65F)
      .effect(() -> new MobEffectInstance((MobEffect)ModEffects.NOURISHMENT.get(), 6000, 0), 1.0F)
      .build();
}
