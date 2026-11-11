package com.SaintAlfonzo.creativecompat.banners;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Builds the Bedspreads and Customizable Elytra textures for banner patterns those two mods bundle no art for, from
 * each pattern's own {@code assets/<ns>/textures/entity/banner/<id>.png} in the installed jars (feature_writeup/
 * creative_compat.md 6.3). No derived texture ships in the jar. Pure Java (no game classes), so it can be checked
 * offline.
 *
 * <p>Placement follows each mod's bundled set, measured once by hand (the old offline generators
 * {@code gen_bedspreads_pattern_compat.py} and {@code gen_elytra_pattern_compat.py}): the pattern's first glyph (the
 * leftmost connected run of columns) is cropped and placed on the mod's canvas. Bedspreads: wide glyphs (24 px or more)
 * stretched to fill (2,14)-(38,44), others fit into 14x20 and stood on x 14, y 42, on a 64x64 canvas. Elytra: fit into
 * 20x40 and centred at (81,23) on 128x64, and the half-size set (10x20 at (40,11) on 64x32). Resampling is Lanczos over
 * premultiplied alpha with the same fixed-point rounding as the generators' image library, so the output equals the
 * files the jar used to carry.
 */
public final class BannerPatternArt {

    public static final String OUT_NS = "creativecompat";

    /** One jar's banner pattern textures: {@code "<ns>/<id>"} to the PNG bytes. */
    public record Source(String label, Map<String, Supplier<byte[]>> patterns) {}

    public record Result(Map<String, byte[]> files, int bedspreads, int elytra, List<String> problems) {}

    private BannerPatternArt() {}

    /**
     * @param sources       first source wins for the same pattern
     * @param bedCovered    (ns, id) Bedspreads already bundles; null = Bedspreads not installed
     * @param elytraCovered id Customizable Elytra already bundles (its set is flat); null = not installed
     */
    public static Result build(List<Source> sources, BiPredicate<String, String> bedCovered, Predicate<String> elytraCovered) {
        Map<String, byte[]> files = new TreeMap<>();
        List<String> problems = new ArrayList<>();
        Map<String, Boolean> seen = new HashMap<>();
        int bed = 0, elytra = 0;
        for (Source s : sources) {
            for (Map.Entry<String, Supplier<byte[]>> e : s.patterns().entrySet()) {
                String key = e.getKey();
                if (seen.putIfAbsent(key, Boolean.TRUE) != null) continue;
                int slash = key.indexOf('/');
                String ns = key.substring(0, slash), id = key.substring(slash + 1);
                boolean wantBed = bedCovered != null && !bedCovered.test(ns, id);
                boolean wantElytra = elytraCovered != null && !elytraCovered.test(id);
                if (!wantBed && !wantElytra) continue;
                try {
                    byte[] raw = e.getValue().get();
                    int[] src = raw == null ? null : rgba64(raw);
                    if (src == null) {
                        problems.add(key + " (" + s.label() + "): unreadable");
                        continue;
                    }
                    String tex = "assets/" + OUT_NS + "/textures/entity/";
                    if (wantBed) {
                        files.put(tex + ns + "/" + id + ".png", png(bedspreads(src), 64, 64));
                        bed++;
                    }
                    if (wantElytra) {
                        files.put(tex + "elytra_banner/" + ns + "/" + id + ".png",
                                png(centred(src, 128, 64, 20, 40, 81, 23), 128, 64));
                        files.put(tex + "elytra_banner_low/" + ns + "/" + id + ".png",
                                png(centred(src, 64, 32, 10, 20, 40, 11), 64, 32));
                        elytra++;
                    }
                } catch (IOException | RuntimeException ex) {
                    problems.add(key + " (" + s.label() + "): " + ex);
                }
            }
        }
        return new Result(files, bed, elytra, problems);
    }

    // ---- placement ----

    /** Image: width, height, RGBA bytes as ints, row-major. */
    private record Img(int w, int h, int[] px) {
        Img crop(int x0, int y0, int x1, int y1) {
            int cw = x1 - x0, ch = y1 - y0;
            int[] out = new int[cw * ch * 4];
            for (int y = 0; y < ch; y++)
                System.arraycopy(px, ((y0 + y) * w + x0) * 4, out, y * cw * 4, cw * 4);
            return new Img(cw, ch, out);
        }
    }

