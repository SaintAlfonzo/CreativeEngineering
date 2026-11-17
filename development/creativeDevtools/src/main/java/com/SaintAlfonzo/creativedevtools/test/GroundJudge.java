package com.SaintAlfonzo.creativedevtools.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The pure parts of the {@code under}, {@code floating} and {@code season} verbs (TestJudges): how one built column is
 * judged, where the native-ground ring is read, the top-N summary and the comma-list split. No Minecraft classes, so the
 * headless harness (tools/scratch_query/cdevtest_harness) tests them. Semantics are those of the retired /zq verbs
 * (tools/scratch_query/zq_probe.js) so old SQ-EFB judges still read the same numbers.
 */
public final class GroundJudge {

	/** What a position holds for the judge: water (any fluid with "water" in its id, waterlogged included), air (air
	 * or a non-solid material), or solid. */
	public enum Kind { AIR, WATER, SOLID }

	@FunctionalInterface
	public interface Column {
		Kind at(int y);
	}

	/** How far down a gap is followed, and the gap the beard is expected to fill (beardReach). */
	public static final int REACH = 64;
	public static final int BEARD = 6;

	/**
	 * One column of a structure: {@code bottom} the lowest solid block between the boxes' minY and maxY, {@code first}
	 * the kind right under it, {@code gap} the non-solid run under it (up to REACH), {@code water} how much of that run
	 * is water, {@code deep} gap over BEARD. {@code first == SOLID} means gap 0 and not deep.
	 */
	public record Under(int bottom, Kind first, int gap, int water, boolean deep) {
	}

	private GroundJudge() {
	}

	/** The column judged; null when nothing between minY and maxY is solid (the column is not counted). */
	public static Under under(int minY, int maxY, Column col) {
		Integer y0 = null;
		for (int y = minY; y <= maxY; y++) {
			if (col.at(y) == Kind.SOLID) {
				y0 = y;
				break;
			}
		}
		if (y0 == null) return null;
		Kind first = col.at(y0 - 1);
		if (first == Kind.SOLID) return new Under(y0, first, 0, 0, false);
		int gap = 0, wat = 0;
		for (int y = y0 - 1; y > y0 - 1 - REACH; y--) {
			Kind k = col.at(y);
			if (k == Kind.SOLID) break;
			gap++;
			if (k == Kind.WATER) wat++;
		}
		return new Under(y0, first, gap, wat, gap > BEARD);
	}

	/** Ring points every 2 blocks, 3 out from the box (x z pairs), the same lattice and inner cut as /zq under. */
	public static List<int[]> ring(int minX, int minZ, int maxX, int maxZ) {
		List<int[]> out = new ArrayList<>();
		for (int x = minX - 3; x <= maxX + 3; x += 2) {
			for (int z = minZ - 3; z <= maxZ + 3; z += 2) {
				if (x > minX - 3 && x < maxX + 2 && z > minZ - 3 && z < maxZ + 2) continue;
				out.add(new int[]{x, z});
			}
		}
		return out;
	}

	/** The n largest entries, count descending, then key ascending (stable output for batch regexes). */
	public static Map<String, Integer> top(Map<String, Integer> m, int n) {
		Map<String, Integer> out = new LinkedHashMap<>();
		m.entrySet().stream()
				.sorted(Comparator.comparing((Map.Entry<String, Integer> e) -> -e.getValue()).thenComparing(Map.Entry::getKey))
				.limit(n)
				.forEach(e -> out.put(e.getKey(), e.getValue()));
		return out;
	}

	/** Base earth ids never seen in the ring (ground match should have swapped them), sorted; "-" when none. */
	public static String foreign(Map<String, Integer> baseEarth, Set<String> ring) {
		List<String> f = new ArrayList<>();
		for (String k : baseEarth.keySet()) if (!ring.contains(k)) f.add(k);
		f.sort(null);
		return f.isEmpty() ? "-" : String.join(",", f);
	}

	/** The falling blocks /zq floating looks for. */
	public static final Set<String> FALLING = Set.of("minecraft:sand", "minecraft:red_sand", "minecraft:gravel");

	/** A falling block with air (or cave air) right below it. */
	public static boolean floats(String id, String below) {
		return FALLING.contains(id) && ("minecraft:air".equals(below) || "minecraft:cave_air".equals(below));
	}

	/** Comma (or space) separated ids, blanks dropped, order kept; refused when empty. */
	public static List<String> list(String raw) {
		List<String> out = new ArrayList<>();
		for (String s : raw.split("[,\\s]+")) if (!s.isBlank()) out.add(s.trim());
		if (out.isEmpty()) throw new TestArgs.Bad("missing ids");
		return out;
	}
}
