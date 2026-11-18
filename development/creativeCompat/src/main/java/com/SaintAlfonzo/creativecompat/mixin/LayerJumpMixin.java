package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassThrough;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Weaker jumps inside deep snow and leaf layers (plan_post_scratch_1115.md G4c, G7.13; {@link PassThrough#jumpPower}):
 * RETURN of {@code getJumpPower} ({@code m_6118_}). Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class LayerJumpMixin {

	@Inject(method = "m_6118_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$layerJump(CallbackInfoReturnable<Float> cir) {
		try {
			float p = cir.getReturnValueF();
			float q = PassThrough.jumpPower((LivingEntity) (Object) this, p);
			if (q != p) cir.setReturnValue(q);
		} catch (Throwable ignored) {
			// vanilla jump
		}
	}
}
