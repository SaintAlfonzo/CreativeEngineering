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
}
