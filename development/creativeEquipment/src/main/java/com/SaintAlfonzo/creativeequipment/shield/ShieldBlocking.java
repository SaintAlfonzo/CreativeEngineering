package com.SaintAlfonzo.creativeequipment.shield;

import com.SaintAlfonzo.creativecommon.shield.EffectTickContext;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecommon.damage.DamageFlag;
import com.SaintAlfonzo.creativecommon.damage.DamageTags;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * "Can this raised tier shield block this hit at all?" - the replacement for vanilla's
 * {@code LivingEntity#isDamageSourceBlocked}, called from {@code mixin/LivingEntityShieldMixin}
 * for tier shields only.
 *
 * <p>Every decision here about what KIND of damage a hit is goes through
 * {@link DamageTags} - never a direct {@code source.isX()} call - so the whole classification
 * is visible and editable as damage tags, per the user's ask. Two exceptions, neither a kind of
 * damage: piercing arrows (a structural vanilla rule - they ignore every shield) and damage dealt by a
 * ticking potion effect ({@link EffectTickContext}; poison ticks share the {@code magic} source with
 * Instant Damage, so no tag can tell them apart).
 *
 * <p>Vanilla's rule is: not bypassArmor, the holder {@code isBlocking()}, not a piercing arrow, and
 * the source has a position inside the front 180 degrees. That rule can never say yes to fire,
 * poison or any other armor-piercing / positionless damage, which is exactly what tier shields
 * must be able to block, so this re-derives it per {@link ShieldCategory}. With every category at
 * its {@code REQUIRED} default and 100% reduction it reduces to the vanilla rule for physical hits.
 */
public final class ShieldBlocking {

    private ShieldBlocking() {}

    /**
     * @return {@code null} when the raised item isn't a tier shield (or the feature is switched
     *         off) - the caller must fall through to vanilla; otherwise the final verdict.
     */
    @Nullable
    public static Boolean verdict(LivingEntity holder, DamageSource source) {
        ItemStack shield = holder.getUseItem();
        ShieldTier tier = ShieldTier.of(shield);
        if (tier == null || !ShieldConfig.enabled()) {
            return null;
        }
        // isBlocking() is vanilla's "shield is up and past its raise delay" (5 ticks; Responsive
        // Shields shortens that globally), so both still apply to tier shields.
        if (!holder.isBlocking() || DamageTags.is(source, DamageFlag.BYPASSES_INVULNERABILITY.tag())) {
            return false; // void / /kill: nothing may block those
        }
        if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
            return false; // vanilla: piercing arrows go through shields - a structural exception, not a damage kind
        }
        MobEffect ticking = EffectTickContext.tickingOn(holder);
        if (ticking != null && ShieldConfig.unblockableEffectTick(ticking)) {
            return false; // poison / wither ticks: never blocked, not even attempted
        }
        ShieldTickGate gate = ShieldTickGate.of(source);
        if (gate != null && !ShieldConfig.blocksTicks(tier, gate)) {
            return false; // burning below netherite, thorns below diamond (defaults): not even attempted
        }

        ShieldCategory category = ShieldCategory.classify(source);
        if (ShieldConfig.reduction(tier, category, source) <= 0) {
            return false;
        }
        if (gate != null) {
            return true; // an allowed tick: no direction, and armour-piercing (barbed pane) doesn't matter
        }
        ShieldGroup group = category.group();
        if (group.honorsArmorBypass() && DamageTags.is(source, DamageFlag.ARMOR_PIERCING.tag())) {
            return false;
        }
        return facesSource(holder, source, ShieldConfig.facing(group));
    }

    /** Vanilla's arc test (XZ-plane dot product of view vs. direction-to-source < 0), plus the two relaxed modes. */
    private static boolean facesSource(LivingEntity holder, DamageSource source, ShieldGroup.Facing mode) {
        if (mode == ShieldGroup.Facing.NONE) {
            return true;
        }
        Vec3 origin = source.getSourcePosition();
        if (origin == null) {
            return mode == ShieldGroup.Facing.IF_POSITIONED;
        }
        Vec3 view = holder.getViewVector(1.0F);
        Vec3 toHolder = origin.vectorTo(holder.position()).normalize();
        toHolder = new Vec3(toHolder.x, 0.0D, toHolder.z);
        return toHolder.dot(view) < 0.0D;
    }
}
