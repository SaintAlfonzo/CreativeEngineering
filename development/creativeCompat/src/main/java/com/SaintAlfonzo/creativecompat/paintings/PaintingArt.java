package com.SaintAlfonzo.creativecompat.paintings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immersive Paintings' Datapacks page entries for every modded painting variant, built from the art in the installed jars
 * (feature_writeup/immersive_paintings.md, TODO licence/compat #1). Pure Java (no Minecraft classes), so it runs in the
 * offline harness; {@link PaintingPack} feeds it the registry and the jars and serves the result.
 *
 * <p>Immersive reads {@code data/<ns>/paintings/**.png} plus a sibling json {name, author, width, height (blocks),
 * resolution (px per block)}. Each variant {@code ns:path} becomes {@code data/creativeengineering/paintings/<ns>/<path>.png}
 * + .json: the ids the old generated datapack used, so paintings already placed keep their picture. Same rules as the old
 * generator (tools/gen_immersive_paintings.py, which now writes only the vanilla ones):
 * <ul>
 *   <li>texture {@code assets/<ns>/textures/painting/<path>.png}, the last source that has it wins;</li>
 *   <li>an animated strip keeps its first frame; art off the block grid is stretched to 16 px per block like vanilla;</li>
 *   <li>under 32 px per block it is scaled up (nearest neighbour, whole factor) to 32, the page's default filter;</li>
 *   <li>name: lang {@code painting.<ns>.<path>.title}, else the path in title case; author: lang {@code .author} /
 *       {@code .artist}, then DarkPaintings-style section credits ({@code <ns>.author.<x>} followed by that artist's titles
 *       in file order), then the mod's display name. "Unknown" and "-" count as missing.</li>
 * </ul>
 */
public final class PaintingArt {

    public static final String OUT_NAMESPACE = "creativeengineering";
    public static final int TARGET_RES = 32;

    /** A registered painting variant: id parts and size in pixels (16 per block). */
    public record Variant(String namespace, String path, int width, int height) {
        String id() {
            return namespace + ":" + path;
        }
    }

    /**
     * Somewhere art and lang can come from: a mod jar, or a jar, zip or folder named in the config. {@code read} takes a
     * path inside the jar ({@code assets/...}) and returns its bytes, or null. {@code displayName} is the mod's name, or
     * null for a non-mod source. {@code hasArt} says whether it has {@code assets/<ns>/textures/painting/}.
     */
    public record Source(String label, String displayName, Function<String, byte[]> read, Predicate<String> hasArt) {}

    /** What a build made, for the log line. */
    public record Result(Map<String, byte[]> files, Map<String, Integer> byNamespace, int upscaled, List<String> notes,
                         List<String> problems) {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Pattern SECTION_AUTHOR = Pattern.compile("^([a-z0-9_]+)\\.author\\.[a-z0-9_]+$");
    private static final Pattern SECTION_TITLE = Pattern.compile("^painting\\.([a-z0-9_]+)\\.(.+)\\.title$");

    private PaintingArt() {}

    /**
     * @param variants every painting variant outside {@code minecraft} (vanilla ones come from the pack's datapack)
     * @param sources  lowest priority first: mod jars, then the config's extra sources in order
     */
    public static Result build(List<Variant> variants, List<Source> sources) {
        Map<String, byte[]> files = new HashMap<>();
        Map<String, Integer> byNs = new TreeMap<>();
        List<String> notes = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        Map<String, Lang> langs = new HashMap<>();
        int upscaled = 0;

        List<Variant> sorted = new ArrayList<>(variants);
        sorted.sort((a, b) -> a.id().compareTo(b.id()));
        for (Variant v : sorted) {
            String ns = v.namespace(), path = v.path();
            int bw = v.width() / 16, bh = v.height() / 16;
            if (bw < 1 || bh < 1 || v.width() % 16 != 0 || v.height() % 16 != 0) {
                problems.add(v.id() + ": size " + v.width() + "x" + v.height() + " px is not whole blocks, skipped");
                continue;
            }
            String texPath = "assets/" + ns + "/textures/painting/" + path + ".png";
            byte[] raw = null;
            String label = null, first = null;
            for (Source s : sources) {
                byte[] b = s.read().apply(texPath);
                if (b != null) {
                    raw = b;
                    label = s.label();
                    if (first == null) first = label;
                }
            }
            if (label != null && !label.equals(first)) {
                notes.add("art for " + v.id() + " from " + label + " (over " + first + ")");
            }
            if (raw == null) {
                problems.add(v.id() + ": no texture " + texPath + " in any source, skipped");
                continue;
            }
            BufferedImage img;
            try {
                img = argb(ImageIO.read(new ByteArrayInputStream(raw)));
            } catch (IOException | RuntimeException e) {
                problems.add(v.id() + ": unreadable texture from " + label + " (" + e + "), skipped");
                continue;
            }
            if (img == null) {
                problems.add(v.id() + ": unreadable texture from " + label + ", skipped");
                continue;
            }
            int res = img.getWidth() / bw;
            if (res >= 1 && img.getWidth() % bw == 0 && img.getHeight() > res * bh && img.getHeight() % (res * bh) == 0) {
                img = img.getSubimage(0, 0, img.getWidth(), res * bh); // animated strip: first frame
            }
            if (res < 1 || img.getWidth() != res * bw || img.getHeight() != res * bh) {
                notes.add(v.id() + ": " + img.getWidth() + "x" + img.getHeight() + " texture is off the " + bw + "x" + bh
                        + " block grid (" + label + "), stretched to " + v.width() + "x" + v.height() + " like vanilla");
                img = nearest(img, v.width(), v.height());
                res = 16;
            }
            if (res < TARGET_RES && TARGET_RES % res == 0) {
                img = nearest(img, bw * TARGET_RES, bh * TARGET_RES);
                res = TARGET_RES;
                upscaled++;
            }

            Lang lang = langs.computeIfAbsent(ns, n -> Lang.read(n, sources));
            String author = lang.author(ns, path);
            if (author == null) {
                author = ownerName(ns, sources);
            }
            String name = lang.names.get("painting." + ns + "." + path + ".title");
            if (name == null) name = titleCase(path);

            JsonObject meta = new JsonObject();
            meta.addProperty("name", name);
            meta.addProperty("author", author);
            meta.addProperty("width", bw);
            meta.addProperty("height", bh);
            meta.addProperty("resolution", res);
            String base = "data/" + OUT_NAMESPACE + "/paintings/" + ns + "/" + path;
            try {
                files.put(base + ".png", png(img));
            } catch (IOException e) {
                problems.add(v.id() + ": could not encode (" + e + "), skipped");
                continue;
            }
            files.put(base + ".json", (GSON.toJson(meta) + "\n").getBytes(StandardCharsets.UTF_8));
            byNs.merge(ns, 1, Integer::sum);
        }
        return new Result(files, byNs, upscaled, notes, problems);
    }

    /** The display name of the first mod source with painting art in this namespace, else the namespace. */
    private static String ownerName(String ns, List<Source> sources) {
        for (Source s : sources) {
            if (s.displayName() != null && s.hasArt().test(ns)) return s.displayName();
        }
        return ns;
    }

    /** One namespace's en_us lang merged over the sources (later wins), plus section credits. */
    private static final class Lang {
        final Map<String, String> names = new HashMap<>();
        final Map<String, String> sections = new HashMap<>();

        static Lang read(String ns, List<Source> sources) {
            Lang lang = new Lang();
            String file = "assets/" + ns + "/lang/en_us.json";
            for (Source s : sources) {
                byte[] b = s.read().apply(file);
                if (b == null) continue;
                JsonObject obj;
                try {
                    JsonElement el = JsonParser.parseString(new String(b, StandardCharsets.UTF_8));
                    if (!el.isJsonObject()) continue;
                    obj = el.getAsJsonObject();
                } catch (RuntimeException e) {
                    continue;
                }
                String[] current = null; // {namespace, artist}
                for (Map.Entry<String, JsonElement> e : obj.entrySet()) { // JsonObject keeps file order
                    if (!e.getValue().isJsonPrimitive()) continue;
                    String key = e.getKey(), val = e.getValue().getAsString();
                    lang.names.put(key, val);
                    Matcher m = SECTION_AUTHOR.matcher(key);
                    if (m.matches()) {
                        current = new String[]{m.group(1), val};
                        continue;
                    }
                    m = SECTION_TITLE.matcher(key);
                    if (m.matches() && current != null && current[0].equals(m.group(1))) {
                        lang.sections.put(m.group(1) + ":" + m.group(2), current[1]);
                    }
                }
            }
            return lang;
        }

        String author(String ns, String path) {
            String a = real(names.get("painting." + ns + "." + path + ".author"));
            if (a == null) a = real(names.get("painting." + ns + "." + path + ".artist"));
            if (a == null) a = real(sections.get(ns + ":" + path));
            return a;
        }
    }

    private static String real(String name) {
        if (name == null) return null;
        String t = name.trim().toLowerCase(Locale.ROOT);
        return t.isEmpty() || t.equals("unknown") || t.equals("-") ? null : name;
    }

    static String titleCase(String path) {
        String last = path.substring(path.lastIndexOf('/') + 1);
        StringBuilder out = new StringBuilder();
        for (String w : last.split("_")) {
            if (w.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.toString();
    }

    private static BufferedImage argb(BufferedImage in) {
        if (in == null || in.getType() == BufferedImage.TYPE_INT_ARGB) return in;
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < in.getHeight(); y++)
            for (int x = 0; x < in.getWidth(); x++) out.setRGB(x, y, in.getRGB(x, y));
        return out;
    }

    /** Nearest-neighbour resize sampling pixel centres, as PIL's Image.NEAREST does. */
    private static BufferedImage nearest(BufferedImage in, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int sw = in.getWidth(), sh = in.getHeight();
        for (int y = 0; y < h; y++) {
            int sy = Math.min(sh - 1, (int) Math.floor((y + 0.5) * sh / h));
            for (int x = 0; x < w; x++) {
                int sx = Math.min(sw - 1, (int) Math.floor((x + 0.5) * sw / w));
                out.setRGB(x, y, in.getRGB(sx, sy));
            }
        }
        return out;
    }

    private static byte[] png(BufferedImage img) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(img, "png", bytes)) throw new IOException("no PNG writer");
        return bytes.toByteArray();
    }
}
