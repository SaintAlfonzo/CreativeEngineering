// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.effect.EffectRules;
import com.SaintAlfonzo.creativecommon.effect.LevelCappedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CommonConfig {@code [effects] levelCaps} ({@link EffectRules#capAmplifier}): an effect that does the same at every level
 * never reads or shows a level above its cap.
 * <ul>
 *   <li>{@code getAmplifier()} ({@code m_19564_}) returns the capped value, so every tooltip that builds an instance (food,
 *   drink, potion, the Vinery wine line), the inventory list, the client sync packet and the effect's own tick and
 *   attribute code see the cap.</li>
 *   <li>{@code update(MobEffectInstance)} ({@code m_19558_}) caps the stored field of both instances first. Vanilla
 *   compares the raw fields there, so an uncapped level III with a short duration would otherwise replace a longer
 *   level I that shows the same.</li>
 * </ul>
 * Vanilla-class target by SRG name with {@code remap = false}, as {@link MobEffectTickContextMixin}.
 */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceLevelCapMixin implements LevelCappedEffect {

	@Shadow(remap = false)
	private int f_19504_;

	@Shadow(remap = false)
	public abstract MobEffect m_19544_();

	@Inject(method = "m_19564_", at = @At("RETURN"), cancellable = true, remap = false)
	private void creativecommon$capLevel(CallbackInfoReturnable<Integer> cir) {
		int raw = cir.getReturnValueI();
		int capped = EffectRules.capAmplifier(m_19544_(), raw);
		if (capped != raw) cir.setReturnValue(capped);
	}

	@Inject(method = "m_19558_", at = @At("HEAD"), remap = false)
	private void creativecommon$capBeforeMerge(MobEffectInstance other, CallbackInfoReturnable<Boolean> cir) {
		creativecommon$capStoredLevel();
		if (other != null) ((LevelCappedEffect) other).creativecommon$capStoredLevel();
	}

	@Override
	public void creativecommon$capStoredLevel() {
		f_19504_ = EffectRules.capAmplifier(m_19544_(), f_19504_);
	}
}
