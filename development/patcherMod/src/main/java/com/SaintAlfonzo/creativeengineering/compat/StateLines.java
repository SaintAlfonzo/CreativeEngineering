package com.SaintAlfonzo.creativeengineering.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The rules behind the WTHIT state lines ({@link WthitStateLines}): a number a block keeps in its state, shown as
 * "Label: n / max". Pure logic, no Minecraft types. From {@code assets/creativeengineering/state_lines.json}:
 *
 * <pre>{@code
 * { "blocks":   ["ns:id", "*glob*", "#ns:block_tag"],
 *   "except":   ["minecraft:water_cauldron"],   // ids / globs the rule skips
 *   "property": "servings",                     // an integer property; a block without it skips the rule
 *   "label":    "servings",                     // lang key creativeengineering.wthit.state.<label>, and the line id
 *   "mode":     "count" }                       // count | taken | plus_one, see Mode
 * }</pre>
 *
 * Every matching rule adds a line, in file order; a later rule whose label or property the block already shows is
 * skipped, so specific rules go first.
 */
public final class StateLines {

    private StateLines() {}

    /** How the property's value turns into "n / max" (min and top are the property's own range). */
    public enum Mode {
        /** the value is the count: value / top (feast servings left, eggs, cauldron level) */
        COUNT,
        /** the value counts what's gone: (top - value + 1) / (top - min + 1) (cake bites taken) */
        TAKEN,
        /** the value is one short of the count: (value + 1) / (top + 1) (Brewin' cheese wheel and pizza) */
        PLUS_ONE
    }

    public record Shown(int value, int max) {}

    public static Shown evaluate(Mode mode, int value, int min, int top) {
        return switch (mode) {
            case COUNT -> new Shown(value, top);
            case TAKEN -> new Shown(top - value + 1, top - min + 1);
            case PLUS_ONE -> new Shown(value + 1, top + 1);
        };
    }

    public record Rule(List<Pattern> ids, List<String> tags, List<Pattern> except, String property, String label,
                       Mode mode) {

        public boolean matchesId(String id) {
            return any(ids, id);
        }

        public boolean excludes(String id) {
            return any(except, id);
        }

        private static boolean any(List<Pattern> patterns, String id) {
            for (Pattern p : patterns) {
                if (p.matcher(id).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static List<Rule> parse(JsonObject root) {
        List<Rule> out = new ArrayList<>();
        JsonArray rules = root.getAsJsonArray("rules");
        if (rules == null) {
            return out;
        }
        int n = 0;
        for (JsonElement el : rules) {
            n++;
            JsonObject o = el.getAsJsonObject();
            List<Pattern> ids = new ArrayList<>();
            List<String> tags = new ArrayList<>();
            for (JsonElement b : o.getAsJsonArray("blocks")) {
                String s = b.getAsString();
                if (s.startsWith("#")) {
                    tags.add(s.substring(1));
                } else {
                    ids.add(com.SaintAlfonzo.creativeengineering.util.Globs.glob(s));
                }
            }
            List<Pattern> except = new ArrayList<>();
            if (o.has("except")) {
                o.getAsJsonArray("except").forEach(e -> except.add(com.SaintAlfonzo.creativeengineering.util.Globs.glob(e.getAsString())));
            }
            if (ids.isEmpty() && tags.isEmpty()) {
                throw new IllegalArgumentException("rule " + n + " matches no blocks");
            }
            if (!o.has("property") || !o.has("label")) {
                throw new IllegalArgumentException("rule " + n + " needs a property and a label");
            }
            Mode mode = o.has("mode") ? Mode.valueOf(o.get("mode").getAsString().toUpperCase(Locale.ROOT)) : Mode.COUNT;
            out.add(new Rule(List.copyOf(ids), List.copyOf(tags), List.copyOf(except), o.get("property").getAsString(),
                    o.get("label").getAsString(), mode));
        }
        return out;
    }
}
