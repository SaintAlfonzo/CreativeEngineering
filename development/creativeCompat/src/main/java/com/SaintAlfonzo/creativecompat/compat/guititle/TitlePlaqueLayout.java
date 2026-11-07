package com.SaintAlfonzo.creativecompat.compat.guititle;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the title plaque a GUI resource pack painted into a container texture and works out how to
 * stretch it to fit a title. Plain Java with no Minecraft classes, so it can be tested headlessly
 * against the real pack textures; {@link TitlePlaques} feeds it pixels and runs the blits.
 *
 * <p>The Engineers GUIs pack pastes the same sprite into every texture: a light parchment fill about
 * 10 rows tall (with a 1px shaded edge), a copper end-cap with a rivet on each side (4px, ending in a
 * dark outline), and a 1px drop shadow above and below the fill. The detector looks for that shape
 * on the row through the middle of the title text; nothing here knows any texture's coordinates, so
 * a pack update or a different plaque pack still works as long as the fill is a light, warm, low
 * saturation band. Vanilla's neutral grey panels never match.
 *
 * <p>Stretching never scales anything. The plaque's plain middle is tiled over the gap, then the end
 * piece (last two fill columns plus the cap) is copied to the new end. Only the side(s) the text
 * overflows grow, so left-anchored titles grow right and centered titles (furnace, dispenser) grow
 * both ways.
 */
public final class TitlePlaqueLayout {

    private TitlePlaqueLayout() {}

    /** ARGB pixel source, in texture pixels. */
    public interface Pixels {
        int argb(int x, int y);
    }

    /** A plaque's fill band [x0, x1) x [top, bottom], plus the width of each end-cap. */
    public record Plaque(int x0, int x1, int top, int bottom, int capW) {
        public Plaque scaledDown(int s) {
            return s == 1 ? this : new Plaque(x0 / s, x1 / s, top / s, bottom / s, Math.max(1, capW / s));
        }
    }

    /** Copy texture rect (srcX, srcY, w, h) to (dstX, dstY). All in GUI pixels relative to the texture origin. */
    public record Blit(int dstX, int dstY, int srcX, int srcY, int w, int h) {}

    /**
     * @param plaque       the plaque under the title, or null when there isn't one
     * @param blits        copies to draw over the background (empty when the title already fits)
     * @param maxTextWidth the widest title that fits inside the GUI, or -1 when no truncation is needed
     * @param centered     whether the title is centered (a truncated title should be re-centered)
     */
    public record Result(Plaque plaque, List<Blit> blits, int maxTextWidth, boolean centered) {
        static final Result NONE = new Result(null, List.of(), -1, false);

        public boolean stretched() {
            return !blits.isEmpty();
        }
    }

    static final int MIN_RUN = 16;
    static final int MIN_HEIGHT = 6;
    static final int MAX_HEIGHT = 24;
    static final int CAP_SCAN = 8;
    static final int DEFAULT_CAP = 4;
    /** Free columns kept between the fill and the text on each side (matches the pack's own left gap). */
    static final int PAD_LEFT = 2;
    static final int PAD_RIGHT = 1;
    /** Columns from the fill edge that travel with the end-cap (inner shading + one plain column). */
    static final int EDGE = 2;
    static final int MAX_TILE = 16;

