package com.SaintAlfonzo.creativecompat.surface;

/**
 * The arithmetic of the pass-through system (plan 1.1, 1.2, 1.3, 4.2), kept free of game classes so the headless
 * harness can check it: growth thresholds, the speed modifier, wading drag, fall distance per block passed, the field
 * of view recomputation and the item stick point.
 */
public final class PassMath {

    private PassMath() {}

    // ---- growth (plan 1.1) ----

    /** An integer growth property ({@code age}) is at least half way: {@code (value - min) / (max - min) >= 0.5}. */
    public static boolean halfGrown(int value, int min, int max) {
        if (max <= min) return true;
        return (long) (value - min) * 2L >= (long) (max - min);
    }

    /**
     * An ordered growth enum's lower-block stages (IE hemp: bottom0..bottom4): stage {@code index} of {@code count}
     * is at least half way. A single stage counts as grown.
     */
    public static boolean enumHalfGrown(int index, int count) {
        if (count <= 1) return true;
        return index * 2 >= count - 1;
    }

    // ---- walking (plan 1.2) ----

    /** The {@code MULTIPLY_TOTAL} modifier amount for a walk multiplier ({@code walk - 1}). */
    public static double modifierAmount(double walk) {
        return walk - 1.0;
    }

    /**
     * Horizontal velocity factor per tick that gives {@code walk} times the normal steady wading speed. In water the
     * speed attribute does nothing: each tick adds a fixed push {@code a}, moves, and keeps {@code keep} of the
     * velocity (0.8 walking, 0.9 sprinting). With an extra factor {@code m} after the move the steady distance per
     * tick is {@code a / (1 - m keep)} instead of {@code a / (1 - keep)}, so {@code m = (1 - (1 - keep) / walk) / keep}.
     * Clamped to 0..1 (a walk value at or below {@code 1 - keep} stops wading).
     */
    public static double wadeFactor(double walk, double keep) {
        if (walk >= 1.0) return 1.0;
        if (keep <= 0.0) return 1.0;
        double m = (1.0 - (1.0 - keep) / walk) / keep;
        return Math.max(0.0, Math.min(1.0, m));
    }

    /** Steady wading distance per tick relative to normal for a per-tick factor {@code m} (for the test). */
    public static double wadeRatio(double m, double keep) {
        return (1.0 - keep) / (1.0 - m * keep);
    }

    // ---- falling (plan 1.2) ----

    /** Fall distance after passing through blocks with these fall factors, one factor per block passed. */
    public static double fallDistanceAfter(double start, double... factors) {
        double d = start;
        for (double f : factors) d *= f;
        return d;
    }

    // ---- attributes and field of view (plan 1.3) ----

    /**
     * An attribute's value from its base and its modifiers by operation, the way vanilla's
     * {@code AttributeInstance#calculateValue} adds them up (ADDITION, then MULTIPLY_BASE on the sum, then
     * MULTIPLY_TOTAL as factors), before the attribute's own clamp.
     */
    public static double attributeValue(double base, double[] addition, double[] multiplyBase, double[] multiplyTotal) {
        double d = base;
        for (double a : addition) d += a;
        double v = d;
        for (double a : multiplyBase) v += d * a;
        for (double a : multiplyTotal) v *= 1.0 + a;
        return v;
    }

    /** Vanilla's speed part of the field of view: {@code (flying ? 1.1 : 1) x (speed / walkSpeed + 1) / 2}. */
    public static float fovSpeedPart(boolean flying, double speed, float walkingSpeed) {
        float f = flying ? 1.1F : 1.0F;
        return f * (((float) speed / walkingSpeed + 1.0F) / 2.0F);
    }

    // ---- items (plan 4.2, used from phase 2) ----

    /**
     * Where an item moving from {@code from} to {@code to} this tick stops inside the cell at {@code cell} (its minimum
     * corner, a 1x1x1 box): the path is clipped to the cell, and the stop point is {@code roll} (0..1) of the way
     * along the clipped part. {@code roll < 0} means no roll hit this tick: the item stops just inside the exit point
     * when the path leaves the cell, or returns null when the path ends inside it (keep moving, roll again next tick).
     * Returns null when the path never enters the cell. The point is always inside the cell, at least {@code inset}
     * from its faces.
     */
    public static double[] stickPoint(double[] from, double[] to, double[] cell, double roll, double inset) {
        double t0 = 0.0, t1 = 1.0;
        for (int a = 0; a < 3; a++) {
            double d = to[a] - from[a];
            double lo = cell[a], hi = cell[a] + 1.0;
            if (Math.abs(d) < 1.0E-12) {
                if (from[a] < lo || from[a] > hi) return null;
                continue;
            }
            double ta = (lo - from[a]) / d, tb = (hi - from[a]) / d;
            if (ta > tb) { double s = ta; ta = tb; tb = s; }
            t0 = Math.max(t0, ta);
            t1 = Math.min(t1, tb);
            if (t0 > t1) return null;
        }
        double t;
        if (roll >= 0.0) {
            t = t0 + (t1 - t0) * Math.min(1.0, roll);
        } else {
            boolean leaves = t1 < 1.0;
            if (!leaves) return null;
            t = t1;
        }
        double[] p = new double[3];
        for (int a = 0; a < 3; a++) {
            double v = from[a] + (to[a] - from[a]) * t;
            p[a] = Math.max(cell[a] + inset, Math.min(cell[a] + 1.0 - inset, v));
        }
        return p;
    }

