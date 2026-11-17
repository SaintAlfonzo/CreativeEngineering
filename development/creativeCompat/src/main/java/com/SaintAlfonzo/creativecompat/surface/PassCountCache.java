package com.SaintAlfonzo.creativecompat.surface;

/**
 * Kept for Creative Devtools' bench, whose {@link #clear()} now also makes every chunk recount
 * ({@link PassCountData#forgetAll()}), so its cold cases stay cold.
 *
 * <p>Was: neighbour counts of the current tick, per block position (plan_passthrough.md 7.2 fallback, scratch fixes): a
 * herd in a field or a crowd in a web block shares cells, so the second entity in a cell reads the count instead of
 * doing the 8 or 26 block lookups again. One per thread ({@link #get()}: server thread, client thread); a new level or
 * game time starts an empty table (a stamp bump, nothing cleared). A block changed during the tick can leave a count
 * one tick stale, which is harmless for a speed.
 *
 * <p>Open addressing, 4096 slots, up to 8 probes; when those are taken the home slot is overwritten. Plain Java (the
 * level is only an identity key), so the headless harness tests it.
 */
public final class PassCountCache {

    private static final int BITS = 12;
    private static final int SIZE = 1 << BITS;
    private static final int MASK = SIZE - 1;
    private static final int PROBES = 8;

    private static final ThreadLocal<PassCountCache> CACHE = ThreadLocal.withInitial(PassCountCache::new);

    private final long[] keys = new long[SIZE];
    /** {@code tag << 8 | count}; tag = tier ordinal * 2 + (faces ? 1 : 0). */
    private final int[] vals = new int[SIZE];
    private final int[] stamps = new int[SIZE];
    private int stamp = 1;
    private Object level;
    private long time = Long.MIN_VALUE;

    /** This thread's cache. */
    public static PassCountCache get() {
        return CACHE.get();
    }

    /** Forget everything (the bench's first-entity cases; tag reload), the baked chunk counts included. */
    public void clear() {
        stamp++;
        level = null;
        PassCountData.forgetAll();
    }

    /** Starts a new table when the level or the game time differs from the last call. */
    public void at(Object level, long time) {
        if (level != this.level || time != this.time) {
            this.level = level;
            this.time = time;
            stamp++;
        }
    }

    static int tag(PassTier tier, PassTier.Neighbours mode) {
        return tier.ordinal() * 2 + (mode.vertical() ? 1 : 0);
    }

    private static int home(long pos) {
        return (int) ((pos * 0x9E3779B97F4A7C15L) >>> (64 - BITS));
    }

    /** The count stored for this position and tier this tick, or -1. Call {@link #at} first. */
    public int count(long pos, int tag) {
        int i = home(pos);
        for (int k = 0; k < PROBES; k++, i = (i + 1) & MASK) {
            if (stamps[i] != stamp) return -1;
            if (keys[i] == pos) {
                int v = vals[i];
                return (v >>> 8) == tag ? (v & 0xFF) : -1;
            }
        }
        return -1;
    }

    public void put(long pos, int tag, int count) {
        int h = home(pos), i = h;
        for (int k = 0; k < PROBES; k++, i = (i + 1) & MASK) {
            if (stamps[i] != stamp || keys[i] == pos) {
                set(i, pos, tag, count);
                return;
            }
        }
        set(h, pos, tag, count);
    }

    private void set(int i, long pos, int tag, int count) {
        stamps[i] = stamp;
        keys[i] = pos;
        vals[i] = tag << 8 | count;
    }
}