    static int luma(int argb) {
        int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    /** Light, warm, low-saturation parchment. */
    static boolean isFill(int argb) {
        int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        if (a < 0xC8) return false;
        int l = luma(argb);
        if (l < 0x60 || l > 0xD8) return false;
        if (r < g || g < b) return false;
        int warm = r - b;
        return warm >= 0x0C && warm <= 0x34;
    }

    /** Every plaque crossing texture row {@code row}, in texture pixels. */
    public static List<Plaque> findPlaques(Pixels px, int texW, int texH, int row) {
        List<Plaque> out = new ArrayList<>();
        if (row < 0 || row >= texH) return out;
        int x = 0;
        while (x < texW) {
            if (!isFill(px.argb(x, row))) {
                x++;
                continue;
            }
            int x0 = x;
            while (x < texW && isFill(px.argb(x, row))) x++;
            int x1 = x;
            if (x1 - x0 < MIN_RUN) continue;

            int cx = (x0 + x1) / 2;
            int top = row, bottom = row;
            while (top > 0 && isFill(px.argb(cx, top - 1))) top--;
            while (bottom < texH - 1 && isFill(px.argb(cx, bottom + 1))) bottom++;
            int height = bottom - top + 1;
            if (height < MIN_HEIGHT || height > MAX_HEIGHT) continue;

            int capRow = (top + bottom) / 2;
            int capL = scanCap(px, texW, x0 - 1, -1, capRow);
            int capR = scanCap(px, texW, x1, 1, capRow);
            int capW = Math.max(capL, capR);
            if (capW <= 0) capW = DEFAULT_CAP;
            out.add(new Plaque(x0, x1, top, bottom, capW));
        }
        return out;
    }

    /** Width of the end-cap: distance to the dark outline pixel that closes it, or -1. */
    private static int scanCap(Pixels px, int texW, int start, int dir, int row) {
        for (int i = 0; i < CAP_SCAN; i++) {
            int x = start + dir * i;
            if (x < 0 || x >= texW) return -1;
            if (luma(px.argb(x, row)) < 0x34) return i + 1;
        }
        return -1;
    }

    /**
     * Plans the stretch for a title drawn at {@code textX} that is {@code textW} wide (Font.width, which
     * includes the 1px advance after the last glyph). All values in GUI pixels.
     *
     * @param margin columns kept clear at the GUI's left and right edges
     */
    public static Result layout(List<Plaque> plaques, int textX, int textW, int imageWidth, int margin) {
        if (textW <= 0) return Result.NONE;
        Plaque p = pick(plaques, textX, textW);
        if (p == null) return Result.NONE;

        boolean centered = Math.abs(2 * textX + textW - imageWidth) <= 2;
        int needX0 = textX - PAD_LEFT;
        int needX1 = textX + textW + PAD_RIGHT;
        if (needX0 >= p.x0 && needX1 <= p.x1) return new Result(p, List.of(), -1, centered);

        int lo = margin + p.capW;
        int hi = imageWidth - margin - p.capW;
        int newX0 = Math.min(p.x0, needX0);
        int newX1 = Math.max(p.x1, needX1);
        int maxTextWidth = -1;
        if (newX0 < lo || newX1 > hi) {
            if (centered) {
                newX0 = Math.min(p.x0, lo);
                newX1 = Math.max(p.x1, hi);
                maxTextWidth = (newX1 - newX0) - PAD_LEFT - PAD_RIGHT;
            } else {
                newX0 = Math.max(newX0, Math.min(p.x0, lo));
                newX1 = Math.max(p.x1, hi);
                maxTextWidth = newX1 - PAD_RIGHT - textX;
            }
        }

        List<Blit> blits = new ArrayList<>();
        int tileX = p.x0 + EDGE + 1;
        int tileW = Math.min(MAX_TILE, (p.x1 - EDGE - 1) - tileX);
        if (tileW <= 0) return new Result(p, List.of(), maxTextWidth, centered);
        int shadeTop = Math.max(0, p.top - 1);
        int shadeH = (p.bottom + 1) - shadeTop + 1;
        int bandH = p.bottom - p.top + 1;

        if (newX1 > p.x1) {
            tile(blits, p.x1 - EDGE, newX1 - EDGE, shadeTop, shadeH, tileX, tileW);
            blits.add(new Blit(newX1 - EDGE, p.top, p.x1 - EDGE, p.top, EDGE + p.capW, bandH));
        }
        if (newX0 < p.x0) {
            tile(blits, newX0 + EDGE, p.x0 + EDGE, shadeTop, shadeH, tileX, tileW);
            blits.add(new Blit(newX0 - p.capW, p.top, p.x0 - p.capW, p.top, p.capW + EDGE, bandH));
        }
        return new Result(p, blits, maxTextWidth, centered);
    }

    /** The plaque the title text sits on: the one it overlaps most (at least a few pixels). */
    private static Plaque pick(List<Plaque> plaques, int textX, int textW) {
        Plaque best = null;
        int bestOverlap = 0;
        for (Plaque p : plaques) {
            int overlap = Math.min(textX + textW, p.x1) - Math.max(textX, p.x0);
            if (overlap > bestOverlap) {
                bestOverlap = overlap;
                best = p;
            }
        }
        return bestOverlap >= Math.min(textW, 6) ? best : null;
    }

    private static void tile(List<Blit> out, int from, int to, int y, int h, int srcX, int srcW) {
        for (int x = from; x < to; x += srcW) {
            out.add(new Blit(x, y, srcX, y, Math.min(srcW, to - x), h));
        }
    }
}