    private static int[] bedspreads(int[] src64) {
        Img im = new Img(64, 64, src64);
        int[] canvas = new int[64 * 64 * 4];
        int[] box = firstGlyph(im);
        if (box == null) return src64;
        Img glyph = im.crop(box[0], box[1], box[2] + 1, box[3] + 1);
        Img resized;
        int px, py;
        if (glyph.w >= 24) {
            resized = resize(glyph, 36, 30);
            px = 2;
            py = 14;
        } else {
            double scale = Math.min(Math.min(14.0 / glyph.w, 20.0 / glyph.h), 1.0);
            int nw = Math.max(1, (int) Math.rint(glyph.w * scale)), nh = Math.max(1, (int) Math.rint(glyph.h * scale));
            resized = resize(glyph, nw, nh);
            px = 14 - nw / 2;
            py = 42 - nh;
        }
        composite(canvas, 64, 64, resized, px, py);
        return canvas;
    }

    private static int[] centred(int[] src64, int cw, int ch, int maxW, int maxH, int cx, int cy) {
        Img im = new Img(64, 64, src64);
        int[] canvas = new int[cw * ch * 4];
        int[] box = firstGlyph(im);
        if (box == null) return canvas;
        Img glyph = im.crop(box[0], box[1], box[2] + 1, box[3] + 1);
        double scale = Math.min(Math.min((double) maxW / glyph.w, (double) maxH / glyph.h), 1.0);
        int nw = Math.max(1, (int) Math.rint(glyph.w * scale)), nh = Math.max(1, (int) Math.rint(glyph.h * scale));
        Img resized = scale != 1.0 ? resize(glyph, nw, nh) : glyph;
        composite(canvas, cw, ch, resized, cx - nw / 2, cy - nh / 2);
        return canvas;
    }

    /** {x0, y0, x1, y1} inclusive of the leftmost run of non-empty columns and its rows, or null when empty. */
    private static int[] firstGlyph(Img im) {
        boolean[] col = new boolean[im.w];
        for (int y = 0; y < im.h; y++)
            for (int x = 0; x < im.w; x++) if (im.px[(y * im.w + x) * 4 + 3] > 0) col[x] = true;
        int start = -1, end = -1;
        for (int x = 0; x < im.w; x++) {
            if (!col[x]) continue;
            if (start < 0) {
                start = end = x;
            } else if (x == end + 1) {
                end = x;
            } else {
                break;
            }
        }
        if (start < 0) return null;
        int top = -1, bottom = -1;
        for (int y = 0; y < im.h; y++) {
            for (int x = start; x <= end; x++) {
                if (im.px[(y * im.w + x) * 4 + 3] > 0) {
                    if (top < 0) top = y;
                    bottom = y;
                    break;
                }
            }
        }
        return new int[]{start, top, end, bottom};
    }

    /** Alpha-composite onto a transparent canvas, with the image library's fixed-point blend. */
    private static void composite(int[] canvas, int cw, int ch, Img src, int ox, int oy) {
        for (int y = 0; y < src.h; y++) {
            int ty = oy + y;
            if (ty < 0 || ty >= ch) continue;
            for (int x = 0; x < src.w; x++) {
                int tx = ox + x;
                if (tx < 0 || tx >= cw) continue;
                int s = (y * src.w + x) * 4, d = (ty * cw + tx) * 4;
                int sa = src.px[s + 3];
                if (sa == 0) continue;
                long da = canvas[d + 3];
                long blend = da * (255 - sa);
                long outa255 = sa * 255L + blend;
                long coef1 = sa * 255L * 255L * 128L / outa255;
                long coef2 = 255L * 128L - coef1;
                for (int c = 0; c < 3; c++) {
                    long t = src.px[s + c] * coef1 + canvas[d + c] * coef2 + (0x80L << 7);
                    canvas[d + c] = (int) ((((t >> 8) + t) >> 8) >> 7);
                }
                long a = outa255 + 0x80;
                canvas[d + 3] = (int) (((a >> 8) + a) >> 8);
            }
        }
    }

    // ---- Lanczos resize over premultiplied alpha (8-bit fixed point) ----

    private static final int PRECISION_BITS = 32 - 8 - 2;

    private static Img resize(Img in, int w, int h) {
        if (in.w == w && in.h == h) return new Img(w, h, in.px.clone());
        int[] p = new int[in.px.length];
        for (int i = 0; i < p.length; i += 4) {
            int a = in.px[i + 3];
            for (int c = 0; c < 3; c++) {
                int t = in.px[i + c] * a + 128;
                p[i + c] = ((t >> 8) + t) >> 8;
            }
            p[i + 3] = a;
        }
        Img cur = new Img(in.w, in.h, p);
        if (w != in.w) cur = pass(cur, w, true);
        if (h != in.h) cur = pass(cur, h, false);
        int[] out = cur.px;
        for (int i = 0; i < out.length; i += 4) {
            int a = out[i + 3];
            if (a == 255 || a == 0) continue;
            for (int c = 0; c < 3; c++) out[i + c] = Math.min(255, Math.max(0, 255 * out[i + c] / a));
        }
        return cur;
    }

