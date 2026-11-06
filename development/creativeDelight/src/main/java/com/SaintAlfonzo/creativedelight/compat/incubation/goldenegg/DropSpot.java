package com.SaintAlfonzo.creativedelight.compat.incubation.goldenegg;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Picks where a golden egg lands: the spot near the goose with the fewest walls around it. Pure logic on
 * block offsets from the goose's own block (no Minecraft types, so it can be tested without the game);
 * {@link GoldenGooseEgg} answers the {@link Probe} questions from the level.
 *
 * Candidates are the spots within {@link #RANGE} blocks horizontally, one block up or down, nearest first.
 * A spot's score is the number of solid blocks among its 8 horizontal neighbours at foot and head height
 * (0..16). The first candidate scoring 0 wins, else the lowest score; a candidate must beat the goose's own
 * spot, otherwise the egg lands where the goose stands.
 */
public final class DropSpot {

    public static final int RANGE = 3;

    /** What the level looks like, by offset from the goose's block. */
    public interface Probe {
        /** Feet and head free (no collision, no fluid) and something to land on below. */
        boolean standable(int dx, int dy, int dz);

        /** Counts as a wall. */
        boolean solid(int dx, int dy, int dz);

        /** In line of sight of the goose, so an egg never lands on the far side of a wall. */
        boolean reachable(int dx, int dy, int dz);
    }

    /** Candidate offsets {dx, dy, dz}, nearest first, the goose's own spot left out. */
    private static final List<int[]> CANDIDATES = new ArrayList<>();

    static {
        for (int dx = -RANGE; dx <= RANGE; dx++)
            for (int dz = -RANGE; dz <= RANGE; dz++)
                for (int dy = -1; dy <= 1; dy++)
                    if ((dx != 0 || dy != 0 || dz != 0) && dx * dx + dz * dz <= RANGE * RANGE)
                        CANDIDATES.add(new int[]{dx, dy, dz});
        // Stable sort: horizontal distance, then height change; ties keep the loop order (deterministic).
        CANDIDATES.sort(Comparator.<int[]>comparingInt(c -> c[0] * c[0] + c[2] * c[2])
                .thenComparingInt(c -> Math.abs(c[1])));
    }

    private DropSpot() {}

    /** Solid blocks among the 8 horizontal neighbours of the spot, at foot and head height. */
    public static int score(Probe probe, int dx, int dy, int dz) {
        int walls = 0;
        for (int y = dy; y <= dy + 1; y++)
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++)
                    if ((x != 0 || z != 0) && probe.solid(dx + x, y, dz + z)) walls++;
        return walls;
    }

    /** The offset {dx, dy, dz} to drop the egg at; {0, 0, 0} = where the goose stands. */
    public static int[] pick(Probe probe) {
        int[] best = {0, 0, 0};
        int bestScore = score(probe, 0, 0, 0);
        if (bestScore == 0) return best;
        for (int[] c : CANDIDATES) {
            if (!probe.standable(c[0], c[1], c[2]) || !probe.reachable(c[0], c[1], c[2])) continue;
            int s = score(probe, c[0], c[1], c[2]);
            if (s == 0) return c.clone();
            if (s < bestScore) {
                best = c;
                bestScore = s;
            }
        }
        return best.clone();
    }

    /**
     * Where to place the egg as a block: like {@link #pick} but the goose's own spot is never an option (the
     * block would land on the goose). Null when no candidate is standable and reachable (a closed 1x1 pen).
     */
    @Nullable
    public static int[] pickBlock(Probe probe) {
        int[] best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int[] c : CANDIDATES) {
            if (!probe.standable(c[0], c[1], c[2]) || !probe.reachable(c[0], c[1], c[2])) continue;
            int s = score(probe, c[0], c[1], c[2]);
            if (s == 0) return c.clone();
            if (s < bestScore) {
                best = c;
                bestScore = s;
            }
        }
        return best == null ? null : best.clone();
    }
}
