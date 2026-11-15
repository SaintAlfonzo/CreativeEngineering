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
     * @param surrounded full count in a {@code surrounded = web} tier (26 of 26 webs)
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
        boolean surrounded = counts && v.surrounded() == PassTier.Surround.WEB && n >= v.neighbours().max();
        return new Cell(tier, n, walk, fall, dense, dense && v.denseWalk() < v.walk(), dense && v.denseFall() < v.fall(),
                surrounded, dense && v.denseResetsFall());
    }

    /** The neighbour count only matters for a tier that counts them; skip the block lookups otherwise. */
    public static boolean needsCount(PassTier.Values v) {
        return v.neighbours() != PassTier.Neighbours.NONE;
    }
}
