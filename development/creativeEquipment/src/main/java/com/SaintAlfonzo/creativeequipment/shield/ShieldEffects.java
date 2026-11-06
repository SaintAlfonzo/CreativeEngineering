package com.SaintAlfonzo.creativeequipment.shield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * The on-block potion effects: parses a tier's {@code shielderEffects} / {@code attackerEffects}
 * config string and applies it. Format (comma-separated entries):
 * {@code effectId;durationTicks[;amplifier[;category|category]]}.
 *
 * <p>Kept as one plain string per list (not a toml list) so it stays editable in Create's
 * in-game Configs screen, which only has a widget for raw strings via {@code StringEntry}.
 * Effect ids are resolved when a hit lands, not when the string is parsed, because modded
 * MobEffects aren't registered yet when the config first loads (same reason as armor_effects.js).
 */
public final class ShieldEffects {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** {@code categories} empty = every category. */
    public record Spec(ResourceLocation effect, int duration, int amplifier, Set<ShieldCategory> categories) {
        boolean appliesTo(ShieldCategory category) {
            return categories.isEmpty() || categories.contains(category);
        }
    }

    // Parsed lists keyed by the raw config string, so a live edit simply misses the cache and
    // re-parses. Warnings are remembered so a bad entry logs once, not once per blocked hit.
    private static final Map<String, List<Spec>> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private ShieldEffects() {}

    public static List<Spec> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return CACHE.computeIfAbsent(raw, ShieldEffects::parseUncached);
    }

    private static List<Spec> parseUncached(String raw) {
        List<Spec> out = new ArrayList<>();
        for (String entry : raw.split(",")) {
            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }
            String[] f = entry.split(";", -1);
            try {
                ResourceLocation id = ResourceLocation.tryParse(f[0].trim());
                if (id == null) {
                    throw new IllegalArgumentException("bad effect id");
                }
                int duration = Integer.parseInt(f[1].trim());
                int amplifier = f.length > 2 && !f[2].isBlank() ? Integer.parseInt(f[2].trim()) : 0;
                EnumSet<ShieldCategory> cats = EnumSet.noneOf(ShieldCategory.class);
                if (f.length > 3) {
                    for (String name : f[3].split("\\|")) {
                        if (name.isBlank()) {
                            continue;
                        }
                        // A filter token is a fine category (melee, projectile, ...) or a whole group
                        // (physical, fire, magic = every category tuned under it).
                        ShieldCategory c = ShieldCategory.byKey(name.trim());
                        ShieldGroup g = c == null ? ShieldGroup.byKey(name.trim()) : null;
                        if (c == null && g == null) {
                            throw new IllegalArgumentException("unknown category '" + name.trim() + "'");
                        }
                        if (c != null) {
                            cats.add(c);
                        } else {
                            for (ShieldCategory member : ShieldCategory.values()) {
                                if (member.group() == g) {
                                    cats.add(member);
                                }
                            }
                        }
                    }
                }
                if (duration <= 0 || amplifier < 0) {
                    throw new IllegalArgumentException("duration must be > 0 and amplifier >= 0");
                }
                out.add(new Spec(id, duration, amplifier, cats));
            } catch (RuntimeException e) {
                // includes ArrayIndexOutOfBounds from a missing duration field
                warnOnce("[shields] ignoring effect entry '" + entry + "' (" + e.getMessage()
                        + ") - expected effectId;durationTicks[;amplifier[;category|category]]");
            }
        }
        return Collections.unmodifiableList(out);
    }

    /** Applies every spec in {@code raw} that matches {@code category} to {@code target}. */
    public static void apply(@Nullable LivingEntity target, String raw, ShieldCategory category) {
        if (target == null) {
            return;
        }
        for (Spec spec : parse(raw)) {
            if (!spec.appliesTo(category)) {
                continue;
            }
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(spec.effect());
            if (effect == null) {
                warnOnce("[shields] unknown effect id '" + spec.effect() + "' in a shield effect list - skipped");
                continue;
            }
            target.addEffect(new MobEffectInstance(effect, spec.duration(), spec.amplifier()));
        }
    }

    private static void warnOnce(String message) {
        if (WARNED.add(message)) {
            LOGGER.warn(message);
        }
    }
}
