package com.SaintAlfonzo.creativeengineering.damage;

import java.util.Collection;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * The flags MC 1.19.2 stores on a {@link DamageSource}, exposed as built-in damage tags in the
 * {@code creativeengineering} namespace ({@code creativeengineering:fire}, ...). They are computed
 * live from the source on every lookup, so any modded source that sets a flag is tagged for
 * free. A data file with the same name ({@code data/creativeengineering/damage_tags/fire.json})
 * adds message ids ON TOP of the flag - it never removes a flagged source.
 *
 * <p>Declaration order is only the order tags are listed in; it carries no priority. Each flag
 * owns one bit of the mask {@link #mask} returns, which {@link DamageTags} uses as part of its
 * cache key (the flags of a given message id are not always constant - {@link #MELEE} depends on
 * the attacker - so a message id alone can't key the cache).
 */
public enum DamageFlag {
    /** Skips armour and toughness - and, as in vanilla, shield blocking. */
    ARMOR_PIERCING("armor_piercing", s -> s.isBypassArmor()),
    FIRE("fire", s -> s.isFire()),
    MAGIC("magic", s -> s.isMagic()),
    EXPLOSION("explosion", s -> s.isExplosion()),
    PROJECTILE("projectile", s -> s.isProjectile()),
    FALL("fall", s -> s.isFall()),
    /** Scaled by difficulty - which zeroes it for a player on Peaceful. */
    DIFFICULTY_SCALED("difficulty_scaled", s -> s.scalesWithDifficulty()),
    /** The direct damager is a living entity (a mob or player hitting in melee, or thorns). */
    MELEE("melee", s -> s.getDirectEntity() instanceof LivingEntity),
    HELMET_DAMAGE("helmet_damage", s -> s.isDamageHelmet()),
    /** Skips the Resistance effect and protection enchantments. */
    BYPASSES_RESISTANCE("bypasses_resistance", s -> s.isBypassMagic()),
    /** Skips protection enchantments only. */
    BYPASSES_ENCHANTMENTS("bypasses_enchantments", s -> s.isBypassEnchantments()),
    /** Void / {@code /kill} tier: hurts creative and invulnerable entities. */
    BYPASSES_INVULNERABILITY("bypasses_invulnerability", s -> s.isBypassInvul());

    private static final DamageFlag[] VALUES = values();

    private final ResourceLocation tag;
    private final Predicate<DamageSource> matcher;

    DamageFlag(String path, Predicate<DamageSource> matcher) {
        // NAMESPACE is a compile-time constant, so this doesn't initialise DamageTags (which
        // itself reads DamageFlag) and can't form a class-init cycle.
        this.tag = new ResourceLocation(DamageTags.NAMESPACE, path);
        this.matcher = matcher;
    }

    /** The built-in tag this flag stands for. */
    public ResourceLocation tag() {
        return tag;
    }

    public boolean matches(DamageSource source) {
        return matcher.test(source);
    }

    /** Bit set of every flag {@code source} has. */
    public static int mask(DamageSource source) {
        int mask = 0;
        for (DamageFlag flag : VALUES) {
            if (flag.matcher.test(source)) {
                mask |= 1 << flag.ordinal();
            }
        }
        return mask;
    }

    /** Adds the tag of every flag set in {@code mask}, in declaration order. */
    public static void addTags(int mask, Collection<ResourceLocation> out) {
        for (DamageFlag flag : VALUES) {
            if ((mask & (1 << flag.ordinal())) != 0) {
                out.add(flag.tag);
            }
        }
    }

    /** True when {@code tag} is one of the built-in flag tags. */
    public static boolean isFlagTag(ResourceLocation tag) {
        for (DamageFlag flag : VALUES) {
            if (flag.tag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /** Lower-case names of the flags set in {@code mask}, for logging. */
    public static String describe(int mask) {
        StringBuilder sb = new StringBuilder();
        for (DamageFlag flag : VALUES) {
            if ((mask & (1 << flag.ordinal())) != 0) {
                sb.append(sb.length() == 0 ? "" : ",").append(flag.tag.getPath());
            }
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }
}
