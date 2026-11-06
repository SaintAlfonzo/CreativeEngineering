package com.SaintAlfonzo.creativecompat.itemphysic;

import net.minecraft.world.damagesource.DamageSource;

/**
 * The damage source for {@link ItemBurnTransform}'s burn-explosions.
 *
 * <p>Vanilla's explosion source ({@code DamageSource.explosion(...)}) is built with
 * {@code setScalesWithDifficulty()}, and {@code Player.hurt} zeroes any difficulty-scaled
 * damage on Peaceful — so a Peaceful survival player takes knockback from an explosion but
 * no damage. This one keeps {@code setExplosion()} (Blast Protection still applies) but
 * omits the difficulty scaling, so it hurts players on every difficulty. The
 * {@code "explosion"} message id keeps the vanilla "Blown up" death message.
 *
 * <p>The {@link DamageSource} constructor is {@code protected}, hence the subclass.
 */
final class BurnBlastDamage extends DamageSource {

    static final DamageSource INSTANCE = new BurnBlastDamage();

    private BurnBlastDamage() {
        super("explosion");
        this.setExplosion();
    }
}
