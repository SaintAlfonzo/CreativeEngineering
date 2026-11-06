package com.SaintAlfonzo.creativedelight.crops;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The rules behind the WTHIT "Growth" line for plants WTHIT doesn't cover (Creative Delight's
 * {@code compat.wthit.WthitCropGrowth}; this class stays in patcherMod, StateLines shares its globs).
 * Pure logic, no Minecraft types, so it runs in the headless harness (tools/test_crop_growth.sh).
 *
 * <p>WTHIT's own provider only knows {@code CropBlock} subclasses and five vanilla blocks by identity
 * (melon and pumpkin stem, nether wart, cocoa, sweet berry bush). Everything else with a growth
 * state - bushes, vines, hemp, tobacco, fruit-tree leaves - gets a rule here, from
 * {@code assets/creativeengineering/crop_growth.json}:
 *
 * <pre>{@code
 * { "blocks":     ["ns:id", "ns:*_glob", "#ns:block_tag"],
 *   "property":   "age",              // or "properties": ["age", "age1"], values are summed
 *   "stages":     ["a", "b", "c"],    // enum property: value names in growth order (default: integer property)
 *   "max":        4,                  // value that counts as fully grown (default: top of the property / last stage)
 *   "harvest":    3,                  // value from which it can be picked (default: max)
 *   "empty":      0,                  // values up to this show no line (a planted-but-empty stem)
 *   "requires":   {"persistent": "false"} }  // only while the blockstate has these values
 * }</pre>
 *
 * A value above {@code max} (hemp's top half, {@code top0}) counts as fully grown.
 */
public final class CropGrowth {

    private CropGrowth() {}

    public enum Stage { GROWING, HARVESTABLE, MATURE }

    /** What the line says: a percentage, plus whether it can be picked / is finished. */
    public record Display(int percent, Stage stage) {}

    /**
     * @param value   growth value read from the state (summed over the rule's properties)
     * @param max     value of a fully grown plant, above 0
     * @param harvest value from which it can be picked, {@code max} when only the full plant can be
     * @param empty   values at or below this show no line, -1 for none
     * @return null when the plant shows no line
     */
    public static Display evaluate(int value, int max, int harvest, int empty) {
        if (max <= 0 || value <= empty) {
            return null;
        }
        if (value >= max) {
            return new Display(100, Stage.MATURE);
        }
        // rounded like WTHIT's own "%.0f%%", but never 100 before the plant is actually grown
        int percent = Math.min(99, Math.round(Math.max(0, value) * 100f / max));
        return new Display(percent, value >= harvest ? Stage.HARVESTABLE : Stage.GROWING);
    }

    public record Rule(List<Pattern> ids, List<String> tags, List<String> properties, List<String> stages,
                       int max, int harvest, int empty, Map<String, String> requires) {

        public boolean matchesId(String id) {
            for (Pattern p : ids) {
                if (p.matcher(id).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    /** What to do with a bush-like block that has no rule: any {@code BushBlock} with an integer "age". */
    public static final String FALLBACK_PROPERTY = "age";

    /** Glob over a full block id: {@code *} matches anything, the rest is literal (Common's {@code Globs}). */
    public static Pattern glob(String text) {
        return com.SaintAlfonzo.creativecommon.util.Globs.glob(text);
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
                    ids.add(glob(s));
                }
            }
            List<String> props = new ArrayList<>();
            if (o.has("properties")) {
                o.getAsJsonArray("properties").forEach(p -> props.add(p.getAsString()));
            } else {
                props.add(o.has("property") ? o.get("property").getAsString() : FALLBACK_PROPERTY);
            }
            List<String> stages = null;
            if (o.has("stages")) {
                stages = new ArrayList<>();
                for (JsonElement s : o.getAsJsonArray("stages")) {
                    stages.add(s.getAsString());
                }
            }
            Map<String, String> requires = new LinkedHashMap<>();
            if (o.has("requires")) {
                o.getAsJsonObject("requires").entrySet().forEach(e -> requires.put(e.getKey(), e.getValue().getAsString()));
            }
            if (ids.isEmpty() && tags.isEmpty()) {
                throw new IllegalArgumentException("rule " + n + " matches no blocks");
            }
            out.add(new Rule(List.copyOf(ids), List.copyOf(tags), List.copyOf(props),
                    stages == null ? null : List.copyOf(stages),
                    o.has("max") ? o.get("max").getAsInt() : -1,
                    o.has("harvest") ? o.get("harvest").getAsInt() : -1,
                    o.has("empty") ? o.get("empty").getAsInt() : -1,
                    Map.copyOf(requires)));
        }
        return out;
    }
}
