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

	/** Hand tools on a staged block (P4): honeycomb waxes, axe and steel wool scrape (R6). */
	public enum Tool {
		HONEYCOMB,
		SCRAPER
	}

	/** True when the tool changes this state: honeycomb on an unwaxed one; a scraper on a waxed or aged one. */
	public static boolean toolApplies(Tool tool, boolean waxed, OxidationStage stage) {
		if (tool == Tool.HONEYCOMB) return !waxed;
		return waxed || stage != OxidationStage.UNAFFECTED;
	}

	/** Supplementaries soap (R9): one use takes wax and every stage off, so anything but plain washes. */
	public static boolean soapWashes(boolean waxed, OxidationStage stage) {
		return waxed || stage != OxidationStage.UNAFFECTED;
	}

	/** Lightning (P4): an unwaxed aged block goes back to unaffected; waxed blocks keep their stage. */
	public static boolean lightningCleans(boolean waxed, OxidationStage stage) {
		return !waxed && stage != OxidationStage.UNAFFECTED;
	}

	/** What a JEI world row shows (P8): honeycomb, scraper (axe and steel wool), lightning or soap. */
	public enum Change {
		HONEYCOMB,
		SCRAPE,
		LIGHTNING,
		SOAP
	}

	/** One state change of a staged block, for the JEI world rows (P8). */
	public record Transition(Change change, OxidationStage fromStage, boolean fromWaxed, OxidationStage toStage,
							 boolean toWaxed) {
	}

	/**
	 * Every state change the hand tools, lightning and soap make, from the same decisions the events and
	 * OxidationManager use: honeycomb waxes at the same stage; a scraper takes wax off first, else one stage back;
	 * lightning sends an unwaxed aged block to unaffected (the struck block; the random walks clean one stage per
	 * step); soap takes wax and every stage off. Order: by change, then from-state (unaffected to oxidized, unwaxed
	 * before waxed).
	 */
	public static java.util.List<Transition> transitions() {
		java.util.List<Transition> out = new java.util.ArrayList<>();
		for (Change change : Change.values()) {
			for (boolean waxed : new boolean[]{false, true}) {
				for (OxidationStage stage : OxidationStage.values()) {
					Transition t = switch (change) {
						case HONEYCOMB -> toolApplies(Tool.HONEYCOMB, waxed, stage) ? new Transition(change, stage, waxed, stage, true) : null;
						case SCRAPE -> !toolApplies(Tool.SCRAPER, waxed, stage) ? null
								: waxed ? new Transition(change, stage, true, stage, false)
								: new Transition(change, stage, false, stage.previous(), false);
						case LIGHTNING -> lightningCleans(waxed, stage) ? new Transition(change, stage, false, OxidationStage.UNAFFECTED, false) : null;
						case SOAP -> soapWashes(waxed, stage) ? new Transition(change, stage, waxed, OxidationStage.UNAFFECTED, false) : null;
					};
					if (t != null) out.add(t);
				}
			}
		}
		return out;
	}

	/**
	 * Breaking a staged or waxed block (P3, R8): the drops keep the stage when {@code keepStageOnBreak} is on and the
	 * state is not plain; only drops that are a target's own item ({@link OxidationTargets#isTargetItem}) get it.
	 */
	public static boolean dropKeepsStage(boolean keepStageOnBreak, boolean waxed, OxidationStage stage) {
		return keepStageOnBreak && (waxed || stage != OxidationStage.UNAFFECTED);
	}
}
