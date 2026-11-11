package com.SaintAlfonzo.creativelabels.client;

import com.SaintAlfonzo.creativelabels.CreativeLabels;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.item.NoteColours;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import com.SaintAlfonzo.creativelabels.shared.BuiltInPacks;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Art read from the donor jars at runtime and derived in memory (sticky_notes.md 3.12, BRIEF 4 "Sprites"). Nothing
 * of theirs is in our jar and nothing derived is written to disk.
 *
 * <p><b>Referenced as-is</b> (stitched onto the block atlas by {@link #onStitch}): Sticky It's note paper
 * {@code sticky_it:entities/sticky} and the four specials {@code sticky_gold}, {@code sticky_creeper},
 * {@code sticky_inverted}, {@code sticky_smile}. The donor jar is a resource pack like any mod, so the resource
 * manager finds them by location.
 *
 * <p><b>Generated</b> into an in-memory resource pack ({@link BuiltInPacks#addMemory}, added on
 * {@link AddPackFindersEvent} at the bottom so real packs override it), built lazily from the donor PNG bytes:
 * <ul>
 * <li>{@code creativelabels:generated/note_grey}: {@code sticky.png} greyscale, brightened so its dominant colour
 * maps to white and a tint shows the true dye colour (the dyed-note base; tint index = face * 5 + spot);</li>
 * <li>{@code note_written}, {@code note_grey_written}, {@code note_<special>_written}: the same images with pen
 * strokes (darkened pixels on the flat face and the first fold) marking a note that carries text;</li>
 * <li>{@code note_item_grey}: {@code item/stickyitem.png} greyscale for the dyed item icon (tinted by ItemColors in
 * phase 2); {@code note_item_<special>}: the icon recoloured with the special paper's dominant colour.</li>
 * </ul>
 * All note textures are 16 x 16 with the note strip in columns 0..5 (Sticky It's model samples u 0..6 px of them).
 */
public final class DonorArt {

    public static final String DONOR_NOTES = "sticky_it";
    public static final String DONOR_LABELS = "labels";
    public static final String PACK_ID = "creativelabels_donor_art";

    private static final String GEN = "generated/";

    public static final ResourceLocation NOTE_PLAIN = new ResourceLocation(DONOR_NOTES, "entities/sticky");
    public static final ResourceLocation NOTE_GREY = new ResourceLocation(CreativeLabels.ID, GEN + "note_grey");
    public static final ResourceLocation NOTE_WRITTEN = new ResourceLocation(CreativeLabels.ID, GEN + "note_written");
    public static final ResourceLocation NOTE_GREY_WRITTEN = new ResourceLocation(CreativeLabels.ID, GEN + "note_grey_written");
    /** Labels' label paper texture (on the block atlas through its {@code labels:block/label} model). */
    public static final ResourceLocation LABEL_PAPER = new ResourceLocation(DONOR_LABELS, "entity/label");

    /** The donor note texture of a special look. */
    public static ResourceLocation special(String special) {
        return new ResourceLocation(DONOR_NOTES, "entities/sticky_" + special);
    }

    /** Our written variant of a special look. */
    public static ResourceLocation specialWritten(String special) {
        return new ResourceLocation(CreativeLabels.ID, GEN + "note_" + special + "_written");
    }

    public static ResourceLocation itemIcon(String special) {
        return new ResourceLocation(CreativeLabels.ID, GEN + "note_item_" + special);
    }

    /** The block-atlas sprite a placed note shows (BRIEF 4). */
    public static ResourceLocation noteSprite(int color, @Nullable String special, boolean written) {
        if (special != null && NoteColours.isSpecial(special)) {
            return written ? specialWritten(special) : special(special);
        }
        if (color != NoteData.NO_COLOR) return written ? NOTE_GREY_WRITTEN : NOTE_GREY;
        return written ? NOTE_WRITTEN : NOTE_PLAIN;
    }

    /** Whether the sprite for this look is tinted with the note's colour. */
    public static boolean tinted(int color, @Nullable String special) {
        return color != NoteData.NO_COLOR && (special == null || !NoteColours.isSpecial(special));
    }

    /** Every note sprite the block model may use: stitched onto the block atlas and listed as model materials. */
    public static List<ResourceLocation> noteSprites() {
        List<ResourceLocation> all = new ArrayList<>(List.of(NOTE_PLAIN, NOTE_GREY, NOTE_WRITTEN, NOTE_GREY_WRITTEN));
        for (NoteColours.Special s : NoteColours.SPECIALS) {
            all.add(special(s.name()));
            all.add(specialWritten(s.name()));
        }
        return all;
    }

    // ----------------------------------------------------------------------------------------------- events

    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) return;
        for (ResourceLocation sprite : noteSprites()) event.addSprite(sprite);
        event.addSprite(LABEL_PAPER);
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        BuiltInPacks.addMemory(event, PackType.CLIENT_RESOURCES, PACK_ID, "Creative Labels: art derived in memory from the donor jars",
                Pack.Position.BOTTOM, DonorArt::files);
    }

    // ----------------------------------------------------------------------------------------------- generation

    @Nullable
    private static volatile Map<String, byte[]> cache;

    /** The generated files (path -> PNG bytes), built once; rebuilt only if the first build failed. */
    public static Map<String, byte[]> files() {
        Map<String, byte[]> files = cache;
        if (files == null) {
            synchronized (DonorArt.class) {
                files = cache;
                if (files == null) {
                    files = build();
                    if (!files.isEmpty()) cache = files;
                }
            }
        }
        return files;
    }

    private static String genPath(String name) {
        return "assets/" + CreativeLabels.ID + "/textures/" + GEN + name + ".png";
    }

    private static Map<String, byte[]> build() {
        Map<String, byte[]> out = new HashMap<>();
        try {
            BufferedImage plain = readDonor(DONOR_NOTES, "textures/entities/sticky.png");
            BufferedImage icon = readDonor(DONOR_NOTES, "textures/item/stickyitem.png");
            if (plain == null || icon == null) {
                CreativeLabels.LOGGER.error("Creative Labels: donor note textures not found in the {} jar; notes will show missing textures", DONOR_NOTES);
                return Map.of();
            }
            BufferedImage grey = greyscale(plain);
            out.put(genPath("note_grey"), png(grey));
            out.put(genPath("note_written"), png(written(plain)));
            out.put(genPath("note_grey_written"), png(written(grey)));
            out.put(genPath("note_item_grey"), png(greyscale(icon)));
            for (NoteColours.Special s : NoteColours.SPECIALS) {
                BufferedImage paper = readDonor(DONOR_NOTES, "textures/entities/sticky_" + s.name() + ".png");
                if (paper == null) {
                    CreativeLabels.LOGGER.warn("Creative Labels: donor texture sticky_{} missing", s.name());
                    continue;
                }
                out.put(genPath("note_" + s.name() + "_written"), png(written(paper)));
                out.put(genPath("note_item_" + s.name()), png(recolour(icon, dominant(paper))));
            }
            CreativeLabels.LOGGER.info("Creative Labels: {} textures derived from the donor jars", out.size());
        } catch (IOException | RuntimeException e) {
            CreativeLabels.LOGGER.error("Creative Labels: could not derive textures from the donor jars", e);
            return Map.of();
        }
        return out;
    }

    /** Reads a PNG from an installed mod jar ({@code assets/<mod>/<path>}); null when the mod or file is missing. */
    @Nullable
    static BufferedImage readDonor(String modId, String path) throws IOException {
        IModFileInfo info = ModList.get().getModFileById(modId);
        if (info == null) return null;
        Path file = info.getFile().findResource("assets", modId, path);
        if (file == null || !Files.exists(file)) return null;
        try (InputStream in = Files.newInputStream(file)) {
            return ImageIO.read(in);
        }
    }

    static byte[] png(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return bytes.toByteArray();
    }

    // ----------------------------------------------------------------------------------------------- pixel maths

    /** The most common opaque colour (RGB) of an image; the paper colour of a note texture. */
    static int dominant(BufferedImage image) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) == 0) continue;
                counts.merge(argb & 0xFFFFFF, 1, Integer::sum);
            }
        }
        int best = 0xFFFFFF, bestCount = -1;
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    /**
     * Greyscale, brightened so the dominant colour's luminance becomes white: multiplied by a dye colour, the paper
     * shows that colour exactly; highlights clamp, the scribbles stay darker.
     */
    static BufferedImage greyscale(BufferedImage src) {
        float ref = Math.max(0.05f, NoteColours.luminance(dominant(src)));
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) continue;
                int g = clamp(Math.round(NoteColours.luminance(argb & 0xFFFFFF) / ref * 255f));
                out.setRGB(x, y, (a << 24) | (g << 16) | (g << 8) | g);
            }
        }
        return out;
    }

    /** The icon recoloured: each pixel keeps its luminance relative to the icon's paper, in the given paper colour. */
    static BufferedImage recolour(BufferedImage icon, int paper) {
        float ref = Math.max(0.05f, NoteColours.luminance(dominant(icon)));
        int pr = paper >> 16 & 255, pg = paper >> 8 & 255, pb = paper & 255;
        BufferedImage out = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < icon.getHeight(); y++) {
            for (int x = 0; x < icon.getWidth(); x++) {
                int argb = icon.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) continue;
                float k = NoteColours.luminance(argb & 0xFFFFFF) / ref;
                out.setRGB(x, y, (a << 24) | (clamp(Math.round(pr * k)) << 16) | (clamp(Math.round(pg * k)) << 8) | clamp(Math.round(pb * k)));
            }
        }
        return out;
    }

    /**
     * Pen strokes for a written note: rows of the 6 px strip darkened to 35 %. Rows 0 and 2 are on the flat face
     * (texture rows 0..2), row 3 on the 45 degree fold (rows 3..4); the donor's own scribbles sit on rows 1, 4, 5.
     */
    private static final int[][] STROKES = {{0, 1, 3}, {2, 1, 4}, {3, 2, 4}};  // row, x from, x to (inclusive)

    static BufferedImage written(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) out.setRGB(x, y, src.getRGB(x, y));
        }
        for (int[] s : STROKES) {
            int y = s[0];
            if (y >= out.getHeight()) continue;
            for (int x = s[1]; x <= s[2] && x < out.getWidth(); x++) {
                int argb = out.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) continue;
                int r = (argb >> 16 & 255) * 35 / 100, g = (argb >> 8 & 255) * 35 / 100, b = (argb & 255) * 35 / 100;
                out.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(255, v);
    }

    private DonorArt() {}
}
