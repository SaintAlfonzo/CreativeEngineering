package com.SaintAlfonzo.creativedelight.delightful;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Food values of the backported items, Delightful 3.8.1's Nutrition read from the donor jar. Effects that 3.8.1
 * takes from another mod use the same lookup and fallback (TeaCompat): Farmer's Respite's Vitality and Caffeinated.
 */
public final class DelightfulBackportFoods {

	private DelightfulBackportFoods() {}

	public static final FoodProperties CANTALOUPE_BREAD = new FoodProperties.Builder().nutrition(9).saturationMod(0.35F)
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0), 1.0F).build();
	public static final FoodProperties WRAPPED_CANTALOUPE = new FoodProperties.Builder().nutrition(8).saturationMod(0.95F)
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0), 1.0F).build();
	public static final FoodProperties CANTALOUPE_POPSICLE = new FoodProperties.Builder().nutrition(3).saturationMod(0.2F).fast().alwaysEat()
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0F).build();
	public static final FoodProperties STUFFED_CANTALOUPE = new FoodProperties.Builder().nutrition(14).saturationMod(0.75F)
			.effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 4800, 0), 1.0F)
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1), 1.0F).build();
	public static final FoodProperties CANTALOUPE_GUMMY = new FoodProperties.Builder().nutrition(2).saturationMod(0.0F).alwaysEat()
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3), 1.0F)
			.effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), 1.0F).build();
	public static final FoodProperties MATCHA_GUMMY = new FoodProperties.Builder().nutrition(2).saturationMod(0.0F).alwaysEat()
			.effect(() -> new MobEffectInstance(greenTea(), 100, 2), 1.0F).build();

	public static final FoodProperties CACTUS_CHILI = new FoodProperties.Builder().nutrition(10).saturationMod(0.7F)
			.effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F)
			.effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0), 1.0F).build();
	public static final FoodProperties CACTUS_SOUP = new FoodProperties.Builder().nutrition(12).saturationMod(0.6F)
			.effect(() -> new MobEffectInstance(ModEffects.NOURISHMENT.get(), 3600, 0), 1.0F).build();

	/** Haste without particles; the HUD icon stays (the five-argument constructor would hide it too). */
	public static final FoodProperties BAKLAVA_SLICE = new FoodProperties.Builder().nutrition(5).saturationMod(0.6F).fast()
			.effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0, false, false, true), 1.0F).build();

	public static final FoodProperties MATCHA_MILKSHAKE = new FoodProperties.Builder().nutrition(2).saturationMod(1.5F).alwaysEat()
			.effect(() -> new MobEffectInstance(greenTea(), 200, 1), 1.0F)
			.effect(() -> new MobEffectInstance(caffeinated(), 300, 0), 1.0F).build();
	public static final FoodProperties SALMONBERRY_MILKSHAKE = new FoodProperties.Builder().nutrition(2).saturationMod(1.5F).alwaysEat()
			.effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 600), 1.0F).build();

	public static final FoodProperties ROASTED_ACORN = new FoodProperties.Builder().nutrition(4).saturationMod(0.5F).build();
	public static final FoodProperties NUT_DOUGH = new FoodProperties.Builder().nutrition(2).saturationMod(0.3F)
			.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F).build();

	/** 3.8.1 TeaCompat.getGreenTeaEffect: Farmer's Respite's Vitality, else Regeneration. */
	private static MobEffect greenTea() {
		return effect("farmersrespite", "vitality", MobEffects.REGENERATION);
	}

	/** 3.8.1 TeaCompat.getCaffeinated: Farmer's Respite's Caffeinated, else Haste. */
	private static MobEffect caffeinated() {
		return effect("farmersrespite", "caffeinated", MobEffects.DIG_SPEED);
	}

	private static MobEffect effect(String namespace, String path, MobEffect fallback) {
		MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, path));
		return effect != null ? effect : fallback;
	}
}
