// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.mixin;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.data.DietAdjustment;
import com.SaintAlfonzo.solraspberrypie.data.GateFoods;
import com.SaintAlfonzo.solraspberrypie.data.MealLog;
import com.SaintAlfonzo.solraspberrypie.logic.Diversity;
import com.SaintAlfonzo.solraspberrypie.logic.Drinks;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.tarinoita.solsweetpotato.tracking.FoodList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Drinks leave the food diet (7): with shouldForbiddenCount on, Apple Pie would still age every remembered food and
 * bump foodsEaten for a blacklisted drink. Returning early keeps the food memory and the counter untouched.
 * <p>
 * Also keeps the unique-food gate counter ({@link GateFoods}, 12.4): filled on a counted eat, cleared with Apple Pie's
 * own counter, saved beside it in Apple Pie's NBT (key {@code solrp_gateFoods}). A list saved without the key is
 * backfilled with the remembered foods eaten since the counter last reset.
 * <p>
 * And the meal log ({@link MealLog}, plan_diet_books.md 1.2): the last {@code endDecay} counted meals, newest first,
 * duplicates kept (key {@code solrp_meals}); cleared with Apple Pie's food memory. A list saved without the key is
 * backfilled from the memory (one entry per food still in the window, so no duplicates).
 * <p>
 * And the diversity table ({@link Diversity}, plan_food_diversity_1121.md R1): a listed food's value replaces Apple
 * Pie's formula and config list; a family member is handled as its family head in every per-item method, so the whole
 * family is one diet entry.
 */
@Mixin(value = FoodList.class, remap = false)
public abstract class FoodListMixin implements GateFoods, MealLog {
	@Unique
	private static final String SOLRP_KEY = "solrp_gateFoods";

	@Shadow
	private int foodsEaten;

	@Shadow
	@Final
	private Map<FoodInstance, Integer> uniqueFoods;

	@Unique
	private static final String SOLRP_MEALS = "solrp_meals";

	/** The Adjustment entry's value ({@code /solrp diversity set}), beside Apple Pie's own list. */
	@Unique
	private static final String SOLRP_ADJUST = "solrp_adjust";

	@Unique
	private final Set<ResourceLocation> solrp$gate = new LinkedHashSet<>();

	@Unique
	private final LinkedList<ResourceLocation> solrp$mealLog = new LinkedList<>();

	@Override
	public List<ResourceLocation> solrp$meals() {
		return solrp$mealLog;
	}

	@Inject(method = "clearFood", at = @At("RETURN"))
	private void solrp$clearMeals(CallbackInfo ci) {
		solrp$mealLog.clear();
	}

	@Override
	public Set<ResourceLocation> solrp$gateFoods() {
		return solrp$gate;
	}

	@Inject(method = "getComplexity", at = @At("HEAD"), cancellable = true)
	private static void solrp$tableValue(FoodInstance food, CallbackInfoReturnable<Double> cir) {
		if ((Object) food instanceof DietAdjustment a && a.solrp$value() != null) {
			cir.setReturnValue(a.solrp$value());
			return;
		}
		Double v = food == null ? null : Diversity.value(food.getItem());
		if (v != null) cir.setReturnValue(v);
	}

	@ModifyVariable(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("HEAD"), argsOnly = true)
	private Item solrp$familyAdd(Item item) {
		return Diversity.head(item);
	}

	@ModifyVariable(method = "addFood(Lnet/minecraft/world/item/Item;Ljava/util/Map;)V", at = @At("HEAD"), argsOnly = true)
	private Item solrp$familyAddTo(Item item) {
		return Diversity.head(item);
	}

	@ModifyVariable(method = "simulateFoodAdd", at = @At("HEAD"), argsOnly = true)
	private Item solrp$familySimulate(Item item) {
		return Diversity.head(item);
	}

	@ModifyVariable(method = "getLastEaten", at = @At("HEAD"), argsOnly = true)
	private Item solrp$familyLastEaten(Item item) {
		return Diversity.head(item);
	}

	@ModifyVariable(method = "hasEaten", at = @At("HEAD"), argsOnly = true)
	private Item solrp$familyHasEaten(Item item) {
		return Diversity.head(item);
	}

