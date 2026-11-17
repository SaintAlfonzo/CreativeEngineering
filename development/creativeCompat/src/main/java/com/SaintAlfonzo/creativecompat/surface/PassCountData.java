package com.SaintAlfonzo.creativecompat.surface;

import java.util.function.IntPredicate;

import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;

/**
 * The neighbor counts of one chunk (plan_post_scratch_1115b.md C2): each counted block's same-tier neighbor count,
 * computed once on first use and kept until a neighbor is broken or placed ({@link PassCounts} drops it). Sparse: one
 * entry per counted block of a tier with density, keyed by its chunk-local position.
 *
 * <p>Entry value: the count (bits 0-2, 0 to 6) and the shape it was counted with (bit 3: faces). A lookup with the other
 * shape misses, so a config change from {@code SIDES} to {@code FACES} only means a recount. The entry set carries a
 * data version ({@link #tagVersion}, the hash of the tier tags it was counted under) and a runtime generation; a
 * mismatch of either empties it on the next lookup, so a stale or missing entry is never wrong, only a recount.
 *
 * <p>Saved in the chunk on the server ({@link #save}, {@link #load}); the client keeps its own copy, unsaved. Plain
 * Java (fastutil only) so the headless harness can test it.
 */
public final class PassCountData {

    /** Runtime generation: bumped by {@link #forgetAll()} (tag reload, bench cold cases). Not saved. */
    private static volatile int generation = 1;
    /** Hash of the tier tags the counts depend on; set on every tag reload ({@link PassCounts}). Saved. */
    private static volatile int currentTagVersion;

    private final Int2ByteOpenHashMap map = new Int2ByteOpenHashMap();
    private int gen = generation;
    private int tagVersion = currentTagVersion;

    public PassCountData() {
        map.defaultReturnValue((byte) -1);
    }

    /** Every chunk's counts are stale (tag reload, or a cold benchmark): each recounts on its next lookup. */
    public static void forgetAll() {
        int g = generation + 1;
        generation = g == 0 ? 1 : g;
    }

    /** The tier tags changed: counts saved under another hash are dropped when their chunk is next read. */
    public static void setTagVersion(int version) {
        currentTagVersion = version;
        forgetAll();
    }

    public static int tagVersion() {
        return currentTagVersion;
    }

    /** Chunk-local key: x and z (4 bits each), y above the level's bottom (up to 12 bits). */
    public static int key(int x, int y, int z, int minY) {
        return (y - minY) << 8 | (z & 15) << 4 | (x & 15);
    }

    public static int keyX(int key) {
        return key & 15;
    }

    public static int keyZ(int key) {
        return key >>> 4 & 15;
    }

    /** {@code key}'s y given the level's bottom. */
    public static int keyY(int key, int minY) {
        return (key >>> 8) + minY;
    }

    private void check() {
        if (gen != generation || tagVersion != currentTagVersion) {
            map.clear();
            gen = generation;
            tagVersion = currentTagVersion;
        }
    }

    /** The count stored for this block and shape, or -1. */
    public int get(int key, boolean faces) {
        check();
        byte v = map.get(key);
        if (v < 0) return -1;
        if (((v & 8) != 0) != faces) return -1;
        return v & 7;
    }

    public void put(int key, boolean faces, int count) {
        check();
        map.put(key, (byte) ((count & 7) | (faces ? 8 : 0)));
    }

    /** Drops one block's entry (it or a neighbor was broken or placed). */
    public void remove(int key) {
        map.remove(key);
    }

    /** Drops every entry on one edge of the chunk: 0 west (x 0), 1 east (x 15), 2 north (z 0), 3 south (z 15). */
    public void dropEdge(int side) {
        map.keySet().removeIf((int k) -> switch (side) {
            case 0 -> keyX(k) == 0;
            case 1 -> keyX(k) == 15;
            case 2 -> keyZ(k) == 0;
            default -> keyZ(k) == 15;
        });
    }

    /** Drops the entries on all four edges (the chunk was loaded: a neighbor may have changed meanwhile). */
    public void dropEdges() {
        map.keySet().removeIf((int k) -> {
            int x = keyX(k), z = keyZ(k);
            return x == 0 || x == 15 || z == 0 || z == 15;
        });
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        check();
        return map.size();
    }

    /** The entries to save ({@code key << 4 | value}), leaving out those {@code keep} refuses (a block that is gone). */
    public int[] save(IntPredicate keep) {
        check();
        int[] out = new int[map.size()];
        int n = 0;
        for (Int2ByteMap.Entry e : map.int2ByteEntrySet()) {
            if (!keep.test(e.getIntKey())) continue;
            out[n++] = e.getIntKey() << 4 | (e.getByteValue() & 15);
        }
        return n == out.length ? out : java.util.Arrays.copyOf(out, n);
    }

    /** Fills from {@link #save}'s array written under {@code savedTagVersion}; another version is dropped. */
    public void load(int savedTagVersion, int[] entries) {
        map.clear();
        gen = generation;
        tagVersion = currentTagVersion;
        if (savedTagVersion != currentTagVersion) return;
        for (int e : entries) map.put(e >>> 4, (byte) (e & 15));
    }
}
