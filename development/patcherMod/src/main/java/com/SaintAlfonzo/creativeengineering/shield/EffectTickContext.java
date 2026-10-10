package com.SaintAlfonzo.creativeengineering.shield;

import javax.annotation.Nullable;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;

/**
 * Which potion effect is ticking on which entity right now. Set by
 * {@code mixin/MobEffectTickContextMixin} around the one place every effect's periodic tick runs
 * ({@code MobEffectInstance#applyEffect} -> {@code MobEffect#applyEffectTick}), so a hurt() fired
 * from inside that tick (poison, wither, Everything Copper's copper poisoning, ...) can be told
 * apart from an ordinary hit that happens to use the same damage source - poison and Instant
 * Damage both hurt with vanilla's {@code DamageSource.MAGIC}, so no damage tag can separate them.
 *
 * <p>Server thread only (effects tick there); the mixin restores the outer value, so nested ticks
 * are fine. Plain static fields rather than a lambda/ThreadLocal: this wraps every effect tick.
 */
public final class EffectTickContext {

    private static LivingEntity entity;
    private static MobEffect effect;

    private EffectTickContext() {}

    /** The entity whose effect is ticking (for the mixin to save before {@link #set}). */
    @Nullable
    public static LivingEntity entity() {
        return entity;
    }

    /** The ticking effect (for the mixin to save before {@link #set}). */
    @Nullable
    public static MobEffect effect() {
        return effect;
    }

    /** Marks {@code ticking} as running on {@code target}; pass the saved values back to restore. */
    public static void set(@Nullable LivingEntity target, @Nullable MobEffect ticking) {
        entity = target;
        effect = ticking;
    }

    /** The effect whose tick is running on {@code target} right now, or null. */
    @Nullable
    public static MobEffect tickingOn(LivingEntity target) {
        return entity == target ? effect : null;
    }
}