	@Inject(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("HEAD"), cancellable = true)
	private void solrp$skipDrinks(Item item, CallbackInfo ci) {
		if (Drinks.isOutOfDiet(item)) ci.cancel();
	}

	@Inject(method = "addFood(Lnet/minecraft/world/item/Item;)V", at = @At("RETURN"))
	private void solrp$countUnique(Item item, CallbackInfo ci) {
		if (!SOLSweetPotatoConfig.shouldCount(item)) return;
		ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
		if (id == null) return;
		solrp$gate.add(id);
		solrp$mealLog.addFirst(id);
		while (solrp$mealLog.size() > MealLog.length()) solrp$mealLog.removeLast();
	}

	@Inject(method = "resetFoodsEaten", at = @At("RETURN"))
	private void solrp$resetUnique(CallbackInfo ci) {
		solrp$gate.clear();
	}

	@Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
	private void solrp$save(CallbackInfoReturnable<CompoundTag> cir) {
		ListTag list = new ListTag();
		for (ResourceLocation id : solrp$gate) list.add(StringTag.valueOf(id.toString()));
		cir.getReturnValue().put(SOLRP_KEY, list);
		ListTag meals = new ListTag();
		for (ResourceLocation id : solrp$mealLog) meals.add(StringTag.valueOf(id.toString()));
		cir.getReturnValue().put(SOLRP_MEALS, meals);
		for (FoodInstance f : uniqueFoods.keySet()) {
			if ((Object) f instanceof DietAdjustment a && a.solrp$value() != null) cir.getReturnValue().putDouble(SOLRP_ADJUST, a.solrp$value());
		}
	}

	@Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
	private void solrp$load(CompoundTag tag, CallbackInfo ci) {
		solrp$loadMeals(tag);
		solrp$loadAdjustment(tag);
		solrp$gate.clear();
		if (tag.contains(SOLRP_KEY, Tag.TAG_LIST)) {
			for (Tag el : tag.getList(SOLRP_KEY, Tag.TAG_STRING)) {
				ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
				if (id != null) solrp$gate.add(id);
			}
			return;
		}
		// A list saved without the key: a food with fewer meals since than foodsEaten was eaten after the reset.
		for (Map.Entry<FoodInstance, Integer> e : uniqueFoods.entrySet()) {
			if (e.getValue() >= foodsEaten || e.getKey().getItem() == null) continue;
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(e.getKey().getItem());
			if (id != null) solrp$gate.add(id);
		}
	}

	/** Puts the saved value back on the Adjustment entry; an entry without one (no key) is dropped. */
	@Unique
	private void solrp$loadAdjustment(CompoundTag tag) {
		Item adj = SolRaspberryPie.ADJUSTMENT.get();
		FoodInstance entry = null;
		for (FoodInstance f : uniqueFoods.keySet()) if (f.getItem() == adj) entry = f;
		if (entry == null) return;
		if (tag.contains(SOLRP_ADJUST, Tag.TAG_ANY_NUMERIC)) ((DietAdjustment) (Object) entry).solrp$setValue(tag.getDouble(SOLRP_ADJUST));
		else uniqueFoods.remove(entry);
	}

	@Unique
	private void solrp$loadMeals(CompoundTag tag) {
		solrp$mealLog.clear();
		if (tag.contains(SOLRP_MEALS, Tag.TAG_LIST)) {
			for (Tag el : tag.getList(SOLRP_MEALS, Tag.TAG_STRING)) {
				ResourceLocation id = ResourceLocation.tryParse(el.getAsString());
				if (id != null) solrp$mealLog.add(id);
			}
			return;
		}
		// A list saved without the key: the remembered foods still in the window, newest first.
		List<Map.Entry<FoodInstance, Integer>> in = new ArrayList<>();
		for (Map.Entry<FoodInstance, Integer> e : uniqueFoods.entrySet()) {
			if (e.getValue() < MealLog.length() && e.getKey().getItem() != null) in.add(e);
		}
		in.sort(Map.Entry.comparingByValue());
		for (Map.Entry<FoodInstance, Integer> e : in) {
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(e.getKey().getItem());
			if (id != null) solrp$mealLog.add(id);
		}
	}
}
