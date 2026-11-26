// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

/**
 * The aging decisions, free of block state access so tools/test_creative_oxidation.sh can check them.
 */
public final class OxidationRules {
	/** Vanilla copper's per-random-tick attempt chance is 1 - 0.94311111 (about 0.0569). */
	private static final double VANILLA_KEEP = 0.94311111;

	private OxidationRules() {
	}

	/** A state that ages on random ticks: a target in the oxidizable tag, not waxed, not yet oxidized. */
	public static boolean isAging(boolean oxidizable, boolean waxed, OxidationStage stage) {
		return oxidizable && !waxed && stage != OxidationStage.OXIDIZED;
	}

	/**
	 * B2 fix: what isRandomlyTicking answers. TRUE for an aging state; null means "ask the block", so a block that random
	 * ticks on its own keeps doing so (upstream forced false on every state carrying the properties).
	 */
	public static Boolean forceRandomTicking(boolean aging) {
		return aging ? Boolean.TRUE : null;
	}

	/**
	 * Whether the block's own randomTick runs after the aging step: only when the block random ticks by itself (a block
	 * that ticks only because it ages would otherwise run its scheduled tick() through the default randomTick), and not
	 * when aging just replaced the state (the block would act on the stale one).
	 */
	public static boolean runOwnRandomTick(boolean blockTicksItself, boolean agedThisTick) {
		return blockTicksItself && !agedThisTick;
	}

	/** Chance per random tick to try a step, scaled by speedFactor (0 stops aging, 1 is vanilla). */
	public static double attemptChance(double speedFactor) {
		if (!(speedFactor > 0.0)) return 0.0;
		return 1.0 - Math.pow(VANILLA_KEEP, speedFactor);
	}

	/**
	 * Vanilla's neighbor rule for the step itself: {@code sameAge} and {@code older} count aging neighbors within 4
	 * blocks (Manhattan); a younger neighbor stops the step before this is asked. Unaffected blocks are 0.75 as likely.
	 */
	public static float stepChance(OxidationStage current, int sameAge, int older) {
		float ratio = (float) (older + 1) / (float) (older + sameAge + 1);
		float ageModifier = current == OxidationStage.UNAFFECTED ? 0.75F : 1.0F;
		return ageModifier * ratio * ratio;
	}
}
