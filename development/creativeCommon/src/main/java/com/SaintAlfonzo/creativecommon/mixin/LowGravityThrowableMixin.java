package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.gravity.LowGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Low gravity for thrown projectiles (snowball, egg, ender pearl, splash and lingering potion, bottle o' enchanting, and
 * modded ThrowableProjectile subclasses): their tick subtracts getGravity() (0.03, potions 0.05, XP bottle 0.07,
 * overridable), so the call is redirected and scaled (see LowGravityPullMixin).
 */
@Mixin(value = ThrowableProjectile.class, remap = false)
public abstract class LowGravityThrowableMixin {

	@Shadow(remap = false)
	protected abstract float m_7139_();

	@Redirect(method = "m_8119_", remap = false, require = 1,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;m_7139_()F", remap = false))
	private float creativecommon$lowGravity(ThrowableProjectile self) {
		return (float) (this.m_7139_() * LowGravity.pullScale((Entity) (Object) this));
	}
}
