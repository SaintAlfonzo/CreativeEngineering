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
}
