package com.SaintAlfonzo.creativecommon.effect;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * CommonConfig {@code [effects] categoryOverrides}: a different {@link MobEffectCategory} for some effects, so a debuff
 * shows red and a buff blue wherever the category picks the colour (feature_writeup/effects.md 2.4). Read by
 * {@code mixin/MobEffectCategoryMixin} on every {@code getCategory()} / {@code isBeneficial()} call, so the lookup
 * is one map hit per effect; the parsed list is rebuilt when the config list changes (Create's Configs screen or a
 * hand edit), with no restart.
 */
public final class EffectCategories {

    private EffectCategories() {}

    /** The config list the maps below were built from (ConfigValue hands back the same list until it changes). */
    private static volatile List<? extends String> source;
    private static volatile Map<ResourceLocation, MobEffectCategory> byId = Map.of();
    /** Per effect: its override, or empty for none. Only effects with a registry name are cached. */
    private static final Map<MobEffect, Optional<MobEffectCategory>> BY_EFFECT = new ConcurrentHashMap<>();

    /** The override for this effect, or {@code null} to keep its own category. */
    public static MobEffectCategory override(MobEffect effect) {
        List<? extends String> raw;
        try {
            raw = CommonConfig.EFFECTS.categoryOverrides.get();
        } catch (IllegalStateException e) {
            return null; // config not loaded yet (very early startup)
        }
        if (raw != source) {
            Map<ResourceLocation, MobEffectCategory> parsed = new HashMap<>();
            for (String entry : raw) {
                int eq = entry.indexOf('=');
                ResourceLocation id = ResourceLocation.tryParse(entry.substring(0, eq));
                if (id != null) parsed.put(id, MobEffectCategory.valueOf(entry.substring(eq + 1)));
            }
            byId = parsed;
            BY_EFFECT.clear();
            source = raw;
        }
        Optional<MobEffectCategory> cached = BY_EFFECT.get(effect);
        if (cached == null) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (id == null) return null; // not registered yet: don't cache
            cached = Optional.ofNullable(byId.get(id));
            BY_EFFECT.put(effect, cached);
        }
        return cached.orElse(null);
    }
}
