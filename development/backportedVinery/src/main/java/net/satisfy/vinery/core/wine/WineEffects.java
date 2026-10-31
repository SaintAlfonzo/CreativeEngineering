package net.satisfy.vinery.core.wine;

import com.mojang.logging.LogUtils;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.satisfy.vinery.platform.PlatformHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WineEffects {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final List<String> DEFAULTS = List.of(
            "vinery:apple_cider|minecraft:strength|0|1800",
            "vinery:apple_wine|minecraft:resistance|0|1800",
            "vinery:glowing_wine|minecraft:glowing|0|1800",
            "vinery:solaris_wine|minecraft:health_boost|0|1800",
            "vinery:kelp_cider|vinery:water_walker|0|1800",
            "vinery:eiswein|vinery:frosty_armor|0|1800",
            "vinery:aegis_wine|vinery:armor_effect|0|1800",
            "vinery:villagers_fright|minecraft:bad_omen|0|1800",
            "vinery:clark_wine|vinery:double_jump|0|1800",
            "vinery:jellie_wine|minecraft:regeneration+minecraft:absorption+minecraft:resistance+minecraft:fire_resistance|0|1800",
            "vinery:noir_wine|minecraft:jump_boost|0|1800",
            "vinery:red_wine|minecraft:slow_falling|0|1800",
            "vinery:strad_wine|minecraft:night_vision|0|1800",
            "vinery:cherry_wine|minecraft:invisibility|0|1800",
            "vinery:cristel_wine|minecraft:water_breathing|0|1800",
            "vinery:lilitu_wine|vinery:party_effect|0|1800",
            "vinery:jo_special_mixture|vinery:climbing_effect|0|1800",
            "vinery:bolvar_wine|vinery:lava_walker|0|1800",
            "vinery:magnetic_wine|vinery:magnet|0|1800",
            "vinery:stal_wine|minecraft:health_boost|0|1800",
            "vinery:chenet_wine|vinery:climbing_effect|0|1800",
            "vinery:bottle_mojang_noir|vinery:experience_effect|0|1800",
            "vinery:chorus_wine|vinery:teleport|0|10",
            "vinery:creepers_crush|vinery:creeper_effect|0|100",
            "vinery:mellohi_wine|minecraft:instant_health|0|1"
    );

    /** One or more effects ({@code a+b+c} in the entry), all at the same level and duration. */
    public record WineEffect(List<MobEffect> effects, int amplifier, int duration) {
        /** The first effect, for callers that show one. */
        public MobEffect effect() {
            return effects.get(0);
        }
    }

    private static List<? extends String> cachedSource;
    private static Map<ResourceLocation, WineEffect> cache = Map.of();

    public static @Nullable WineEffect get(Item item) {
        return all().get(ForgeRegistries.ITEMS.getKey(item));
    }

    /** Every parsed {@code effects} entry, wine id -> effect. The pack's JEED provider recipes read this (alcohol.md B7.5). */
    public static Map<ResourceLocation, WineEffect> all() {
        List<? extends String> source = PlatformHelper.getWineEffects();
        if (source != cachedSource) {
            cache = parse(source);
            cachedSource = source;
        }
        return cache;
    }

    /** {@link #all()} grouped as effect id -> wine ids, strings only, for KubeJS (Rhino reads plain strings safely). */
    public static Map<String, List<String>> winesByEffect() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        all().forEach((wine, effect) -> {
            for (MobEffect e : effect.effects()) {
                ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(e);
                if (id != null) {
                    out.computeIfAbsent(id.toString(), k -> new ArrayList<>()).add(wine.toString());
                }
            }
        });
        return out;
    }

    private static Map<ResourceLocation, WineEffect> parse(List<? extends String> entries) {
        Map<ResourceLocation, WineEffect> result = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] parts = entry.split("\\|");
            ResourceLocation wine = parts.length == 4 ? ResourceLocation.tryParse(parts[0].trim()) : null;
            List<MobEffect> effects = new ArrayList<>();
            if (parts.length == 4) {
                for (String part : parts[1].split("\\+")) {
                    ResourceLocation effectId = ResourceLocation.tryParse(part.trim());
                    MobEffect effect = effectId == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                    if (effect == null) {
                        effects.clear();
                        break;
                    }
                    effects.add(effect);
                }
            }
            if (wine == null || effects.isEmpty()) {
                LOGGER.warn("Ignoring invalid wine effect entry '{}', expected wine|effect[+effect...]|amplifier|durationTicks", entry);
                continue;
            }
            try {
                result.put(wine, new WineEffect(List.copyOf(effects), Math.max(0, Integer.parseInt(parts[2].trim())), Math.max(1, Integer.parseInt(parts[3].trim()))));
            } catch (NumberFormatException e) {
                LOGGER.warn("Ignoring wine effect entry '{}': amplifier and duration must be numbers", entry);
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
