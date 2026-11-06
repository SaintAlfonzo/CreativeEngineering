package com.SaintAlfonzo.creativeequipment.shield;

import java.util.Set;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecommon.damage.DamageFlag;
import com.SaintAlfonzo.creativecommon.damage.DamageTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Repeating environmental damage (1 damage a tick or so, no attacker, no direction) that only some
 * tiers may block at all. Each gate is one damage tag plus a per-tier on/off config key
 * ({@code [shields.<tier>].<configKey>}, defaults in {@link ShieldTier#defaultBlocksTicks}).
 *
 * <p>A tier the gate is off for doesn't attempt the block (no shield sound, full damage). A tier it is
 * on for blocks the hit from any direction and ignores the armour-piercing rule - these sources have no
 * position and several are armour-piercing - at the hit's normal group percentage, which the
 * round-down rule turns into a full block for 1-damage ticks.
 *
 * <p>Projectiles never count: an ownerless fireball reuses the {@code onFire} id.
 */
public enum ShieldTickGate {
    /** On fire, in fire, campfires, braziers, magma, stoves. */
    BURN("burn_tick", "blocksBurnTicks", "Blocks burning"),
    /** Cactus, berry bushes and other thorny plants, barbed panes. */
    CONTACT("contact_tick", "blocksContactTicks", "Blocks thorns");

    private final ResourceLocation tag;
    private final String configKey;
    private final String label;

    ShieldTickGate(String tagPath, String configKey, String label) {
        this.tag = DamageTags.id(tagPath);
        this.configKey = configKey;
        this.label = label;
    }

    public ResourceLocation tag() {
        return tag;
    }

    /** Per-tier toml key. */
    public String configKey() {
        return configKey;
    }

    /** Full-view tooltip label. */
    public String label() {
        return label;
    }

    /** The gate this hit falls under, or null for an ordinary hit (and for every projectile). */
    @Nullable
    public static ShieldTickGate of(DamageSource source) {
        Set<ResourceLocation> tags = DamageTags.tagsOf(source);
        if (tags.contains(DamageFlag.PROJECTILE.tag())) {
            return null;
        }
        for (ShieldTickGate g : values()) {
            if (tags.contains(g.tag)) {
                return g;
            }
        }
        return null;
    }
}
