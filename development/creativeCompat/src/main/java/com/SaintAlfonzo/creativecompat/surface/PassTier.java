package com.SaintAlfonzo.creativecompat.surface;

import java.util.Locale;

/**
 * The pass-through tiers (feature_writeup/plan_passthrough.md sections 1 and 2): blocks you stand in rather than on
 * (crops, grass, ropes, vines, cobwebs, leaf piles) and how fast you walk and fall through them. Each tier is a block
 * tag {@code creativecompat:passthrough/<key>} (kubejs blocktags.js) plus a config table
 * {@code [surface.passthrough.<key>]} whose defaults are {@link #defaults()}. {@link PassThrough} reads the values,
 * {@link PassRule} turns them into a speed for one block.
 *
 * <p>Plain Java on purpose (no game classes), so the headless test harness can check the defaults and the rule math.
 */
public enum PassTier {
    DENSE_CROP("dense_crop",
            "Crops grown in thick fields: corn, IE hemp, rice and rice panicles, Vintage Delight oats, wheat, tomatoes.",
            v(0.75, 1.0, Growth.TALL_HALF, Neighbours.SIDES, 3, 0.50, 1.0)),
    CROP("crop", "Every other planted crop, Farmer's Respite coffee.",
            v(0.75, 1.0, Growth.HALF, Neighbours.NONE, 3, 0.75, 1.0)),
    WILD_CROP("wild_crop", "Wild crops (Farmer's Delight and other mods).",
            v(0.75, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.75, 1.0)),
    VINE("vine", "Every vine: vanilla, twisting, weeping and cave vines, grape vines, modded vines. Slows only while moving through it.",
            v(0.75, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.75, 1.0).walkOnlyMoving(true)),
    TALL_PLANT("tall_plant", "2-tall plants: tall grass, large fern, double flowers, eyebulb.",
            v(0.75, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.75, 1.0)),
    CANE("cane", "Sugar cane, Nether's Delight propelplant.",
            v(0.50, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.50, 1.0)),
    ROPE("rope", "Ropes (Farmer's Delight, Supplementaries, Quark). Not while climbing: ladder rules stay vanilla.",
            v(0.75, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.75, 1.0)),
    COBWEB("cobweb", "Cobwebs, reinforced cobwebs, Fantasy Furniture webs, Create Crafts & Additions barbed wire, IE razor wire.",
            v(0.50, 0.85, Growth.ANY, Neighbours.FACES, 3, 0.35, 0.20).denseResetsFall(true)
                    .surrounded(Surround.WEB).items(ItemRule.STICK).projectiles(true)),
    BUSH("bush", "Berry bushes: sweet berry, raspberry, grape bushes.",
            v(0.36, 1.0, Growth.ANY, Neighbours.NONE, 3, 0.36, 1.0)),
    /**
     * Layer height blocks (plan_post_scratch_1115b.md D, owner): snow layers, leaf piles, bamboo leaves. Replaces the
     * leaf_pile tier. The walk value comes from the block's height ({@link PassRule#layerWalk}), not from {@code walk};
     * tag {@code creativecompat:passthrough_layers}.
     */
    LAYERS("layers", "Layer height blocks: snow layers, leaf piles, bamboo leaves. "
            + "The walk value falls with the height (layerMaxSlow at the highest walk-through layer); high piles act whole.",
            v(1.0, 1.0, Growth.ANY, Neighbours.NONE, 3, 1.0, 1.0).minLayers(2).depthStriderPasses(true)
                    .layers(0.5, 2)), // LAYER_MAX_SLOW, SOLID_BELOW_FULL
    GRASS("grass", "Short grass, fern and other short plants. 1.0 = no effect; listed so it can be tuned.",
            v(1.0, 1.0, Growth.ANY, Neighbours.NONE, 3, 1.0, 1.0));

    /** Which growth state of a block counts (plan 1.1). */
    public enum Growth {
        /** Always counts. */
        ANY,
        /** Its own growth (age, or hemp's growth enum) is at least half its maximum. */
        HALF,
        /** Tall crops: the lower block's growth decides for both blocks; an upper block looks down one block. */
        TALL_HALF
    }

    /**
     * Which neighbours count toward density (plan_post_scratch_1115b.md C1): none, the 4 side-adjacent cells on the
     * same layer, or the 6 face-adjacent cells. {@code RING} and {@code CUBE} are the pre-2026-11-15 names, kept only
     * as aliases for code that still names them ({@link #canonical()}); the config accepts the three new names, and
     * {@link PassConfigMigration} rewrites an old file.
     */
    public enum Neighbours {
        NONE(0), SIDES(4), FACES(6),
        /** @deprecated old name, counts as {@link #SIDES}. */
        @Deprecated RING(4),
        /** @deprecated old name, counts as {@link #FACES}. */
        @Deprecated CUBE(6);

        private final int max;

        Neighbours(int max) {
            this.max = max;
        }

        /** A full count: every neighbour cell is in the tier. */
        public int max() {
            return max;
        }

        /** The current name of this shape (an old alias maps to its new one). */
        @SuppressWarnings("deprecation")
        public Neighbours canonical() {
            return this == RING ? SIDES : this == CUBE ? FACES : this;
        }

        /** The 6 face cells (true) or the 4 side cells (false). */
        public boolean vertical() {
            return canonical() == FACES;
        }

        /** The names the config accepts. */
        public static final java.util.List<Neighbours> CONFIG = java.util.List.of(NONE, SIDES, FACES);
    }

