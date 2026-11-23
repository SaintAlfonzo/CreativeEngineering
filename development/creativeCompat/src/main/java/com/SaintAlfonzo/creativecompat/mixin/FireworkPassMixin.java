package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassProjectiles;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fireworks in webs (plan_passthrough.md 5.1 {@code linger}; phase 0 brief 5.1): {@code FireworkRocketEntity#tick}
 * ({@code m_8119_}) moves and then puts its motion back, so a change made inside the move is lost. The move only
 * records the web result; RETURN of the tick applies it ({@code surface.PassProjectiles#fireworkTail}). A rocket
 * boosting a gliding player ({@code isAttachedToEntity}, {@code m_37088_}) is skipped. SRG names,
 * {@code remap = false}.
 */
@Mixin(value = FireworkRocketEntity.class, remap = false)
public abstract class FireworkPassMixin {

	@Shadow
	private boolean m_37088_() {
		throw new AssertionError();
	}

	@Inject(method = "m_8119_", at = @At("RETURN"))
	private void creativecompat$fireworkWeb(CallbackInfo ci) {
		try {
			PassProjectiles.fireworkTail((FireworkRocketEntity) (Object) this, m_37088_());
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("FireworkPassMixin", t);
			// a broken lookup must never stop the rocket
		}
	}
}
