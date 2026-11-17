package com.SaintAlfonzo.creativecompat.surface;

/**
 * The pass-through rule for one block (plan sections 1 and 2): given its tier's values and how many neighbours of the
 * same tier it has, the walk and fall multipliers that apply inside it, whether density lowered them, and whether the
 * block is surrounded (a full count in a {@code surrounded = web} tier). Shared by movement ({@link PassThrough}),
 * items and projectiles (phase 2) and the WTHIT line (phase 5), so they always agree.
 *
 * <p>Plain Java (no game classes) so the headless harness can test it.
 */
public final class PassRule {

    private PassRule() {}

    /**
     * One counted block.
     *
     * @param neighbours same-tier neighbours counted (0 when the tier counts none)
     * @param walk       horizontal multiplier inside (dense value when dense)
     * @param fall       downward multiplier inside, and the fall-distance factor per block passed
     * @param dense      the count reached {@code denseFrom}
     * @param walkDense  density lowered the walk value (what "(Dense neighbors)" marks)
     * @param fallDense  density lowered the fall value
     * @param surrounded the count reached {@code surroundedAt} in a {@code surrounded = web} tier (6 of 6 faces)
     * @param resetFall  dense with {@code denseResetsFall}: fall distance goes to 0 every tick
     */
    public record Cell(PassTier tier, int neighbours, double walk, double fall, boolean dense, boolean walkDense,
                       boolean fallDense, boolean surrounded, boolean resetFall) {}

    public static Cell cell(PassTier tier, PassTier.Values v, int neighbours) {
        boolean counts = v.neighbours() != PassTier.Neighbours.NONE;
        int n = counts ? neighbours : 0;
        boolean dense = counts && n >= v.denseFrom();
        double walk = dense ? v.denseWalk() : v.walk();
        double fall = dense ? v.denseFall() : v.fall();
        boolean surrounded = counts && v.surrounded() == PassTier.Surround.WEB && n >= v.surroundedCount();
        return new Cell(tier, n, walk, fall, dense, dense && v.denseWalk() < v.walk(), dense && v.denseFall() < v.fall(),
                surrounded, dense && v.denseResetsFall());
    }

    /**
     * A layer height block's rule (plan_post_scratch_1115b.md D2): {@code walk} from {@link #layerWalk}, the tier's
     * fall value, no density.
     */
    public static Cell layerCell(PassTier tier, PassTier.Values v, double walk) {
        return new Cell(tier, 0, walk, v.fall(), false, false, false, false, false);
    }

    /**
     * Highest walk-through layer {@code H = max - solidBelowFull - 1} of a block whose layers run up to {@code max}
     * ({@code solidBelowFull} clamped to 0..max).
     */
    public static int highestWalkThrough(int max, int solidBelowFull) {
        int s = Math.max(-1, Math.min(solidBelowFull, max)); // -1: never acts whole (leaf layers)
        return max - s - 1;
    }

    /**
     * Walk value inside a layer block of {@code layers} out of {@code max} (plan_post_scratch_1115b.md D2), or NaN
     * when the block acts whole (no pass-through rule: {@code layers >= max - solidBelowFull}, or {@code H < 2} so it
     * never slows). One layer: 1.0. From 2 layers the value falls in equal steps to {@code layerMaxSlow} at H:
     * {@code walk(l) = 1 - (1 - layerMaxSlow) * (l - 1) / (H - 1)}.
     */
    public static double layerWalk(int layers, int max, int solidBelowFull, double layerMaxSlow) {
        int h = highestWalkThrough(max, solidBelowFull);
        if (h < 2 || layers > h) return Double.NaN;
        if (layers < 2) return 1.0;
        return 1.0 - (1.0 - layerMaxSlow) * (layers - 1) / (double) (h - 1);
    }

    /** The neighbour count only matters for a tier that counts them; skip the block lookups otherwise. */
    public static boolean needsCount(PassTier.Values v) {
        return v.neighbours() != PassTier.Neighbours.NONE;
    }
}
