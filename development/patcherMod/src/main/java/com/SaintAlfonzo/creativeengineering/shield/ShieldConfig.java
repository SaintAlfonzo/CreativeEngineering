package com.SaintAlfonzo.creativeengineering.shield;

import com.SaintAlfonzo.creativeengineering.CEConfig;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Null-safe reads of {@link CEConfig#SHIELDS}. A {@code ConfigValue#get()} throws until Forge has
 * loaded the toml (JEI / creative-tab building can ask an item for its durability very early),
 * so every read falls back to the enum default instead of propagating that. Values are read
 * fresh on every call, which is what makes the config live-editable.
 */
public final class ShieldConfig {

    private ShieldConfig() {}

    public static boolean enabled() {
        try {
            return CEConfig.SHIELDS.enabled.get();
        } catch (RuntimeException notLoaded) {
            return true;
        }
    }

    public static float wearThreshold() {
        try {
            return CEConfig.SHIELDS.wearThreshold.get().floatValue();
        } catch (RuntimeException notLoaded) {
            return 3.0F;
        }
    }

    /** Ticks a disabled shield stays unusable. See {@code [shields].disableCooldownTicks}. */
    public static int disableCooldownTicks() {
        try {
            return CEConfig.SHIELDS.disableCooldownTicks.get();
        } catch (RuntimeException notLoaded) {
            return 100;
        }
    }

    public static ShieldGroup.Facing facing(ShieldGroup group) {
        try {
            return CEConfig.SHIELDS.facing.get(group).get();
        } catch (RuntimeException notLoaded) {
            return group.defaultFacing();
        }
    }

    public static int durability(ShieldTier tier) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).durability.get();
        } catch (RuntimeException notLoaded) {
            return tier.defaultDurability();
        }
    }

    /** Percent of a hit {@code tier} absorbs for {@code group} (0-100); 0 = not blocked. */
    public static int reduction(ShieldTier tier, ShieldGroup group) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).reduction.get(group).get();
        } catch (RuntimeException notLoaded) {
            return tier.defaultReduction(group);
        }
    }

    public static int reduction(ShieldTier tier, ShieldCategory category) {
        return reduction(tier, category.group());
    }

    /**
     * Percent of a hit this tier absorbs, honoring the elemental fire/electric/cold/chemical
     * breakdown ({@link #elementalBreakdownActive}) when {@code category} is ELEMENTAL and this
     * tier has switched into it. Every other category ignores {@code source} and behaves exactly
     * like {@link #reduction(ShieldTier, ShieldCategory)}.
     */
    public static int reduction(ShieldTier tier, ShieldCategory category, DamageSource source) {
        if (category == ShieldCategory.ELEMENTAL && elementalBreakdownActive(tier)) {
            ElementalKind kind = ElementalKind.classify(source);
            return kind == null ? 0 : elementalReduction(tier, kind);
        }
        return reduction(tier, category);
    }

    /** Raw config value for one element: -1 = not set (falls back to the blanket elemental value). */
    private static int elementalOverrideRaw(ShieldTier tier, ElementalKind kind) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).elementalReduction.get(kind).get();
        } catch (RuntimeException notLoaded) {
            return tier.defaultElementalBreakdown(kind);
        }
    }

    /**
     * True once at least one of this tier's four fire/electric/cold/chemical overrides is set
     * (0-100, not the -1 default) - the tier has switched from one blanket elemental percentage to
     * the per-element breakdown, and {@code reduction.elemental} is ignored entirely for it.
     */
    public static boolean elementalBreakdownActive(ShieldTier tier) {
        for (ElementalKind k : ElementalKind.values()) {
            if (elementalOverrideRaw(tier, k) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Percent this tier absorbs for one specific element, ARRAY MODE ONLY - 0 if that element was
     * left at -1 (or explicitly set to 0). Meaningless (and unused) unless
     * {@link #elementalBreakdownActive} is true for this tier.
     */
    public static int elementalReduction(ShieldTier tier, ElementalKind kind) {
        int raw = elementalOverrideRaw(tier, kind);
        return raw < 0 ? 0 : raw;
    }

    /** Percent of vanilla wear taken; 100 = vanilla. */
    public static int durabilityCost(ShieldTier tier, ShieldGroup group) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).durabilityCost.get(group).get();
        } catch (RuntimeException notLoaded) {
            return 100;
        }
    }

    public static int durabilityCost(ShieldTier tier, ShieldCategory category) {
        return durabilityCost(tier, category.group());
    }

    /** Damage amount (post-reduction) below which a blocked melee hit stops knocking its attacker back. */
    public static float knockbackThreshold(ShieldTier tier) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).knockbackThreshold.get().floatValue();
        } catch (RuntimeException notLoaded) {
            return tier.defaultKnockbackThreshold();
        }
    }

    public static String repairItem(ShieldTier tier) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).repairItem.get();
        } catch (RuntimeException notLoaded) {
            return tier.defaultRepairItem();
        }
    }

    public static String shielderEffects(ShieldTier tier) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).shielderEffects.get();
        } catch (RuntimeException notLoaded) {
            return "";
        }
    }

    public static String attackerEffects(ShieldTier tier) {
        try {
            return CEConfig.SHIELDS.tiers.get(tier).attackerEffects.get();
        } catch (RuntimeException notLoaded) {
            return "";
        }
    }
}
