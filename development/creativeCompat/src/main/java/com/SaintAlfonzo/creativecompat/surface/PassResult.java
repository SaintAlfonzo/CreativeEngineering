package com.SaintAlfonzo.creativecompat.surface;

import java.util.Arrays;

/**
 * Everything the pass-through blocks an entity is in add up to on one tick (plan 1 "Which block counts"): the lowest
 * walk and the lowest fall over every counted block, each with its own density, plus the surrounded and fall-reset
 * flags and the counted cells themselves. Filled by {@link PassThrough#evaluate}; read by movement here, by items and
 * projectiles (phase 2: {@link #surroundedCell}, {@link #fallTier}) and by anything else that needs the rule.
 *
 * <p>Mutable and reusable (one per entity in {@link PassState}), so the per-tick scan allocates nothing. Plain Java.
 */
public final class PassResult {

	/** Lowest walk multiplier over the counted blocks (1.0 = none). */
	public double walk = 1.0;
	/** Lowest fall multiplier over the counted blocks (1.0 = none). */
	public double fall = 1.0;
	/** Tier of the block that set {@link #walk} / {@link #fall}, or null. */
	public PassTier walkTier, fallTier;
	/** Density lowered the deciding block's value. */
	public boolean walkDense, fallDense;
	/** Neighbor count of the block that set {@link #walk} / {@link #fall} (0 for tiers that count none). */
	public int walkNeighbours, fallNeighbours;
	/** Some counted block is surrounded (plan 4.3: any web cell the entity overlaps). */
	public boolean surrounded;
	/** Packed position (BlockPos#asLong) of the first surrounded cell, valid when {@link #surrounded}. */
	public long surroundedCell;
	/** Tier of {@link #surroundedCell}, valid when {@link #surrounded}. */
	public PassTier surroundedTier;
	/** Some counted block is dense with denseResetsFall. */
	public boolean resetFall;
	/** Some counted block's tier has projectiles = true (phase 2 reads it). */
	public boolean projectiles;

	/** Counted cells this tick: packed positions and their per-block rule. */
	private long[] cellPos = new long[8];
	private PassRule.Cell[] cellRule = new PassRule.Cell[8];
	private int cells;

	public void reset() {
		walk = 1.0;
		fall = 1.0;
		walkTier = fallTier = null;
		walkDense = fallDense = false;
		walkNeighbours = fallNeighbours = 0;
		surrounded = false;
		surroundedCell = 0L;
		surroundedTier = null;
		resetFall = false;
		projectiles = false;
		Arrays.fill(cellRule, 0, cells, null);
		cells = 0;
	}

	/** Adds one counted block. {@code walkCounts}: false for a rope while climbing or a vine while standing still. */
	public void add(long pos, PassRule.Cell c, boolean walkCounts, boolean tierProjectiles) {
		if (cells == cellPos.length) {
			cellPos = Arrays.copyOf(cellPos, cells * 2);
			cellRule = Arrays.copyOf(cellRule, cells * 2);
		}
		cellPos[cells] = pos;
		cellRule[cells] = c;
		cells++;
		if (walkCounts && c.walk() < walk) {
			walk = c.walk();
			walkTier = c.tier();
			walkDense = c.walkDense();
			walkNeighbours = c.neighbours();
		} else if (walkCounts && walkTier == null) {
			walkTier = c.tier();
			walkNeighbours = c.neighbours();
		}
		if (c.fall() < fall) {
			fall = c.fall();
			fallTier = c.tier();
			fallDense = c.fallDense();
			fallNeighbours = c.neighbours();
		} else if (fallTier == null) {
			fallTier = c.tier();
			fallNeighbours = c.neighbours();
		}
		if (c.surrounded() && !surrounded) {
			surrounded = true;
			surroundedCell = pos;
			surroundedTier = c.tier();
		}
		resetFall |= c.resetFall();
		projectiles |= tierProjectiles;
	}

	/** True when the entity is in at least one counted block. */
	public boolean any() {
		return cells > 0;
	}

	public int cells() {
		return cells;
	}

	public long cellPos(int i) {
		return cellPos[i];
	}

	public PassRule.Cell cellRule(int i) {
		return cellRule[i];
	}

	/** True when {@code pos} is one of this tick's counted cells. */
	public boolean hasCell(long pos) {
		for (int i = 0; i < cells; i++) {
			if (cellPos[i] == pos) return true;
		}
		return false;
	}
}
