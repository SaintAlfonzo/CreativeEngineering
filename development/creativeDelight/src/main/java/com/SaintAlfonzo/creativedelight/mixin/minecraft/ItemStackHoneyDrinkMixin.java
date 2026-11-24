// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.food.HoneyDrinks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Items in {@code #creativedelight:honey_drink_sound} drink with the honey bottle's sounds (the sip while drinking and
 * the last gulp), as {@code HoneyBottleItem} does: the smoothies (TODO sweets #8, feature_writeup/smoothies.md 1),
 * which are KubeJS items with no sound setter. On ItemStack so any item can join by tag.
 *
 * <p>SRG names with {@code remap = false} (no refmap in this build): m_41615_ getDrinkingSound, m_41616_ getEatingSound.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackHoneyDrinkMixin {

	@Inject(method = "m_41615_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$honeyDrinkingSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (HoneyDrinks.is((ItemStack) (Object) this)) cir.setReturnValue(SoundEvents.HONEY_DRINK);
	}

	@Inject(method = "m_41616_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$honeyEatingSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (HoneyDrinks.is((ItemStack) (Object) this)) cir.setReturnValue(SoundEvents.HONEY_DRINK);
	}
}
