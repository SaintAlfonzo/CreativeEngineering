package com.SaintAlfonzo.creativecompat.surface;

/**
 * Jumping and falling inside walk-through layers (plan_post_scratch_1115.md G4c, G7.13), and the leaf thorn numbers
 * (G7.13, G7.14). Synced with the pass-through values ({@link PassValues#motion}): the client moves the local player.
 *
 * <ul>
 *   <li>Snow layers: from {@code snowJumpLayers} layers up the jump power is multiplied by {@code snowJumpFactor}
 *       (0.67 gives powder snow's hop of about 0.63 blocks); falling inside any snow layer is capped at
 *       {@code snowFallSpeed} blocks per tick (powder snow's 0.0784 x 1.5).</li>
 *   <li>Leaf layers: from {@code leafJumpLayers} up the jump power is multiplied by {@code leafJumpFactor} (0.9 still
 *       clears a 1-block step from inside the pile, about 1.05 blocks); falling inside is capped at
 *       {@code leafFallSpeed}.</li>
 * </ul>
 * Plain Java (no game classes) so the headless harness tests it.
 */
public record LayerMotion(int snowJumpLayers, double snowJumpFactor, double snowFallSpeed,
						  int leafJumpLayers, double leafJumpFactor, double leafFallSpeed) {

	/** Powder snow's fall per tick inside: gravity after drag (0.08 x 0.98) times its vertical factor 1.5. */
	public static final double POWDER_SNOW_FALL = 0.0784 * 1.5;

	public static final LayerMotion DEFAULTS = new LayerMotion(6, 0.67, 0.12, 5, 0.9, 0.24);

	/** No cap: falling is not limited. */
	public static final double NO_CAP = Double.POSITIVE_INFINITY;

	/** Jump power multiplier inside a layer block of {@code layers} ({@code open} = leaf layers); 1.0 below the threshold. */
	public double jumpFactor(boolean open, int layers) {
		if (open) return layers >= leafJumpLayers ? leafJumpFactor : 1.0;
		return layers >= snowJumpLayers ? snowJumpFactor : 1.0;
	}

	/** Fastest fall in blocks per tick inside a layer block ({@code open} = leaf layers). */
	public double fallCap(boolean open) {
		double c = open ? leafFallSpeed : snowFallSpeed;
		return c > 0.0 ? c : NO_CAP;
	}

	/** Downward speed after the cap ({@code vy} negative = falling); rising or slower falls are unchanged. */
	public static double capFall(double vy, double cap) {
		return vy < -cap ? -cap : vy;
	}

	/**
	 * Thorn prick chance (G7.13): {@code base x (4 - armor pieces) / 4}, so full armor never pricks. Any item in an
	 * armor slot counts (an elytra in the chest slot too).
	 */
	public static double thornChance(double base, int armorPieces) {
		int free = 4 - Math.max(0, Math.min(4, armorPieces));
		return base * free / 4.0;
	}

	/**
	 * The player moves fast enough to be pricked (G7.14): the measured horizontal speed divided by the full-pile walk
	 * factor ({@code fullWalk}, the 8-layer value) is at least {@code minSpeed} x walking speed. A pile of 6 slows less
	 * than a full one, so it pricks more easily. A 10 percent margin covers acceleration and rounding.
	 */
	public static boolean thornSpeed(double blocksPerTick, double fullWalk, double minSpeed) {
		double w = fullWalk > 0.0 ? fullWalk : 1.0;
		return blocksPerTick / w >= minSpeed * WALK_SPEED * 0.9;
	}

	/** A player's walking speed on flat ground, blocks per tick (4.317 m/s). */
	public static final double WALK_SPEED = 0.21585;
}
