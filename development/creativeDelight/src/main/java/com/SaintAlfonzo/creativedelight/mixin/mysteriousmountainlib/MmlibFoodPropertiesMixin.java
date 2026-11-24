// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.mixin.mysterious_mountain_lib;

import java.lang.reflect.Field;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mysterious Mountain Lib's food items (Corn Delight's dishes and kernels: {@code ItemFoodBase}, its subclass
 * {@code ItemDrinkBase}, and {@code ItemFoodSeeds}) build a new FoodProperties from their own FoodInfo on every
 * {@code getFoodProperties()} call (SRG {@code m_41473_}) and never set Item's {@code foodProperties} field, so the
 * pack's KubeJS food values (food_value_copies.js, which writes that field) never applied (r1120nv SQ-FF-04 nachos
 * bowl, SQ-FF-09 corn seeds; TODO meals #69). The field is null on these items unless KubeJS set it: when it is set,
 * it wins; when not, MMLib's own value stands.
 */
@Pseudo
@Mixin(targets = {"cn.mcmod_mmf.mmlib.item.ItemFoodBase", "cn.mcmod_mmf.mmlib.item.ItemFoodSeeds"}, remap = false)
public abstract class MmlibFoodPropertiesMixin {

	/** Item.foodProperties (SRG f_41380_); null when the lookup failed, then nothing changes. */
	@Unique
	private static final Field creativedelight$FOOD = creativedelight$field();

	@Unique
	private static Field creativedelight$field() {
		try {
			return ObfuscationReflectionHelper.findField(Item.class, "f_41380_");
		} catch (RuntimeException e) {
			return null;
		}
	}

	@Inject(method = "m_41473_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativedelight$packFood(CallbackInfoReturnable<FoodProperties> cir) {
		if (creativedelight$FOOD == null) return;
		try {
			Object set = creativedelight$FOOD.get(this);
			if (set instanceof FoodProperties food) cir.setReturnValue(food);
		} catch (IllegalAccessException ignored) {
			// leave MMLib's value
		}
	}
}