    private static Img pass(Img in, int outSize, boolean horizontal) {
        int inSize = horizontal ? in.w : in.h;
        double scale = (double) inSize / outSize, filterscale = Math.max(scale, 1.0);
        double support = 3.0 * filterscale;
        int ksize = (int) Math.ceil(support) * 2 + 1;
        int[] kk = new int[outSize * ksize];
        int[] bounds = new int[outSize * 2];
        for (int xx = 0; xx < outSize; xx++) {
            double center = (xx + 0.5) * scale, ww = 0.0, ss = 1.0 / filterscale;
            int xmin = (int) (center - support + 0.5);
            if (xmin < 0) xmin = 0;
            int xmax = (int) (center + support + 0.5);
            if (xmax > inSize) xmax = inSize;
            xmax -= xmin;
            double[] k = new double[xmax];
            for (int x = 0; x < xmax; x++) {
                k[x] = lanczos((x + xmin - center + 0.5) * ss);
                ww += k[x];
            }
            for (int x = 0; x < xmax; x++) {
                double v = ww != 0.0 ? k[x] / ww : k[x];
                kk[xx * ksize + x] = v < 0 ? (int) (-0.5 + v * (1 << PRECISION_BITS)) : (int) (0.5 + v * (1 << PRECISION_BITS));
            }
            bounds[xx * 2] = xmin;
            bounds[xx * 2 + 1] = xmax;
        }
        int ow = horizontal ? outSize : in.w, oh = horizontal ? in.h : outSize;
        int[] out = new int[ow * oh * 4];
        for (int y = 0; y < oh; y++) {
            for (int x = 0; x < ow; x++) {
                int o = horizontal ? x : y;
                int min = bounds[o * 2], n = bounds[o * 2 + 1];
                for (int c = 0; c < 4; c++) {
                    int ss = 1 << (PRECISION_BITS - 1);
                    for (int i = 0; i < n; i++) {
                        int sx = horizontal ? min + i : x, sy = horizontal ? y : min + i;
                        ss += in.px[(sy * in.w + sx) * 4 + c] * kk[o * ksize + i];
                    }
                    out[(y * ow + x) * 4 + c] = Math.min(255, Math.max(0, ss >> PRECISION_BITS));
                }
            }
        }
        return new Img(ow, oh, out);
    }

    private static double sinc(double x) {
        if (x == 0.0) return 1.0;
        x *= Math.PI;
        return Math.sin(x) / x;
    }

    private static double lanczos(double x) {
        return -3.0 <= x && x < 3.0 ? sinc(x) * sinc(x / 3) : 0.0;
    }

    // ---- PNG in and out ----

    /** The PNG as 64x64 RGBA (nearest-neighbour when another size), or null when unreadable. */
    private static int[] rgba64(byte[] raw) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
        if (img == null) return null;
        int w = img.getWidth(), h = img.getHeight();
        int[] px = new int[w * h * 4];
        Raster r = img.getRaster();
        int bands = r.getNumBands();
        // Grey PNGs: read the samples, as getRGB would convert through the linear grey colour space.
        boolean grey = img.getColorModel() instanceof ComponentColorModel && bands <= 2;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = (y * w + x) * 4;
                if (grey) {
                    int v = r.getSample(x, y, 0);
                    px[i] = px[i + 1] = px[i + 2] = v;
                    px[i + 3] = bands == 2 ? r.getSample(x, y, 1) : 255;
                } else {
                    int argb = img.getRGB(x, y);
                    px[i] = argb >> 16 & 255;
                    px[i + 1] = argb >> 8 & 255;
                    px[i + 2] = argb & 255;
                    px[i + 3] = argb >>> 24;
                }
            }
        }
        if (w == 64 && h == 64) return px;
        int[] out = new int[64 * 64 * 4];
        for (int y = 0; y < 64; y++) {
            int sy = Math.min(h - 1, (int) Math.floor((y + 0.5) * h / 64));
            for (int x = 0; x < 64; x++) {
                int sx = Math.min(w - 1, (int) Math.floor((x + 0.5) * w / 64));
                System.arraycopy(px, (sy * w + sx) * 4, out, (y * 64 + x) * 4, 4);
            }
        }
        return out;
    }

    private static byte[] png(int[] px, int w, int h) throws IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = (y * w + x) * 4;
                img.setRGB(x, y, px[i + 3] << 24 | px[i] << 16 | px[i + 1] << 8 | px[i + 2]);
            }
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(img, "png", bytes)) throw new IOException("no PNG writer");
        return bytes.toByteArray();
    }
}
