// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SnowLayers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mobs sunk in snow layers (plan_post_scratch_1115.md G11; {@link SnowLayers#mobMove}): RETURN of
 * {@code handleRelativeFrictionAndCalculateMovement} ({@code m_21074_}), where vanilla gives ladders and powder snow
 * their climb speed. A sunk mob that walks into a block climbs out; a stale path over the top is dropped.
 * Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class SnowMobMixin {

	@Inject(method = "m_21074_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$snowClimb(Vec3 input, float friction, CallbackInfoReturnable<Vec3> cir) {
		try {
			Vec3 v = SnowLayers.mobMove((LivingEntity) (Object) this, cir.getReturnValue());
			if (v != null) cir.setReturnValue(v);
		} catch (Throwable ignored) {
			// vanilla movement
		}
	}
}
