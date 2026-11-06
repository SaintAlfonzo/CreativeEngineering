package com.SaintAlfonzo.creativecalendar;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One-time move of the calendar keys out of patcherMod's config (feature_writeup/creative_calendar.md 4.3, plan
 * scratch_bugfix_1103 7.7). At construction, before Forge loads the COMMON configs: read [seasons], [time] (with its
 * subsections), [crop_seasons] and the strays perf.seasonsMeltIntervalTicks, wthit.cropSeasons and wthit.cropOutOfSeason
 * from config/creativeengineering-common.toml, keep the values, and strip those lines (with their comments) from that file.
 * When Creative Calendar's own file loads, each kept value whose new key still holds the default is written into it.
 * A value already edited in the new file wins. Logs what moved; silent when the old file has none of the keys.
 */
final class CalendarConfigMigration {

    private static final Pattern SECTION = Pattern.compile("^\\[(time|seasons|crop_seasons)(\\.[A-Za-z_.]+)?\\]\\s*$");
    private static final Pattern TOP = Pattern.compile("^\\[[^\\]]+\\]\\s*$");
    private static final Pattern STRAY = Pattern.compile("^\\s*(seasonsMeltIntervalTicks|cropSeasons|cropOutOfSeason)\\s*=");
    private static final List<String> SECTIONS = List.of("time", "seasons", "crop_seasons");
    private static final Map<String, List<String>> STRAYS = Map.of(
            "seasonsMeltIntervalTicks", List.of("perf", "seasonsMeltIntervalTicks"),
            "cropSeasons", List.of("wthit", "cropSeasons"),
            "cropOutOfSeason", List.of("wthit", "cropOutOfSeason"));

    /** New-file path -> value read from the old file. Filled at construction, consumed once on the first load. */
    private static final Map<List<String>, Object> PENDING = new LinkedHashMap<>();

    private CalendarConfigMigration() {}

    /** Construction: read and strip. */
    static void readAndStrip() {
        Path old = FMLPaths.CONFIGDIR.get().resolve("creativeengineering-common.toml");
        if (!Files.isRegularFile(old)) return;
        String text;
        try {
            text = Files.readString(old, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }
        Config parsed;
        try {
            parsed = new TomlParser().parse(text);
        } catch (RuntimeException e) {
            CreativeCalendar.LOGGER.warn("[creativecalendar] config/creativeengineering-common.toml does not parse, moved keys left alone: {}", e.toString());
            return;
        }
        for (String s : SECTIONS) {
            Object sec = parsed.get(s);
            if (sec instanceof UnmodifiableConfig c) collect(c, new ArrayList<>(List.of(s)));
        }
        STRAYS.forEach((key, target) -> {
            for (UnmodifiableConfig.Entry e : parsed.entrySet()) {
                if (e.getValue() instanceof UnmodifiableConfig c && c.contains(key)) PENDING.put(target, c.get(key));
            }
        });
        if (PENDING.isEmpty()) return;

        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        boolean[] drop = new boolean[lines.size()];
        for (int i = 0; i < lines.size(); ) {
            if (SECTION.matcher(lines.get(i)).matches()) {
                int start = commentStart(lines, i);
                int j = i + 1;
                while (j < lines.size() && !(TOP.matcher(lines.get(j)).matches() && !SECTION.matcher(lines.get(j)).matches())) j++;
                int end = j < lines.size() ? commentStart(lines, j) : j;
                for (int x = start; x < end; x++) drop[x] = true;
                i = j;
                continue;
            }
            if (STRAY.matcher(lines.get(i)).find()) for (int x = commentStart(lines, i); x <= i; x++) drop[x] = true;
            i++;
        }
        StringBuilder out = new StringBuilder();
        int blank = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (drop[i]) continue;
            String l = lines.get(i);
            blank = l.isBlank() ? blank + 1 : 0;
            if (blank > 1) continue;
            out.append(l);
            if (i < lines.size() - 1) out.append('\n');
        }
        try {
            Files.writeString(old, out.toString(), StandardCharsets.UTF_8);
            CreativeCalendar.LOGGER.info("[creativecalendar] took {} moved keys out of config/creativeengineering-common.toml", PENDING.size());
        } catch (IOException e) {
            CreativeCalendar.LOGGER.warn("[creativecalendar] could not rewrite config/creativeengineering-common.toml: {}", e.toString());
        }
    }

    private static void collect(UnmodifiableConfig c, List<String> path) {
        for (UnmodifiableConfig.Entry e : c.entrySet()) {
            List<String> p = new ArrayList<>(path);
            p.add(e.getKey());
            if (e.getValue() instanceof UnmodifiableConfig sub) collect(sub, p);
            else PENDING.put(p, e.getValue());
        }
    }

    private static int commentStart(List<String> lines, int i) {
        int j = i;
        while (j > 0 && lines.get(j - 1).stripLeading().startsWith("#")) j--;
        return j;
    }

    /** First load of creativecalendar-common.toml: write the kept values over defaults. */
    static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != CalendarConfig.SPEC || PENDING.isEmpty()) return;
        List<String> moved = new ArrayList<>();
        PENDING.forEach((path, value) -> {
            Object v = CalendarConfig.SPEC.getValues().get(path);
            Object spec = CalendarConfig.SPEC.getSpec().get(path);
            if (!(v instanceof ForgeConfigSpec.ConfigValue<?> cv) || !(spec instanceof ForgeConfigSpec.ValueSpec vs)) return;
            Object now = cv.get();
            if (!same(now, vs.getDefault()) || same(now, value)) return;
            Object fixed = coerce(value, now);
            if (fixed == null || !vs.test(fixed)) return;
            set(cv, fixed);
            moved.add(String.join(".", path) + " = " + fixed);
        });
        PENDING.clear();
        if (!moved.isEmpty()) {
            CalendarConfig.SPEC.save();
            CreativeCalendar.LOGGER.info("[creativecalendar] moved edited values into config/{}-common.toml: {}", CreativeCalendar.MOD_ID, moved);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void set(ForgeConfigSpec.ConfigValue<T> cv, Object v) {
        cv.set((T) v);
    }

    private static boolean same(Object a, Object b) {
        if (a instanceof Number x && b instanceof Number y) return x.doubleValue() == y.doubleValue();
        if (a instanceof Enum<?> e && b != null) return e.name().equals(b.toString());
        return a == null ? b == null : a.equals(b);
    }

    /** The parsed toml value in the type the spec holds (ints read as Integer or Long, enums as strings). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerce(Object value, Object like) {
        if (like instanceof Integer && value instanceof Number n) return n.intValue();
        if (like instanceof Long && value instanceof Number n) return n.longValue();
        if (like instanceof Double && value instanceof Number n) return n.doubleValue();
        if (like instanceof Enum<?> e && value != null) {
            try {
                return Enum.valueOf((Class) e.getDeclaringClass(), value.toString());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        if (like instanceof List && value instanceof List) return value;
        if (like != null && like.getClass().isInstance(value)) return value;
        return null;
    }
}
