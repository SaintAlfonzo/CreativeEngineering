package com.SaintAlfonzo.creativecompat.surface;

import net.minecraft.world.phys.HitResult;

/**
 * Duck interface on {@code Projectile} ({@code mixin/ProjectilePassMixin}): its protected {@code onHit}, for the
 * {@code break} group (plan_passthrough.md 5.1).
 */
public interface PassImpact {

	/** {@code Projectile#onHit}, virtual (each subclass's own impact runs). */
	void creativecompat$impact(HitResult hit);
}
