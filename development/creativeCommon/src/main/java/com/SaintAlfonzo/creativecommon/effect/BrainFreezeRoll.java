// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.effect;

import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * One player's brain freeze count (TODO sweets #9, feature_writeup/smoothies.md section 3, owner). Plain Java,
 * no game classes, so the roll window runs in a headless test. <ul>
 *   <li>The first cold food after a gap longer than the window only starts the count: no roll.</li>
 *   <li>Every cold food eaten within the window of the one before rolls; the n-th roll in a row hits at
 *       {@code n x step} (capped at 1).</li>
 *   <li>A hit lasts {@code lengths[n - 1]} seconds (the last entry for every later roll) and resets the count; the
 *       food that hit still opens a new window.</li>
 * </ul>
 */
public final class BrainFreezeRoll {

	private boolean started;
	private long lastTick;
	private int rolls;

	/** Rolls made since the count last started or reset. */
	public int rolls() {
		return rolls;
	}

	/**
	 * A cold food eaten at {@code now} (game ticks). Returns the brain freeze length in seconds, or 0 when there was
	 * no roll or the roll missed.
	 */
	public int eat(long now, long windowTicks, double step, List<? extends Integer> lengths, DoubleSupplier random) {
		boolean inWindow = started && now - lastTick <= windowTicks && now >= lastTick;
		started = true;
		lastTick = now;
		if (!inWindow) {
			rolls = 0;
			return 0;
		}
		rolls++;
		double chance = Math.min(1.0, step * rolls);
		if (random.getAsDouble() >= chance) return 0;
		int seconds = length(lengths, rolls);
		rolls = 0;
		return seconds;
	}

	/** The n-th entry of lengths (1-based), the last one past the end; 0 for an empty list. */
	public static int length(List<? extends Integer> lengths, int n) {
		if (lengths == null || lengths.isEmpty() || n < 1) return 0;
		Integer v = lengths.get(Math.min(n, lengths.size()) - 1);
		return v == null ? 0 : Math.max(0, v);
	}
}
