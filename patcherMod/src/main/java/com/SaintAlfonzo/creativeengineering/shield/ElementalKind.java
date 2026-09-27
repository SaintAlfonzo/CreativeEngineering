package com.SaintAlfonzo.creativeengineering.shield;

import java.util.Locale;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.damage.DamageTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

/**
 * The four finer kinds a hit under the ELEMENTAL {@link ShieldGroup} can be broken down into, for
 * a tier that wants fire/electric/cold/chemical tuned separately instead of one blanket elemental
 * percentage. See {@code CEConfig.Shields.TierCfg#elementalReduction} (the
 * {@code [shields.<tier>.elemental_breakdown]} section) and {@link ShieldConfig}.
 *
 * <p>Backed by the same base damage tags {@code elemental.json} itself {@code #include}s
 * ({@code fire}/{@code cold}/{@code electric}/{@code chemical}), so a hit that classifies as
 * {@link ShieldCategory#ELEMENTAL} always matches exactly one of these four - <b>unless</b>
 * someone later widens {@code elemental.json} to include some other tag (e.g. {@code pneumatic}),
 * in which case {@link #classify} returns {@code null} and callers fall back to the blanket
 * elemental value, since there is no more specific number configured for it.
 */
public enum ElementalKind {
    FIRE("fire", DamageTags.id("fire")),
    ELECTRIC("electric", DamageTags.id("electric")),
    COLD("cold", DamageTags.id("cold")),
    CHEMICAL("chemical", DamageTags.id("chemical"));

    private final String key;
    private final ResourceLocation tag;

    ElementalKind(String key, ResourceLocation tag) {
        this.key = key;
        this.tag = tag;
    }

    /** Lower-case toml key. */
    public String key() {
        return key;
    }

    public String label() {
        return key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);
    }

    /** First (in declaration order) whose tag the hit carries; null if none of the four match. */
    @Nullable
    public static ElementalKind classify(DamageSource source) {
        var tags = DamageTags.tagsOf(source);
        for (ElementalKind k : values()) {
            if (tags.contains(k.tag)) {
                return k;
            }
        }
        return null;
    }
}
