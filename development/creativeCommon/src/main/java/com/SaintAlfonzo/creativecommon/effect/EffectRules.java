package com.SaintAlfonzo.creativecommon.effect;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * CommonConfig {@code [effects]} {@code remap}, {@code removed}, {@code forceHudIcon} and {@code forceHudIconItems}
 * ({@code potions/effects #8, #9}).
 * Read by {@code mixin/LivingEntityEffectRemapMixin} (every {@code addEffect}), {@code mixin/FruitsDelightEffectFuncMixin}
 * and the client {@code mixin/GuiEffectIconMixin}. Each list is re-parsed when the config hands back a new list (Create's
 * Configs screen or a hand edit), as in {@link EffectCategories}.
 */
public final class EffectRules {

    private EffectRules() {}

    private static volatile List<? extends String> remapSource;
    private static volatile Map<ResourceLocation, ResourceLocation> remapIds = Map.of();
    private static volatile List<? extends String> removedSource;
    private static volatile Set<ResourceLocation> removedIds = Set.of();
    private static volatile List<? extends String> hudSource;
    private static volatile Set<ResourceLocation> hudIds = Set.of();

    /** True when {@code [effects] removed} lists the effect. */
    public static boolean isRemoved(MobEffect effect) {
        List<? extends String> raw;
        try {
            raw = CommonConfig.EFFECTS.removed.get();
        } catch (IllegalStateException notLoaded) {
            return false;
        }
        if (raw != removedSource) {
            removedIds = parseIds(raw);
            removedSource = raw;
        }
        return !removedIds.isEmpty() && removedIds.contains(ForgeRegistries.MOB_EFFECTS.getKey(effect));
    }

    /** The {@code [effects] remap} target for the effect, or null for none (or a target that isn't registered). */
    @Nullable
    public static MobEffect target(MobEffect effect) {
        List<? extends String> raw;
        try {
            raw = CommonConfig.EFFECTS.remap.get();
        } catch (IllegalStateException notLoaded) {
            return null;
        }
        if (raw != remapSource) {
            Map<ResourceLocation, ResourceLocation> parsed = new HashMap<>();
            for (String entry : raw) {
                int eq = entry.indexOf('=');
                ResourceLocation from = ResourceLocation.tryParse(entry.substring(0, eq));
                ResourceLocation to = ResourceLocation.tryParse(entry.substring(eq + 1));
                if (from != null && to != null) parsed.put(from, to);
            }
            remapIds = parsed;
            remapSource = raw;
        }
        if (remapIds.isEmpty()) return null;
        ResourceLocation to = remapIds.get(ForgeRegistries.MOB_EFFECTS.getKey(effect));
        MobEffect target = to == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(to);
        return target == effect ? null : target;
    }

    /** The instance with its effect remapped (same duration, level and flags), or the instance itself when not remapped. */
    public static MobEffectInstance remap(MobEffectInstance instance) {
        MobEffect to = target(instance.getEffect());
        if (to == null) return instance;
        return new MobEffectInstance(to, instance.getDuration(), instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), instance.showIcon());
    }

    /** True when {@code [effects] forceHudIcon} lists the effect. Client. */
    public static boolean forceHudIcon(MobEffect effect) {
        List<? extends String> raw;
        try {
            raw = CommonConfig.EFFECTS.forceHudIcon.get();
        } catch (IllegalStateException notLoaded) {
            return false;
        }
        if (raw != hudSource) {
            hudIds = parseIds(raw);
            hudSource = raw;
        }
        return !hudIds.isEmpty() && hudIds.contains(ForgeRegistries.MOB_EFFECTS.getKey(effect));
    }

    private static volatile List<? extends String> iconItemSource;
    private static volatile Set<String> iconItemEntries = Set.of();
    /** The item an entity is finishing using right now (server thread), set by {@code mixin/LivingEntityUseItemIconMixin}. */
    @Nullable
    private static Item usingItem;

    public static void setUsingItem(@Nullable Item item) {
        usingItem = item;
    }

    /**
     * The instance with its HUD icon on when it is added while an item in {@code [effects] forceHudIconItems} is being
     * finished, or the instance itself.
     */
    public static MobEffectInstance withItemIcon(MobEffectInstance instance) {
        Item item = usingItem;
        if (item == null || instance.showIcon() || !forceIconItem(item)) return instance;
        return new MobEffectInstance(instance.getEffect(), instance.getDuration(), instance.getAmplifier(),
                instance.isAmbient(), instance.isVisible(), true);
    }

    private static boolean forceIconItem(Item item) {
        List<? extends String> raw;
        try {
            raw = CommonConfig.EFFECTS.forceHudIconItems.get();
        } catch (IllegalStateException notLoaded) {
            return false;
        }
        if (raw != iconItemSource) {
            Set<String> entries = new HashSet<>();
            for (String entry : raw) entries.add(entry.trim());
            iconItemEntries = Set.copyOf(entries);
            iconItemSource = raw;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && (iconItemEntries.contains(id.getNamespace()) || iconItemEntries.contains(id.toString()));
    }

    private static Set<ResourceLocation> parseIds(List<? extends String> raw) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (String entry : raw) {
            ResourceLocation id = ResourceLocation.tryParse(entry.trim());
            if (id != null) ids.add(id);
        }
        return Set.copyOf(ids);
    }
}
