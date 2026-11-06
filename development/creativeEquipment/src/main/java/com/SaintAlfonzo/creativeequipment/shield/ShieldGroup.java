package com.SaintAlfonzo.creativeequipment.shield;

import com.SaintAlfonzo.creativeequipment.CEQConfig;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * The knobs a tier shield is actually tuned by: physical, elemental, magic. Each group gets its
 * own 0-100% {@code reduction}, {@code durabilityCost} and {@code facing} key per tier in the
 * config ({@code CEQConfig.Shields}); the finer {@link ShieldCategory} values only decide which
 * group a hit belongs to (and can be used to filter on-block effects).
 *
 * <p>Each group (other than PHYSICAL, which is the exhaustive default) is backed by one meta
 * damage tag - {@code creativecommon:elemental} / {@code creativecommon:magic} - built
 * from {@code #include}s of the finer tags in {@code data/creativeengineering/damage_tags/}
 * ({@link com.SaintAlfonzo.creativecommon.damage.DamageTags}). Moving a kind of damage
 * between groups (e.g. deciding acid should also count as elemental) is a one-line edit to that
 * meta tag's file - no Java, no new config key.
 *
 * <p>To give some damage type its own config keys entirely, add a constant here and point a
 * {@link ShieldCategory} at it. A group a tier has no default for starts at 0% ("not blocked").
 */
public enum ShieldGroup {
    /** Melee, blunt/cutting machinery, piercing, firearms, explosions, projectiles, and anything unclassified. */
    PHYSICAL("physical", Facing.REQUIRED, true),
    /** Fire, cold, electric and chemical damage - {@code creativecommon:elemental}. */
    ELEMENTAL("elemental", Facing.IF_POSITIONED, false),
    /** Vanilla magic plus the pack's psychic/life-drain tags - {@code creativecommon:magic}. */
    MAGIC("magic", Facing.IF_POSITIONED, false);

    /** How a group's direction check works (vanilla shields only ever do REQUIRED). */
    public enum Facing {
        /** Vanilla: the source needs a position and must be inside the shield's front 180 degrees. */
        REQUIRED,
        /** The arc test applies only when the source has a position; positionless sources (lava, potions) are blocked from any direction. */
        IF_POSITIONED,
        /** Never checked - the raised shield blocks this from every direction. */
        NONE
    }

    private final String key;
    private final Facing defaultFacing;
    private final boolean honorsArmorBypass;

    ShieldGroup(String key, Facing defaultFacing, boolean honorsArmorBypass) {
        this.key = key;
        this.defaultFacing = defaultFacing;
        this.honorsArmorBypass = honorsArmorBypass;
    }

    /** Lower-case toml key / effect-filter name - also the matching meta tag's path for ELEMENTAL/MAGIC. */
    public String key() {
        return key;
    }

    public String label() {
        return key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);
    }

    public Facing defaultFacing() {
        return defaultFacing;
    }

    /**
     * True for PHYSICAL: a source tagged {@code creativecommon:armor_piercing} is never
     * blockable, same as vanilla's own bypassArmor rule. False for ELEMENTAL/MAGIC, whose sources
     * (on-fire, poison, ...) carry that tag almost universally - the whole point of blocking them
     * is to ignore it.
     */
    public boolean honorsArmorBypass() {
        return honorsArmorBypass;
    }

    @Nullable
    public static ShieldGroup byKey(String key) {
        for (ShieldGroup g : values()) {
            if (g.key.equalsIgnoreCase(key)) {
                return g;
            }
        }
        return null;
    }
}
