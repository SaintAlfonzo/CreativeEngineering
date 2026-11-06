package com.SaintAlfonzo.creativeequipment.shield;

import java.util.Set;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecommon.damage.DamageTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

/**
 * How an incoming hit is classified, purely by which {@code creativeengineering:*} damage tag
 * ({@link DamageTags}) it carries - never by reading {@link DamageSource}'s own flags directly.
 * A category also names the {@link ShieldGroup} (physical / elemental / magic) hits of that tag
 * are tuned under, and can be named in an on-block effect's category filter.
 *
 * <p>EXPLOSION/PROJECTILE/MELEE read the comprehensive, hand-verified base tags of the same name
 * (which mirror - and extend - the old vanilla-flag-only tags); MAGIC and ELEMENTAL read the two
 * pure meta tags ({@code data/creativeengineering/damage_tags/magic.json},
 * {@code elemental.json}), each just a list of {@code #include}s of finer tags. Widening what
 * counts as elemental or magic - or reclassifying a modded source into explosion/projectile/melee
 * - is a data edit to one of those files; no Java, no rebuild.
 *
 * <p><b>Declaration order is classification priority</b> - {@link #classify} returns the first
 * constant whose tag the hit carries, so specific categories sit above broad ones (a blaze
 * fireball is both projectile and fire; PROJECTILE is declared first, so it lands there and is
 * physical, not elemental; thorns is magic and also has a living direct entity, but MAGIC is
 * declared above MELEE so it lands there). {@link #OTHER} carries no tag, matches everything, and
 * must stay last - PHYSICAL doesn't need its own tag check because it is that exhaustive default.
 *
 * <p><b>To add a category:</b> add one constant above {@link #OTHER} naming the tag it reads (any
 * {@link DamageTags#id}) and the group it tunes under. Reusing an existing group means no new
 * config keys; pointing it at a new {@link ShieldGroup} gives it keys of its own in every tier.
 */
public enum ShieldCategory {
    EXPLOSION("explosion", ShieldGroup.PHYSICAL, DamageTags.id("explosion")),
    PROJECTILE("projectile", ShieldGroup.PHYSICAL, DamageTags.id("projectile")),
    MAGIC("magic", ShieldGroup.MAGIC, DamageTags.id("magic")),
    ELEMENTAL("elemental", ShieldGroup.ELEMENTAL, DamageTags.id("elemental")),
    MELEE("melee", ShieldGroup.PHYSICAL, DamageTags.id("melee")),
    /** Everything else. With the default REQUIRED facing this is exactly vanilla's rule. */
    OTHER("other", ShieldGroup.PHYSICAL, null);

    private final String key;
    private final ShieldGroup group;
    /** The tag a hit must carry to land here; null only for {@link #OTHER}. */
    @Nullable
    private final ResourceLocation tag;

    ShieldCategory(String key, ShieldGroup group, @Nullable ResourceLocation tag) {
        this.key = key;
        this.group = group;
        this.tag = tag;
    }

    /** Lower-case effect-filter name. */
    public String key() {
        return key;
    }

    /** The physical / elemental / magic group this category is tuned under. */
    public ShieldGroup group() {
        return group;
    }

    public static ShieldCategory classify(DamageSource source) {
        Set<ResourceLocation> tags = DamageTags.tagsOf(source);
        for (ShieldCategory c : values()) {
            if (c.tag == null || tags.contains(c.tag)) {
                return c;
            }
        }
        return OTHER;
    }

    @Nullable
    public static ShieldCategory byKey(String key) {
        for (ShieldCategory c : values()) {
            if (c.key.equalsIgnoreCase(key)) {
                return c;
            }
        }
        return null;
    }
}
