package net.satisfy.vinery.forge.upstream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.item.DrinkBlockItem;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The look a medium wine takes lying in the wine box and the 4x rack: {@code vinery_backport:block/wine_box/<id>}.
 * Built in memory from the upstream art like the rest of {@link UpstreamTranslator}, so no recoloured upstream texture
 * ships in the jar (D13).
 * <ul>
 *     <li>alcohol.md 10.17: Stal and Strad keep their own model ({@link #OWN_SHAPE}); every other medium takes
 *     {@link #BOX_SHAPE}'s model with its texture recoloured in the wine's own palette (its model's particle texture).
 *     The 4x rack draws the same model, only smaller (BottleRenderer).</li>
 *     <li>The recolour is textureswapper/palette_swap.py's: the shape's colours ranked by brightness, each taking the
 *     palette colour at the same relative rank (plain), or at the opposite rank for {@link #INVERT} ({@code --invert}).
 *     {@link #DROPS} leaves some of a wine's colours out of its palette first ({@code --drop}).</li>
 * </ul>
 * {@link #dump} writes every wine texture the last build saw to disk for hand texturing.
 */
public final class BottleLooks {
    private static final String NS = "vinery_backport";
    private static final String KIND = "wine_box";
    /** Mediums that keep their own model everywhere (user, 2026-10-19: Praetorian is fine as is). */
    private static final Set<String> SKIP = Set.of("praetorian_wine");
    /** Mediums whose own model is the box shape already (user, scratchpad 2026-10-19). */
    private static final Set<String> OWN_SHAPE = Set.of("stal_wine", "strad_wine");
    /** The wine whose model every other medium takes, recoloured. */
    private static final String BOX_SHAPE = "strad_wine";
    /** Wines recoloured with the swap tool's --invert mapping (user, scratchpad 2026-10-19: Magnetic; 10-20: Glowing, Aegis). */
    private static final Set<String> INVERT = Set.of("magnetic_wine", "glowing_wine", "aegis_wine");
    /** Colours left out of a wine's palette before the swap (user, scratchpad 2026-10-20). */
    private static final Map<String, Set<Drop>> DROPS = Map.of(
            "magnetic_wine", EnumSet.of(Drop.WHITE),
            "mellohi_wine", EnumSet.of(Drop.BROWN),
            "apple_cider", EnumSet.of(Drop.BROWN),
            "creepers_crush", EnumSet.of(Drop.BROWN));
    /** Every wine's textures from the last build, upstream and recoloured, by asset path ({@link #dump}). */
    private static volatile Map<String, byte[]> lastTextures = Map.of();
    /** Wine -> where each of its textures is drawn, for the dump's index. */
    private static volatile Map<String, String> lastIndex = Map.of();

    private BottleLooks() {
    }

    static void add(Map<String, byte[]> out) {
        String shapeModel = wineModel(out, BOX_SHAPE);
        String shapeTexture = particle(out, shapeModel);
        JsonObject shapeJson = model(out, shapeModel);
        BufferedImage shape = shapeTexture == null ? null : texture(out, shapeTexture);
        if (shape == null || shapeJson == null || !shapeJson.has("textures")) {
            Vinery.LOGGER.warn("[vinery-upstream] no {} art, wine box looks skipped", BOX_SHAPE);
            return;
        }
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : shapeJson.getAsJsonObject("textures").entrySet()) {
            if (e.getValue().getAsString().equals(shapeTexture)) keys.add(e.getKey());
        }
        Map<String, byte[]> textures = new TreeMap<>();
        Map<String, String> index = new TreeMap<>();
        int looks = 0;
        for (Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof DrinkBlockItem drink)) {
                continue;
            }
            String wine = String.valueOf(ForgeRegistries.BLOCKS.getKey(drink.getBlock()).getPath());
            String own = wineModel(out, wine);
            Set<String> ownTextures = textureIds(out, own);
            ownTextures.forEach(id -> copy(out, id, textures));
            String look = "own model";
            if (drink.getBottleSize() == DrinkBlockItem.BottleSize.MEDIUM && !SKIP.contains(wine)) {
                if (OWN_SHAPE.contains(wine)) {
                    if (own != null && addAlias(out, wine, own)) looks++;
                } else if (addVariant(out, wine, shape, shapeModel, INVERT.contains(wine), DROPS.getOrDefault(wine, Set.of()), keys)) {
                    looks++;
                    look = BOX_SHAPE + " shape, " + NS + ":block/" + KIND + "/" + wine + (INVERT.contains(wine) ? " (inverted)" : "")
                            + (DROPS.containsKey(wine) ? " (no " + DROPS.get(wine).stream().map(d -> d.name().toLowerCase()).collect(Collectors.joining(", ")) + ")" : "");
                    copy(out, NS + ":block/" + KIND + "/" + wine, textures);
                }
            }
            index.put(wine, drink.getBottleSize().name().toLowerCase() + "\tplaced: " + String.join(", ", ownTextures)
                    + "\tbox / 4x rack: " + look);
        }
        lastTextures = Map.copyOf(textures);
        lastIndex = Map.copyOf(index);
        Vinery.LOGGER.info("[vinery-upstream] {} wine box looks", looks);
    }

    /**
     * Writes every wine texture the last asset build saw to {@code dir}: {@code upstream/} as the donor jars have it,
     * {@code generated/} the recolours, plus {@code index.tsv} (wine, size, its textures, its box / 4x rack look).
     * Returns how many PNGs were written; 0 before any build (a dedicated server never builds assets).
     */
    public static int dump(Path dir) throws IOException {
        Map<String, byte[]> textures = lastTextures;
        for (Map.Entry<String, byte[]> e : textures.entrySet()) {
            String path = e.getKey();
            String folder = path.startsWith("assets/" + NS + "/") ? "generated" : "upstream";
            Path file = dir.resolve(folder).resolve(path.substring(path.indexOf("/textures/") + "/textures/".length()));
            Files.createDirectories(file.getParent());
            Files.write(file, e.getValue());
        }
        StringBuilder tsv = new StringBuilder("wine\tsize\tplaced textures\tbox and 4x rack\n");
        lastIndex.forEach((wine, line) -> tsv.append(wine).append('\t').append(line).append('\n'));
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("index.tsv"), tsv.toString(), StandardCharsets.UTF_8);
        return textures.size();
    }

    /** A box model that is just the wine's own model, so the renderers know it belongs to the box set. */
    private static boolean addAlias(Map<String, byte[]> out, String wine, String own) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", own);
        out.put(path(NS + ":block/" + KIND + "/" + wine, "models", "json"), UpstreamTranslator.bytes(model));
        return true;
    }

    /** One recoloured texture plus a model that points {@code keys} of {@code parent} at it. */
    private static boolean addVariant(Map<String, byte[]> out, String wine, BufferedImage shape, String parent, boolean invert, Set<Drop> drops, List<String> keys) {
        String palette = particle(out, wineModel(out, wine));
        BufferedImage colours = palette == null ? null : texture(out, palette);
        if (colours == null) {
            Vinery.LOGGER.warn("[vinery-upstream] no art for the {} {}", KIND, wine);
            return false;
        }
        String id = NS + ":block/" + KIND + "/" + wine;
        try {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(swap(shape, colours, invert, drops), "png", png);
            out.put(path(id, "textures", "png"), png.toByteArray());
        } catch (IOException e) {
            Vinery.LOGGER.warn("[vinery-upstream] couldn't write the {} {}", KIND, wine, e);
            return false;
        }
        JsonObject textures = new JsonObject();
        for (String key : keys) textures.addProperty(key, id);
        textures.addProperty("particle", id);
        JsonObject model = new JsonObject();
        model.addProperty("parent", parent);
        model.add("textures", textures);
        out.put(path(id, "models", "json"), UpstreamTranslator.bytes(model));
        return true;
    }

    /**
     * palette_swap.py's mapping: shape colours by brightness rank -> palette colours at the same relative rank, or the
     * opposite rank when {@code invert} (the tool's --invert: the shape's darkest colour takes the palette's brightest).
     * Palette colours matching any of {@code drops} are left out (the tool's --drop), unless that would leave none.
     */
    static BufferedImage swap(BufferedImage shape, BufferedImage palette, boolean invert, Set<Drop> drops) {
        List<Integer> paletteColours = opaqueColours(palette);
        List<Integer> kept = paletteColours.stream().filter(c -> drops.stream().noneMatch(d -> d.test(c))).toList();
        if (!kept.isEmpty()) paletteColours = kept;
        List<Integer> shapeColours = opaqueColours(shape);
        BufferedImage result = new BufferedImage(shape.getWidth(), shape.getHeight(), BufferedImage.TYPE_INT_ARGB);
        if (paletteColours.isEmpty() || shapeColours.isEmpty()) {
            return shape;
        }
        int min = brightness(shapeColours.get(0));
        int max = brightness(shapeColours.get(shapeColours.size() - 1));
        int last = paletteColours.size() - 1;
        Map<Integer, Integer> map = new HashMap<>();
        for (int colour : shapeColours) {
            double t = max == min ? 0.5 : (brightness(colour) - min) / (double) (max - min);
            int index = Math.max(0, Math.min(last, (int) Math.rint(t * last))); // Python's round: halves to even
            map.put(colour, paletteColours.get(invert ? last - index : index));
        }
        for (int y = 0; y < shape.getHeight(); y++) {
            for (int x = 0; x < shape.getWidth(); x++) {
                int argb = shape.getRGB(x, y);
                result.setRGB(x, y, map.getOrDefault(argb, argb));
            }
        }
        return result;
    }

    /** Distinct non-transparent ARGB colours, darkest first (ties by value, so the result doesn't vary). */
    private static List<Integer> opaqueColours(BufferedImage image) {
        Set<Integer> colours = new LinkedHashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) > 0) colours.add(argb);
            }
        }
        List<Integer> sorted = new ArrayList<>(colours);
        sorted.sort(Comparator.comparingInt(BottleLooks::brightness).thenComparingInt(c -> c));
        return sorted;
    }

    /** Palette colour classes the swap can leave out; the HSV bounds match palette_swap.py's DROP_CLASSES. */
    enum Drop {
        /** Warm mid-dark hues: wood, dirt, rust. */
        BROWN,
        /** Near-white: almost no saturation, very bright. */
        WHITE;

        boolean test(int argb) {
            float[] hsv = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
            return switch (this) {
                case BROWN -> hsv[0] * 360 >= 10 && hsv[0] * 360 <= 45 && hsv[1] >= 0.4f && hsv[2] <= 0.7f;
                case WHITE -> hsv[1] <= 0.15f && hsv[2] >= 0.85f;
            };
        }
    }

    private static int brightness(int argb) {
        return ((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF);
    }

    /** The model a wine block draws (its fake_model=false variant). */
    private static String wineModel(Map<String, byte[]> out, String wine) {
        byte[] state = out.get("assets/vinery/blockstates/" + wine + ".json");
        JsonObject json = state == null ? null : UpstreamTranslator.parseObject(state);
        if (json == null || !json.has("variants")) return null;
        for (Map.Entry<String, JsonElement> variant : json.getAsJsonObject("variants").entrySet()) {
            if (!variant.getKey().contains("fake_model=true") && variant.getValue().isJsonObject()) {
                return variant.getValue().getAsJsonObject().get("model").getAsString();
            }
        }
        return null;
    }

    /** Every texture a model and its parents name (a parent's first, so the child's wins on a shared key). */
    private static Set<String> textureIds(Map<String, byte[]> out, String modelId) {
        Map<String, String> byKey = new LinkedHashMap<>();
        List<JsonObject> chain = new ArrayList<>();
        for (JsonObject model = model(out, modelId); model != null && chain.size() < 8;
             model = model.has("parent") ? model(out, model.get("parent").getAsString()) : null) {
            chain.add(0, model);
        }
        for (JsonObject model : chain) {
            if (!model.has("textures")) continue;
            model.getAsJsonObject("textures").entrySet().forEach(e -> byKey.put(e.getKey(), e.getValue().getAsString()));
        }
        Set<String> ids = new LinkedHashSet<>();
        for (String id : byKey.values()) {
            if (!id.startsWith("#") && out.containsKey(path(id, "textures", "png"))) ids.add(id);
        }
        return ids;
    }

    private static void copy(Map<String, byte[]> out, String textureId, Map<String, byte[]> into) {
        String path = path(textureId, "textures", "png");
        byte[] png = out.get(path);
        if (png != null) into.put(path, png);
    }

    private static JsonObject model(Map<String, byte[]> out, String id) {
        byte[] json = id == null ? null : out.get(path(id, "models", "json"));
        return json == null ? null : UpstreamTranslator.parseObject(json);
    }

    /** A wine's colours: its model's particle texture (the main bottle texture for every wine). */
    private static String particle(Map<String, byte[]> out, String modelId) {
        JsonObject model = model(out, modelId);
        if (model == null || !model.has("textures") || !model.getAsJsonObject("textures").has("particle")) return null;
        return model.getAsJsonObject("textures").get("particle").getAsString();
    }

    private static BufferedImage texture(Map<String, byte[]> out, String id) {
        byte[] png = out.get(path(id, "textures", "png"));
        if (png == null) return null;
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            return null;
        }
    }

    private static String path(String id, String folder, String ext) {
        int colon = id.indexOf(':');
        String ns = colon < 0 ? "minecraft" : id.substring(0, colon);
        return "assets/" + ns + "/" + folder + "/" + id.substring(colon + 1) + "." + ext;
    }
}
