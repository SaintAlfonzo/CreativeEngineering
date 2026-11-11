package com.SaintAlfonzo.creativedevtools.cdevplace;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Structure Catalogue coordinates (feature_writeup/plan_structure_catalogue.md 2): one cursor per side of spawn. Columns
 * are fed in order with their measured item sizes, so nothing has to be measured ahead of the column being built.
 * No game classes, so it runs in the plain Java test (tools/test_worldgen_tools.sh).
 */
final class CatalogueLayout {

	static final int SIGN_WIDTH = 16;   // about what a sign line (90 px) holds; the renderer clips the rest

	/** Item footprint along X and Z. */
	record Size(int x, int z) {}

	/** Where a column landed: its X extent and each item's first Z. */
	record Placement(int minX, int maxX, int[] minZ, int endZ) {}

	private final int[] levelGaps;
	private final int variationGap, originZ, startX;
	private final boolean negative;
	private List<String> previous;
	private int edge;

	/** {@code negative}: columns run -X and the first one ends at {@code -originX}. */
	CatalogueLayout(int[] levelGaps, int variationGap, int originX, int originZ, boolean negative) {
		this.levelGaps = levelGaps;
		this.variationGap = variationGap;
		this.originZ = originZ;
		this.negative = negative;
		this.startX = negative ? -originX : originX;
	}

	/** The gap of the highest level that differs. Identical paths use the biome level's. */
	static int gapBetween(int[] levelGaps, List<String> a, List<String> b) {
		for (int level = 0; level < levelGaps.length; level++)
			if (!Objects.equals(a.get(level), b.get(level))) return levelGaps[level];
		return levelGaps[levelGaps.length - 1];
	}

	/** Places the next column; items are left-aligned on its low X side and stack along +Z. */
	Placement place(List<String> path, List<Size> items) {
		int width = 1;
		for (Size s : items) width = Math.max(width, s.x());
		int gap = previous == null ? 0 : gapBetween(levelGaps, previous, path);
		int minX, maxX;
		if (negative) {
			maxX = previous == null ? startX : edge - 1 - gap;
			minX = maxX - width + 1;
			edge = minX;
		} else {
			minX = previous == null ? startX : edge + 1 + gap;
			maxX = minX + width - 1;
			edge = maxX;
		}
		previous = path;
		int[] minZ = new int[items.size()];
		int z = originZ;
		for (int i = 0; i < items.size(); i++) {
			minZ[i] = z;
			z += items.get(i).z() + variationGap;
		}
		return new Placement(minX, maxX, minZ, z - variationGap - 1);
	}

	/** The X the next column would start near; used only as a chunk to measure whole structures at. */
	int nextX() {
		return previous == null ? startX : edge;
	}

	/** Levels that need a new sign going from {@code prev} (null at the start) to {@code cur}: a change resets all deeper levels. */
	static List<Integer> signLevels(List<String> prev, List<String> cur) {
		List<Integer> out = new ArrayList<>();
		boolean changed = prev == null;
		for (int level = 0; level < cur.size(); level++) {
			if (!changed && !Objects.equals(prev.get(level), cur.get(level))) changed = true;
			if (changed && cur.get(level) != null) out.add(level);
		}
		return out;
	}

	/** Z of a level's sign: the category sign stands furthest from the strip. */
	static int signZ(int level, int levels) {
		return -2 * (levels - level);
	}

	/** A level sign: what the level is, the value wrapped on words, and the item count. */
	static List<String> levelSign(String label, String value, int count) {
		List<String> out = new ArrayList<>();
		out.add(fit(label));
		StringBuilder line = new StringBuilder();
		List<String> wrapped = new ArrayList<>();
		for (String word : value.split(" ")) {
			if (line.length() > 0 && line.length() + 1 + word.length() > SIGN_WIDTH) {
				wrapped.add(line.toString());
				line.setLength(0);
			}
			if (line.length() > 0) line.append(' ');
			line.append(word);
		}
		if (line.length() > 0) wrapped.add(line.toString());
		for (int i = 0; i < wrapped.size() && i < 2; i++) out.add(fit(wrapped.get(i)));
		out.add(count + (count == 1 ? " item" : " items"));
		return out;
	}

	private static String fit(String s) {
		return s.length() <= SIGN_WIDTH ? s : s.substring(0, SIGN_WIDTH - 2) + "..";
	}
}
