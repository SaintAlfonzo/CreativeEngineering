package com.SaintAlfonzo.creativedelight.hearth.item;

/**
 * Watering can tiers (pack addition; Hearth and Harvest 1.4.1 has one can, 16 / 16 on a 3x3, which is the gold tier
 * here). {@code size} is the side of the square area a use works on, {@code capacity} holds for water and fertiliser
 * each, {@code cauldronLevels} is what one fill takes from a water cauldron (a fill from a well or other water tank is
 * always one bucket, from a water source block free).
 */
public enum WateringCanTier {
    COPPER(1, 8, 1),
    IRON(2, 12, 1),
    GOLD(3, 16, 1),
    DIAMOND(4, 20, 2),
    NETHERITE(5, 32, 3),
    DRAGON(7, 64, 3);

    public final int size;
    public final int capacity;
    public final int cauldronLevels;

    WateringCanTier(int size, int capacity, int cauldronLevels) {
        this.size = size;
        this.capacity = capacity;
        this.cauldronLevels = cauldronLevels;
    }

    /**
     * Offsets {lo, hi} from the clicked block along one axis. Odd sizes are centred; even sizes take the extra row
     * on the side of the block that was clicked ({@code frac} = hit position within the block, 0..1).
     */
    public int[] span(double frac) {
        int half = size / 2;
        if (size % 2 == 1) return new int[] {-half, half};
        return frac < 0.5 ? new int[] {-half, half - 1} : new int[] {-(half - 1), half};
    }
}
