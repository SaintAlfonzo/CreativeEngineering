package com.SaintAlfonzo.creativedelight.compat.art;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pixel operations for {@link CompartmentArt}: the textures CD's compartments need are built on the client from other mods'
 * art instead of shipping modified copies (plan_cb_publish.md 10, TODO licence/cd #1, mobs/nests #2). Plain java.awt, no
 * Minecraft classes, so the logic can be checked against the hand-made art outside the game.
 * <ul>
 *     <li>{@link #paletteSwap}: textureswapper/palette_swap.py's default mode. The source's opaque colours are placed by
 *     brightness (R+G+B) between its darkest and brightest; each takes the palette colour at the same relative position
 *     (palette ordered dark to bright, ties by {@link #ORDER}; rounding half to even like Python's round).</li>
 *     <li>{@link #recolour}: a fixed colour-for-colour map, every other pixel unchanged.</li>
 * </ul>
 */
public final class Recolour {

	private Recolour() {}

	private static int brightness(int argb) {
		return ((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF);
	}

	/**
	 * Palette order: brightness, then alpha, blue, green, red. The tool breaks brightness ties in Python set order; this rule
	 * matches the owner's 2026-10-31 rack art for 20 of its 21 woods (the 21st differs only between equally bright colours).
	 */
	static final Comparator<Integer> ORDER = Comparator.<Integer>comparingInt(Recolour::brightness)
			.thenComparingInt(c -> (c >>> 24) & 0xFF)
			.thenComparingInt(c -> c & 0xFF)
			.thenComparingInt(c -> (c >> 8) & 0xFF)
			.thenComparingInt(c -> (c >> 16) & 0xFF);

	public static BufferedImage read(byte[] png) throws IOException {
		BufferedImage in = ImageIO.read(new ByteArrayInputStream(png));
		if (in == null) throw new IOException("not an image");
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < in.getHeight(); y++)
			for (int x = 0; x < in.getWidth(); x++) out.setRGB(x, y, in.getRGB(x, y));
		return out;
	}

	public static byte[] png(BufferedImage image) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		return out.toByteArray();
	}

	private static boolean opaque(int argb) {
		return (argb >>> 24) != 0;
	}

	/** The image's distinct opaque colours, dark to bright. */
	public static List<Integer> palette(BufferedImage image) {
		Set<Integer> colours = new LinkedHashSet<>();
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {
				int c = image.getRGB(x, y);
				if (opaque(c)) colours.add(c);
			}
		List<Integer> list = new ArrayList<>(colours);
		list.sort(ORDER);
		return list;
	}

	/**
	 * {@code shape} recoloured with {@code palette} (dark to bright). Pixels where {@code keep} is true are left out of the
	 * swap and copied unchanged ({@code keep} may be null).
	 */
	public static BufferedImage paletteSwap(BufferedImage shape, List<Integer> palette, boolean[][] keep) {
		int w = shape.getWidth(), h = shape.getHeight();
		int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				int c = shape.getRGB(x, y);
				if (!opaque(c) || kept(keep, x, y)) continue;
				int b = brightness(c);
				lo = Math.min(lo, b);
				hi = Math.max(hi, b);
			}
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int n = palette.size();
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				int c = shape.getRGB(x, y);
				if (!opaque(c) || kept(keep, x, y) || n == 0) {
					out.setRGB(x, y, c);
					continue;
				}
				double t = hi != lo ? (brightness(c) - lo) / (double) (hi - lo) : 0.5;
				int i = (int) Math.rint(t * (n - 1));
				out.setRGB(x, y, palette.get(Math.max(0, Math.min(n - 1, i))));
			}
		return out;
	}

	private static boolean kept(boolean[][] keep, int x, int y) {
		return keep != null && y < keep.length && x < keep[y].length && keep[y][x];
	}

	/** {@code image} with every colour in {@code map} (ARGB to ARGB) replaced. */
	public static BufferedImage recolour(BufferedImage image, Map<Integer, Integer> map) {
		BufferedImage out = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++) {
				int c = image.getRGB(x, y);
				out.setRGB(x, y, map.getOrDefault(c, c));
			}
		return out;
	}
}
