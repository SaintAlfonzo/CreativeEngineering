package com.SaintAlfonzo.creativecompat.surface;

/**
 * Per-entity pass-through state ({@link PassHolder}): last tick's counted cells (to find the newly entered ones for
 * the per-block fall-distance factor, plan 1.2), whether the speed modifier is on and at what amount, and a reusable
 * {@link PassResult} so the per-tick scan does not allocate.
 */
public final class PassState {

    /** This tick's result; also readable by later phases right after the hook ran. */
    public final PassResult result = new PassResult();

    long[] lastCells = new long[8];
    int lastCount;

    /** {@code tickCount} of the entity when {@link #result} was last filled (fireworks read it at their tick end). */
    int resultTick = Integer.MIN_VALUE;

    // ---- items and orbs stuck in a web (plan 4.2; saved, PassSave) ----
    boolean itemStuck;
    long stuckCell;
    double stuckX, stuckY, stuckZ;
    /** NoGravity before the item stuck, restored when it is let go. */
    boolean priorNoGravity;
    /** Block state and config snapshot the stuck cell last passed the web test with (not saved). */
    net.minecraft.world.level.block.state.BlockState stuckState;
    PassValues stuckValues;

    // ---- projectiles (plan 5.1) ----
    /** Snag group: held at the center of {@link #snagCell}. */
    boolean snagged;
    long snagCell;
    /** Linger group: held at the linger point while {@link #lingerCell} stays a web. */
    boolean lingering;
    long lingerCell;
    double lingerX, lingerY, lingerZ;
    /** Break group: the impact already ran on this side. */
    boolean broken;
    /** Break group: ticks in a row stalled in a web (plan_post_scratch_1115b.md B). */
    int stallTicks;

    // ---- arrow despawn (plan 5.4) ----
    /** 0 unknown, 1 not long-life (vanilla minute), 2 long-life eligible (pickable, with a pickup item). */
    byte longLife;
    /** Demoted to vanilla's minute by the per-chunk cap (saved, PassSave). */
    boolean shortLife;
    /** In the per-chunk long-life set of {@link #arrowChunk}. */
    boolean arrowJoined;
    long arrowChunk;

    /** Amount of the speed modifier currently on the entity, NaN when none. */
    double modifierAmount = Double.NaN;

    boolean hasModifier() {
        return !Double.isNaN(modifierAmount);
    }

    boolean wasIn(long pos) {
        for (int i = 0; i < lastCount; i++) {
            if (lastCells[i] == pos) return true;
        }
        return false;
    }

    /** Remembers this tick's counted cells as "last tick" for the next one. */
    void rememberCells() {
        int n = result.cells();
        if (lastCells.length < n) lastCells = new long[Math.max(n, lastCells.length * 2)];
        for (int i = 0; i < n; i++) lastCells[i] = result.cellPos(i);
        lastCount = n;
    }

    void forgetCells() {
        lastCount = 0;
    }

    /** Something here must be written with the entity (PassSave). */
    boolean needsSave() {
        return itemStuck || shortLife;
    }
}
