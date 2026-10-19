package com.SaintAlfonzo.creativeengineering.time;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CEConfig.Time.Preset;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code [time] preset} (feature_writeup/time.md 3.9): SINGLEPLAYER and SERVER are full sets of every {@code [time]}
 * value; today they differ only in {@code monthsPerLevel} (1 vs 3). The preset is derived from the values:
 * <ul>
 *     <li>picking SINGLEPLAYER or SERVER writes that preset's values;</li>
 *     <li>editing a value labels the config with the preset it now matches, or CUSTOM if none;</li>
 *     <li>at load, a preset whose values were never written (the field edited with the game closed) is written then,
 *     as long as the values still match one preset; values matching none make it CUSTOM.</li>
 * </ul>
 */
public final class TimePresets {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Every value a preset sets, by toml path. */
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> VALUES = new LinkedHashMap<>();
    private static final Map<Preset, Map<String, Object>> PRESETS = new LinkedHashMap<>();

    private static Preset lastPreset;

    static {
        CEConfig.Time t = CEConfig.TIME;
        VALUES.put("enabled", t.enabled);
        VALUES.put("pauseWhenEmpty", t.pauseWhenEmpty);
        VALUES.put("hudTimeOfDay", t.hudTimeOfDay);
        VALUES.put("day.dayMinutes", t.dayMinutes);
        VALUES.put("day.nightMinutes", t.nightMinutes);
        VALUES.put("day.dayStart", t.dayStart);
        VALUES.put("day.nightStart", t.nightStart);
        VALUES.put("seasons.clock", t.seasonClock);
        VALUES.put("seasons.daysPerSubSeason", t.daysPerSubSeason);
        VALUES.put("seasons.startingSubSeason", t.startingSubSeason);
        VALUES.put("wine.clock", t.wineClock);
        VALUES.put("wine.monthsPerLevel", t.monthsPerLevel);
        VALUES.put("wine.maxLevel", t.maxLevel);
        VALUES.put("wine.boosts.cellarMultiplier", t.cellarMultiplier);

        Map<String, Object> singleplayer = new LinkedHashMap<>();
        VALUES.forEach((key, value) -> singleplayer.put(key, value.getDefault()));
        Map<String, Object> server = new LinkedHashMap<>(singleplayer);
        server.put("wine.monthsPerLevel", 3);
        PRESETS.put(Preset.SINGLEPLAYER, singleplayer);
        PRESETS.put(Preset.SERVER, server);
    }

    private TimePresets() {}

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CEConfig.COMMON_SPEC) {
            reconcile();
        }
    }

    /** Brings the preset label and the values into line (rules above); saves only when something changed. */
    public static synchronized void reconcile() {
        if (!CEConfig.COMMON_SPEC.isLoaded()) {
            return;
        }
        Preset chosen = CEConfig.TIME.preset.get();
        Preset matched = matching();
        Preset result;
        boolean write = false;
        if (chosen == Preset.CUSTOM || chosen == matched) {
            result = matched == null ? Preset.CUSTOM : matched;
        } else if (lastPreset != null && chosen != lastPreset) {
            result = chosen; // picked in the Configs screen or the file while running
            write = true;
        } else if (lastPreset == null && matched != null) {
            result = chosen; // first load: the field names another preset than the values, so it was picked
            write = true;
        } else {
            result = matched == null ? Preset.CUSTOM : matched; // values edited by hand
        }

        List<String> changes = new ArrayList<>();
        if (write) {
            PRESETS.get(result).forEach((key, target) -> setValue(key, target, changes));
        }
        if (result != chosen) {
            CEConfig.TIME.preset.set(result);
            changes.add("preset " + chosen + " -> " + result);
        }
        lastPreset = result;
        if (!changes.isEmpty()) {
            CEConfig.COMMON_SPEC.save();
            changes.forEach(change -> LOGGER.info("[time] {}", change));
        }
    }

    /** The preset whose every value equals the current ones, or null. */
    private static Preset matching() {
        for (Map.Entry<Preset, Map<String, Object>> preset : PRESETS.entrySet()) {
            boolean all = true;
            for (Map.Entry<String, Object> value : preset.getValue().entrySet()) {
                if (!same(VALUES.get(value.getKey()).get(), value.getValue())) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return preset.getKey();
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setValue(String key, Object target, List<String> changes) {
        ForgeConfigSpec.ConfigValue value = VALUES.get(key);
        Object current = value.get();
        if (!same(current, target)) {
            value.set(target);
            changes.add(key + " " + current + " -> " + target);
        }
    }

    private static boolean same(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) {
            return Math.abs(x.doubleValue() - y.doubleValue()) < 1e-9;
        }
        return Objects.equals(a, b);
    }
}
