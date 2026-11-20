package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.effect.EffectRules;
import com.SaintAlfonzo.creativecommon.effect.LevelCappedEffect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * {@code LivingEntity#addEffect(MobEffectInstance, Entity)} ({@code m_147207_}; the one-argument overload calls it) applies
 * CommonConfig {@code [effects] remap} and {@code removed} ({@link EffectRules}, {@code potions/effects #9}): a removed effect
 * is refused, a remapped one is added as its target instead, and an effect from an item in {@code forceHudIconItems} gets
 * its HUD icon ({@code potions/effects #8}); the stored level is lowered to its {@code levelCaps} entry. This is the catch-all for sources KubeJS's food-property
 * swap can't reach: cake blocks, effects given by commands (Quark Delight's crab), mob code and potions already brewed.
 * Server side only; the client receives the remapped effect.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectRemapMixin {

	@Inject(method = "m_147207_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeengineering$remapEffect(MobEffectInstance instance, Entity source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.level.isClientSide) return;
		if (EffectRules.isRemoved(instance.getEffect())) {
			cir.setReturnValue(false);
			return;
		}
		MobEffectInstance changed = EffectRules.withItemIcon(EffectRules.remap(instance));
		// [effects] levelCaps: store the capped level, so the saved NBT matches what getAmplifier() shows.
		((LevelCappedEffect) changed).creativecommon$capStoredLevel();
		if (changed != instance) cir.setReturnValue(self.addEffect(changed, source));
	}
}
