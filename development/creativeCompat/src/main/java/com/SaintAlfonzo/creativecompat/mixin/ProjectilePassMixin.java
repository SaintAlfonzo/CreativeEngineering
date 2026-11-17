package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassImpact;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * {@code Projectile#onHit} ({@code m_6532_}, protected) as {@link PassImpact} for the {@code break} group
 * (plan_passthrough.md 5.1): a projectile in a surrounded web has its impact run as a block hit at the cell center
 * ({@code surface.PassProjectiles}). The shadow call is virtual, so every subclass's own {@code onHit} runs. A duck
 * interface instead of an {@code @Invoker}: the annotation processor checks invokers against the Mojmap compile jar.
 */
@Mixin(value = Projectile.class, remap = false)
public abstract class ProjectilePassMixin implements PassImpact {

    @Shadow
    protected void m_6532_(HitResult hit) {
        throw new AssertionError();
    }

    @Override
    public void creativecompat$impact(HitResult hit) {
        m_6532_(hit);
    }
}
