package com.SaintAlfonzo.creativecompat.surface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * One-time config move to the 2026-11-15 pass-through model (plan_post_scratch_1115b.md C1 and D), on the toml text
 * before Forge loads the file (like {@link LeafPileMigration}, which runs first):
 * <ul>
 *   <li>C1: in every {@code [surface.passthrough.<tier>]} table that has no {@code surroundedAt} key yet (a file from
 *       before the change), {@code neighbours = "RING"} becomes {@code "SIDES"}, {@code "CUBE"} becomes {@code "FACES"},
 *       and {@code denseFrom = 6} (the old default) becomes 3 (the new one). Another denseFrom is an edit and stays.</li>
 *   <li>D: the {@code [surface.passthrough.leaf_pile]} table goes; its edited values move into
 *       {@code [surface.passthrough.layers]}: {@code minLayers} (unless the old default 4: the new default 2 applies),
 *       {@code fall}, {@code depthStriderPasses} and {@code walk} as {@code layerMaxSlow} (each only when not its old
 *       default).</li>
 * </ul>
 * Forge then adds the new keys and rewrites the layout when it corrects the file. Plain Java (no game classes) so the
 * headless harness can test {@link #migrate}.
 */
public final class PassConfigMigration {

    private PassConfigMigration() {}

    static final String PREFIX = "[surface.passthrough.";
    static final String LEAF = "[surface.passthrough.leaf_pile]";
    static final String LAYERS = "[surface.passthrough.layers]";
    static final int OLD_DENSE_FROM = 6;
    static final int NEW_DENSE_FROM = 3;

    private static final Pattern KEY = Pattern.compile("^(\\s*)([A-Za-z]+)\\s*=\\s*(.+?)\\s*(#.*)?$");

    /** What {@link #migrate} did; {@code notes} says what changed, for the log. */
    public record Result(String text, boolean changed, List<String> notes) {}

    @Nullable
    public static Result run(Path file) throws IOException {
        if (!Files.isRegularFile(file)) return null;
        String text = Files.readString(file, StandardCharsets.UTF_8);
        Result r = migrate(text);
        if (!r.changed()) return null;
        Files.writeString(file, r.text(), StandardCharsets.UTF_8);
        return r;
    }

    public static Result migrate(String text) {
        List<String> lines = new ArrayList<>(List.of(text.split("\n", -1)));
        List<String> notes = new ArrayList<>();
        boolean changed = false;

        // C1: neighbour names and the denseFrom default, per tier table without surroundedAt
        for (int h = 0; h < lines.size(); h++) {
            String header = header(lines.get(h));
            if (header == null || !header.startsWith(PREFIX) || header.equals(LEAF) || header.equals(LAYERS)) continue;
            int end = bodyEnd(lines, h);
            if (keyLine(lines, h, end, "surroundedAt") >= 0) continue;
            String table = header.substring(PREFIX.length(), header.length() - 1);
            int n = keyLine(lines, h, end, "neighbours");
            if (n >= 0) {
                Matcher m = KEY.matcher(lines.get(n));
                m.matches();
                String v = unquote(m.group(3)).toUpperCase(Locale.ROOT);
                String to = v.equals("RING") ? "SIDES" : v.equals("CUBE") ? "FACES" : null;
                if (to != null) {
                    lines.set(n, m.group(1) + "neighbours = \"" + to + "\"");
                    notes.add(table + " neighbours " + v + " -> " + to);
                    changed = true;
                }
            }
            int d = keyLine(lines, h, end, "denseFrom");
            if (d >= 0) {
                Matcher m = KEY.matcher(lines.get(d));
                m.matches();
                if (OLD_DENSE_FROM == parseInt(m.group(3), -1)) {
                    lines.set(d, m.group(1) + "denseFrom = " + NEW_DENSE_FROM);
                    notes.add(table + " denseFrom 6 -> 3");
                    changed = true;
                }
            }
        }

        // D: leaf_pile -> layers
        int leaf = find(lines, LEAF);
        if (leaf >= 0) {
            int end = bodyEnd(lines, leaf);
            Map<String, String> carry = new LinkedHashMap<>();
            String minLayers = value(lines, leaf, end, "minLayers");
            if (minLayers != null && parseInt(minLayers, 4) != 4) carry.put("minLayers", String.valueOf(clamp(parseInt(minLayers, 2), 1, 16)));
            String fall = value(lines, leaf, end, "fall");
            if (fall != null && parseDouble(fall, 1.0) != 1.0) carry.put("fall", fall);
            String ds = value(lines, leaf, end, "depthStriderPasses");
            if (ds != null && ds.equals("false")) carry.put("depthStriderPasses", "false");
            String walk = value(lines, leaf, end, "walk");
            if (walk != null && parseDouble(walk, 0.45) != 0.45) {
                carry.put("layerMaxSlow", String.valueOf(Math.max(0.05, Math.min(1.0, parseDouble(walk, 0.5)))));
            }
            // the table and its own comment block above the header (at the header's indent)
            int start = leaf;
            String indent = leading(lines.get(leaf));
            while (start > 0 && lines.get(start - 1).startsWith(indent + "#")
                    && leading(lines.get(start - 1)).equals(indent)) start--;
            List<String> out = new ArrayList<>(lines.subList(0, start));
            List<String> rest = new ArrayList<>(lines.subList(end, lines.size()));
            while (!out.isEmpty() && out.get(out.size() - 1).isBlank() && !rest.isEmpty() && rest.get(0).isBlank()) {
                rest.remove(0);
            }
            out.addAll(rest);
            lines = out;
            notes.add("[surface.passthrough.leaf_pile] removed" + (carry.isEmpty() ? "" : ", carried to layers: " + carry));
            changed = true;
            if (!carry.isEmpty()) {
                int lh = find(lines, LAYERS);
                if (lh >= 0) {
                    int lend = bodyEnd(lines, lh);
                    String in = leading(lines.get(lh)) + "\t";
                    for (Map.Entry<String, String> e : carry.entrySet()) {
                        int k = keyLine(lines, lh, lend, e.getKey());
                        if (k >= 0) {
                            lines.set(k, leading(lines.get(k)) + e.getKey() + " = " + e.getValue());
                        } else {
                            lines.add(lh + 1, in + e.getKey() + " = " + e.getValue());
                            lend++;
                        }
                    }
                } else {
                    while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) lines.remove(lines.size() - 1);
                    lines.add("");
                    lines.add(LAYERS);
                    for (Map.Entry<String, String> e : carry.entrySet()) lines.add("\t" + e.getKey() + " = " + e.getValue());
                    lines.add("");
                }
            }
        }
        return new Result(changed ? String.join("\n", lines) : text, changed, notes);
    }

    /** The table header on this line (comment stripped, trimmed), or null. */
    @Nullable
    private static String header(String line) {
        String t = line.trim();
        if (!t.startsWith("[")) return null;
        int hash = t.indexOf('#');
        if (hash > 0) t = t.substring(0, hash).trim();
        return t;
    }

    private static int find(List<String> lines, String header) {
        for (int i = 0; i < lines.size(); i++) {
            if (header.equals(header(lines.get(i)))) return i;
        }
        return -1;
    }

    /**
     * End (exclusive) of the table at {@code h}: the next header, or the next comment indented no deeper than the
     * header (the next table's comment block). Blank lines at the end stay outside.
     */
    private static int bodyEnd(List<String> lines, int h) {
        int ind = leading(lines.get(h)).length();
        int i = h + 1;
        int lastBody = h;
        while (i < lines.size()) {
            String l = lines.get(i);
            if (!l.isBlank()) {
                if (header(l) != null || (l.trim().startsWith("#") && leading(l).length() <= ind)) break;
                lastBody = i;
            }
            i++;
        }
        return lastBody + 1;
    }

    private static int keyLine(List<String> lines, int h, int end, String key) {
        for (int i = h + 1; i < end; i++) {
            Matcher m = KEY.matcher(lines.get(i));
            if (m.matches() && m.group(2).equals(key)) return i;
        }
        return -1;
    }

    @Nullable
    private static String value(List<String> lines, int h, int end, String key) {
        int i = keyLine(lines, h, end, key);
        if (i < 0) return null;
        Matcher m = KEY.matcher(lines.get(i));
        m.matches();
        return unquote(m.group(3));
    }

    private static String unquote(String v) {
        v = v.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) return v.substring(1, v.length() - 1);
        return v;
    }

    private static String leading(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(0, i);
    }

    private static int parseInt(String s, int dflt) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    private static double parseDouble(String s, double dflt) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return dflt;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
