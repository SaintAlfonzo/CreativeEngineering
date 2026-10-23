package com.SaintAlfonzo.incubation_additions.goldenegg;

/**
 * A bird that may lay a golden egg instead of its next egg (geese, mixin/GooseEntityEggLayerMixin).
 * Lives outside the mixin package so LayEggInNestGoalMixin can test for it.
 */
public interface GoldenEggLayer {

    /** True from the winning roll until the golden egg is dropped; the nest goal stays off meanwhile. */
    boolean incubation_additions$isGoldenEggDue();
}
