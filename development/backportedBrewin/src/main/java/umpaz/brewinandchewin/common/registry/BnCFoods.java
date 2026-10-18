package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import vectorwing.farmersdelight.common.registry.ModEffects;

// Port: effects go in as suppliers (Forge FoodProperties.Builder), so Farmer's Delight's RegistryObject effects resolve
// lazily.
public class BnCFoods {
   public static final FoodProperties BEER = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, true, false), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties VODKA = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 3600, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties MEAD = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.SWEET_HEART, 2400, 0, false, false), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties RICE_WINE = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 1200, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties PALE_JANE = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 3600, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 2400, 0, false, false), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties EGG_GROG = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties GLITTERING_GRENADINE = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 600, 0), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties SACCHARINE_RUM = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 3600, 1), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 2400, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.SWEET_HEART, 3600, 0, false, false), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties SALTY_FOLLY = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 3600, 1), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 1800, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties BLOODY_MARY = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 1), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.RAGING, 1200, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties RED_RUM = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 2), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.RAGING, 2400, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties STRONGROOT_ALE = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 1), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties STEEL_TOE_STOUT = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 2), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties DREAD_NOG = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 4800, 2), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 4200, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties WITHERING_DROSS = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 3600, 2), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 3000, 0), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3000, 0), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.WITHER, 1200, 0), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties KOMBUCHA = new Builder()
      .effect(() -> new MobEffectInstance(BnCEffects.TIPSY, 2400, 0), 1.0F)
      .effect(() -> new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false), 1.0F)
      .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1), 1.0F)
      .alwaysEat()
      .build();
   public static final FoodProperties FLAXEN_CHEESE = new Builder().nutrition(4).saturationMod(1.0F).build();
   public static final FoodProperties SCARLET_CHEESE = new Builder().nutrition(4).saturationMod(1.0F).build();
   public static final FoodProperties VEGETABLE_OMELET = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties CREAMY_ONION_SOUP = new Builder()
      .nutrition(12)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties CHEESY_PASTA = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties HORROR_LASAGNA = new Builder()
      .nutrition(16)
      .saturationMod(0.55F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties SCARLET_PIEROGI = new Builder()
      .nutrition(12)
      .saturationMod(1.0F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 8400, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties FIERY_FONDUE = new Builder()
      .nutrition(14)
      .saturationMod(0.75F)
      .effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 8400, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties PIZZA_SLICE = new Builder().nutrition(5).saturationMod(1.0F).build();
   public static final FoodProperties QUICHE_SLICE = new Builder().nutrition(4).saturationMod(0.8F).fast().build();
   public static final FoodProperties HAM_AND_CHEESE_SANDWICH = new Builder().nutrition(9).saturationMod(1.0F).build();
   public static final FoodProperties KIMCHI = new Builder().nutrition(2).saturationMod(0.6F).build();
   public static final FoodProperties JERKY = new Builder().nutrition(3).saturationMod(0.7F).fast().build();
   public static final FoodProperties PICKLED_PICKLES = new Builder().nutrition(4).saturationMod(0.3F).build();
   public static final FoodProperties KIPPERS = new Builder().nutrition(6).saturationMod(0.5F).build();
   public static final FoodProperties COCOA_FUDGE = new Builder()
      .nutrition(4)
      .saturationMod(0.8F)
      .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 0, false, false), 1.0F)
      .build();
   public static final FoodProperties SWEET_BERRY_JAM = new Builder().nutrition(6).saturationMod(0.4F).build();
   public static final FoodProperties GLOW_BERRY_MARMALADE = new Builder().nutrition(6).saturationMod(0.4F).build();
   public static final FoodProperties APPLE_JELLY = new Builder().nutrition(10).saturationMod(0.6F).build();
}
