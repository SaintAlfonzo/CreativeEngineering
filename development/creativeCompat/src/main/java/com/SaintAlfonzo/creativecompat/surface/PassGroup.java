package com.SaintAlfonzo.creativecompat.surface;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What a projectile does in pass-through webs (plan_passthrough.md 5.1). Every group but {@link #EXEMPT} takes the
 * web slowdown (whole velocity x walk, downward also x fall; dense values when dense) in every web of a tier with
 * {@code projectiles = true}; the group decides what happens in a surrounded web ({@link #SNAG}: in any web).
 * Chosen by the entity-type tag {@code creativecompat:passthrough/projectile/<key>} (phase 4 fills them), else by
 * {@link #byClassChain} (base-class default). {@code surface.PassProjectiles} applies it.
 *
 * <p>Plain Java (no game classes) so the headless harness can test the defaults.
 */
public enum PassGroup {
    /** Surrounded web: the impact runs as a block hit at the cell center, then the projectile is gone. */
    BREAK,
    /** Surrounded web: no horizontal motion, on the cell's center line, sinks at the dense fall speed. */
    SETTLE,
    /** Any web: held at the center of the web cell until reeled in, picked up or the web breaks. */
    SNAG,
    /** Surrounded web: held where it entered until its flight time ends (fireworks). */
    LINGER,
    /** Surrounded web: an arrow goes into its stuck state where it is (5.3). */
    HANG,
    /** Slowdown only. */
    SLOW,
    /** Not slowed at all (modded bullets and munitions). */
    EXEMPT;

    /** Tag path after {@code creativecompat:} ({@code passthrough/projectile/break}). */
    public String tagPath() {
        return "passthrough/projectile/" + name().toLowerCase(Locale.ROOT);
    }

    /**
     * Base-class defaults (plan 5.1 "with no tag"), most specific class first. Vanilla classes get their table group
     * so the system works before the phase 4 tags exist; Quark pickarangs and the Botania chakram are matched by name
     * (optional mods). Arrow-like {@code HANG}, throwable and self-propelled {@code BREAK}, anything else
     * {@code SLOW}.
     */
    private static final Map<String, PassGroup> BY_CLASS = Map.ofEntries(
            Map.entry("net.minecraft.world.entity.projectile.FishingHook", SNAG),
            Map.entry("net.minecraft.world.entity.projectile.FireworkRocketEntity", LINGER),
            Map.entry("net.minecraft.world.entity.projectile.ThrownPotion", SETTLE),
            Map.entry("net.minecraft.world.entity.projectile.ThrownExperienceBottle", SETTLE),
            Map.entry("vazkii.quark.content.tools.entity.rang.AbstractPickarang", SNAG),
            Map.entry("vazkii.botania.common.entity.ThornChakramEntity", SNAG),
            Map.entry("net.minecraft.world.entity.projectile.AbstractArrow", HANG),
            Map.entry("net.minecraft.world.entity.projectile.ThrowableProjectile", BREAK),
            Map.entry("net.minecraft.world.entity.projectile.AbstractHurtingProjectile", BREAK),
            Map.entry("net.minecraft.world.entity.projectile.LlamaSpit", BREAK),
            Map.entry("net.minecraft.world.entity.projectile.ShulkerBullet", BREAK));

    /** The default for a class chain (the entity's class, then each superclass, as binary names). */
    public static PassGroup byClassChain(List<String> chain) {
        for (String name : chain) {
            PassGroup g = BY_CLASS.get(name);
            if (g != null) return g;
        }
        return SLOW;
    }

    /** The class chain of {@code c} up to (excluding) {@code Object}. */
    public static List<String> chainOf(Class<?> c) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) out.add(k.getName());
        return out;
    }
}
