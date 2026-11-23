package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecommon.effect.SteadyRules;

import net.minecraft.world.entity.Entity;

/**
 * Steady ({@code food-tuning #51}, {@link SteadyRules}): its holder ignores block triggers. In 1.19.2
 * {@code Entity.isIgnoringBlockTriggers} ({@code m_6090_}) is read only by the wooden and stone pressure plates and
 * the tripwire; the weighted plates and big dripleaf have their own mixins.
 */
@Mixin(value = Entity.class, remap = false)
public abstract class SteadyBlockTriggersMixin {

	@Inject(method = "m_6090_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativecommon$steadyIgnoresTriggers(CallbackInfoReturnable<Boolean> cir) {
		if (SteadyRules.isSteady((Entity) (Object) this)) cir.setReturnValue(true);
	}
}