    /**
     * Entry and exit fractions {@code {t0, t1}} of the segment {@code from -> to} inside the unit cell at {@code cell}
     * (its minimum corner), or null when the segment misses it. A zero-length segment gives {@code {0, 0}} when the
     * point is inside.
     */
    public static double[] clip(double[] from, double[] to, double[] cell) {
        double t0 = 0.0, t1 = 1.0;
        for (int a = 0; a < 3; a++) {
            double d = to[a] - from[a];
            double lo = cell[a], hi = cell[a] + 1.0;
            if (Math.abs(d) < 1.0E-12) {
                if (from[a] < lo || from[a] > hi) return null;
                continue;
            }
            double ta = (lo - from[a]) / d, tb = (hi - from[a]) / d;
            if (ta > tb) { double s = ta; ta = tb; tb = s; }
            t0 = Math.max(t0, ta);
            t1 = Math.min(t1, tb);
            if (t0 > t1) return null;
        }
        return new double[]{t0, t1};
    }

    /**
     * Plan 4.2 over every stick cell along one tick's path (phase 2, {@code surface.PassItems}). {@code cells} are the
     * minimum corners of the stick-tier cells the path box covers (first {@code n} used, any order). The path's first
     * run of touching web cells decides: a roll hit ({@code roll} 0..1) stops the item {@code roll} of the way along
     * that run; no hit ({@code roll < 0}) stops it just inside the run's exit when the path leaves the webs this tick,
     * or returns null when the path ends inside them (roll again next tick). A path with no length inside a web sticks
     * where it is. The point is always inside one of the cells, at least {@code inset} from its faces. Null when the
     * path enters no cell.
     */
    public static double[] stickAlong(double[] from, double[] to, double[][] cells, int n, double roll, double inset) {
        double dx = to[0] - from[0], dy = to[1] - from[1], dz = to[2] - from[2];
        if (dx * dx + dy * dy + dz * dz < 1.0E-10) {
            for (int i = 0; i < n; i++) {
                if (clip(from, from, cells[i]) != null) return clampInto(from, cells[i], inset);
            }
            return null;
        }
        double[] t0 = new double[n], t1 = new double[n];
        int[] idx = new int[n];
        int m = 0;
        for (int i = 0; i < n; i++) {
            double[] c = clip(from, to, cells[i]);
            if (c == null || c[1] - c[0] < 1.0E-9) continue; // missed, or only grazes an edge
            // insertion by entry fraction
            int k = m++;
            while (k > 0 && t0[k - 1] > c[0]) {
                t0[k] = t0[k - 1];
                t1[k] = t1[k - 1];
                idx[k] = idx[k - 1];
                k--;
            }
            t0[k] = c[0];
            t1[k] = c[1];
            idx[k] = i;
        }
        if (m == 0) return null;
        double runStart = t0[0], runEnd = t1[0];
        int last = 0, end = 1;
        for (; end < m; end++) {
            if (t0[end] > runEnd + 1.0E-9) break;
            if (t1[end] > runEnd) {
                runEnd = t1[end];
                last = end;
            }
        }
        double t;
        int in;
        if (roll >= 0.0) {
            t = runStart + (runEnd - runStart) * Math.min(1.0, roll);
            in = 0;
            for (int k = 0; k < end; k++) {
                if (t0[k] <= t + 1.0E-12 && t <= t1[k] + 1.0E-12) in = k;
            }
        } else {
            if (runEnd >= 1.0 - 1.0E-12) return null;
            t = runEnd;
            in = last;
        }
        double[] p = {from[0] + dx * t, from[1] + dy * t, from[2] + dz * t};
        return clampInto(p, cells[idx[in]], inset);
    }

    private static double[] clampInto(double[] p, double[] cell, double inset) {
        double[] q = new double[3];
        for (int a = 0; a < 3; a++) q[a] = Math.max(cell[a] + inset, Math.min(cell[a] + 1.0 - inset, p[a]));
        return q;
    }

    // ---- surrounded webs (plan 4.3) ----

    /** Vanilla ladder climb speed while jumping ({@code LivingEntity#travel}). */
    public static final double LADDER_CLIMB = 0.2;
    /** Vanilla ladder slide cap ({@code LivingEntity#handleOnClimbable}). */
    public static final double LADDER_SLIDE = 0.15;

    /**
     * Vertical speed in a surrounded web (plan 4.3): jumping climbs at ladder speed x the dense walk value, sneaking
     * descends at ladder slide speed x the same value, no input hangs (0).
     */
    public static double surroundedVertical(boolean jumping, boolean sneaking, double denseWalk) {
        if (jumping) return LADDER_CLIMB * denseWalk;
        if (sneaking) return -LADDER_SLIDE * denseWalk;
        return 0.0;
    }

    // ---- arrow despawn (plan 5.4) ----

    /** Vanilla's stuck-arrow life ({@code AbstractArrow#tickDespawn}). */
    public static final int VANILLA_ARROW_LIFE = 1200;

    /**
     * Despawn limit for a stuck arrow: -1 = leave vanilla's code alone (not pickable, or the system is off), vanilla's
     * minute when demoted by the per-chunk cap, else the long life.
     */
    public static int despawnLimit(boolean enabled, boolean pickable, boolean demoted, int longLife) {
        if (!enabled || !pickable) return -1;
        return demoted ? VANILLA_ARROW_LIFE : longLife;
    }

    /** Index of the oldest arrow (highest {@code life}; the earliest listed on a tie), -1 for none. */
    public static int oldestIndex(int[] lives, int n) {
        int best = -1;
        for (int i = 0; i < n; i++) {
            if (best < 0 || lives[i] > lives[best]) best = i;
        }
        return best;
    }
}