    /** What a full neighbour count does (plan 4.3). */
    public enum Surround { NONE, WEB }

    /** What happens to dropped items and orbs inside the tier (plan 4.2). */
    public enum ItemRule { NONE, STICK }

    /**
     * Every config value of one tier, as one immutable snapshot (also the wire format of {@link PassSync}).
     * {@code surroundedAt}: the count that makes a {@code WEB} tier surrounded (clamped to the shape's max);
     * {@code minLayers}, {@code layerMaxSlow}, {@code solidBelowFull}: the layer height rule ({@link #LAYERS} only).
     */
    public record Values(double walk, double fall, Growth growth, Neighbours neighbours, int denseFrom,
                         double denseWalk, double denseFall, boolean denseResetsFall, Surround surrounded,
                         int surroundedAt, int minLayers, boolean walkOnlyMoving, ItemRule items,
                         double itemStickChance, boolean projectiles, boolean depthStriderPasses,
                         double layerMaxSlow, int solidBelowFull) {

        Values walkOnlyMoving(boolean b) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, minLayers, b, items, itemStickChance, projectiles, depthStriderPasses,
                    layerMaxSlow, solidBelowFull);
        }

        Values denseResetsFall(boolean b) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, b,
                    surrounded, surroundedAt, minLayers, walkOnlyMoving, items, itemStickChance, projectiles,
                    depthStriderPasses, layerMaxSlow, solidBelowFull);
        }

        Values surrounded(Surround s) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    s, surroundedAt, minLayers, walkOnlyMoving, items, itemStickChance, projectiles, depthStriderPasses,
                    layerMaxSlow, solidBelowFull);
        }

        Values items(ItemRule i) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, minLayers, walkOnlyMoving, i, itemStickChance, projectiles,
                    depthStriderPasses, layerMaxSlow, solidBelowFull);
        }

        Values projectiles(boolean b) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, minLayers, walkOnlyMoving, items, itemStickChance, b, depthStriderPasses,
                    layerMaxSlow, solidBelowFull);
        }

        Values minLayers(int n) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, n, walkOnlyMoving, items, itemStickChance, projectiles,
                    depthStriderPasses, layerMaxSlow, solidBelowFull);
        }

        Values depthStriderPasses(boolean b) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, minLayers, walkOnlyMoving, items, itemStickChance, projectiles, b,
                    layerMaxSlow, solidBelowFull);
        }

        Values layers(double maxSlow, int solidBelow) {
            return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, denseResetsFall,
                    surrounded, surroundedAt, minLayers, walkOnlyMoving, items, itemStickChance, projectiles,
                    depthStriderPasses, maxSlow, solidBelow);
        }

        /** The count that makes a block surrounded: {@code surroundedAt}, at most the shape's max. */
        public int surroundedCount() {
            return Math.min(surroundedAt, neighbours.max());
        }

        /** Nothing in this tier changes movement (all speeds 1.0, no fall reset, no surround): the scan can skip it. */
        public boolean inert() {
            if (walk == 1.0 && layerMaxSlow < 1.0) return false; // layers: the walk comes from the height
            return walk == 1.0 && fall == 1.0 && (neighbours == Neighbours.NONE
                    || (denseWalk == 1.0 && denseFall == 1.0 && !denseResetsFall && surrounded == Surround.NONE));
        }
    }

    /**
     * Defaults that are the same for every tier: no density effect, no reset, surrounded at 6, no layers rule
     * (layerMaxSlow 1.0 outside the layer tier), item chance 0.35.
     */
    private static Values v(double walk, double fall, Growth growth, Neighbours neighbours, int denseFrom,
                            double denseWalk, double denseFall) {
        return new Values(walk, fall, growth, neighbours, denseFrom, denseWalk, denseFall, false, Surround.NONE,
                6, 1, false, ItemRule.NONE, 0.35, false, false, 1.0, 2);
    }

    /** Layer rule defaults (plan_post_scratch_1115b.md D2): 0.5 at the highest walk-through layer, 2 stages solid. */
    public static final double LAYER_MAX_SLOW = 0.5;
    public static final int SOLID_BELOW_FULL = 2;

    private final String key;
    private final String description;
    private final Values defaults;

    PassTier(String key, String description, Values defaults) {
        this.key = key;
        this.description = description;
        this.defaults = defaults;
    }

    /** Toml table under {@code [surface.passthrough]}, and the tag path after {@code creativecompat:passthrough/}. */
    public String key() {
        return key;
    }

    /**
     * The block tag path: {@code passthrough/<key>}, except the layer tier's {@code passthrough_layers}
     * (plan_post_scratch_1115b.md D). The entity exempt tag stays {@code passthrough_exempt/<key>}.
     */
    public String tagPath() {
        return this == LAYERS ? "passthrough_layers" : "passthrough/" + key;
    }

    /** The walk value comes from the block's layer height ({@link PassRule#layerWalk}). */
    public boolean layered() {
        return this == LAYERS;
    }

    public String description() {
        return description;
    }

    public Values defaults() {
        return defaults;
    }

    /**
     * Rope: the walk value does not apply while climbing (on the rope and moving up or down, sneaking on it, or off
     * the ground; plan 3.2). Not a config key: it is what makes a rope a rope.
     */
    public boolean noWalkWhileClimbing() {
        return this == ROPE;
    }

    /** Display name for logs and the WTHIT line ("Dense Crop"). */
    public String title() {
        StringBuilder b = new StringBuilder();
        for (String part : key.split("_")) {
            if (b.length() > 0) b.append(' ');
            b.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return b.toString();
    }
}
