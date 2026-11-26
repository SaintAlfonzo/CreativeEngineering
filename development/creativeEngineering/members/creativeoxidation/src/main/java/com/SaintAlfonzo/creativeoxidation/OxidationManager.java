// SPDX-License-Identifier: GPL-3.0-only
// Port of Create: Oxidized 1.0.0 (Mr.Mulle, MIT; NOTICE-upstream-MIT.txt).
package com.SaintAlfonzo.creativeoxidation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Aging, waxing, scraping and lightning cleaning on block states that carry the properties. Random ticks call
 * {@link #randomTick} (BlockStateBaseMixin); the interaction and lightning hooks that call {@link #wax}, {@link #scrape}
 * and {@link #lightningStrike} come in P4 (plan_creative_oxidation.md).
 */
public final class OxidationManager {
	/** Block update + client update + is-moving: the block entity stays, Create's onRemove logic does not run. */
	private static final int IN_PLACE_UPDATE = 67;

	private OxidationManager() {
	}

	/** One random tick on an aging state; true when it stepped to the next stage. */
	public static boolean randomTick(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
		if (!OxidationConfig.enabled() || !OxidationProperties.isAging(state)) return false;
		if (random.nextDouble() >= OxidationRules.attemptChance(OxidationConfig.speedFactor())) return false;

		OxidationStage current = state.getValue(OxidationProperties.STAGE);
		int sameAge = 0;
		int older = 0;
		for (BlockPos nearby : BlockPos.withinManhattan(pos, 4, 4, 4)) {
			if (nearby.distManhattan(pos) > 4) break;
			if (nearby.equals(pos)) continue;
			BlockState other = level.getBlockState(nearby);
			int otherAge;
			if (OxidationProperties.isOxidizable(other)) {
				if (other.getValue(OxidationProperties.WAXED)) continue;
				otherAge = other.getValue(OxidationProperties.STAGE).ordinal();
			} else if (other.getBlock() instanceof WeatheringCopper copper) {
				otherAge = copper.getAge().ordinal();
			} else {
				continue;
			}
			if (otherAge < current.ordinal()) return false;
			if (otherAge == current.ordinal()) sameAge++;
			else older++;
		}
		if (random.nextFloat() < OxidationRules.stepChance(current, sameAge, older)) {
			setStage(level, pos, state, current.next());
			return true;
		}
		return false;
	}

	public static boolean wax(Level level, BlockPos pos, BlockState state) {
		return wax(level, pos, state, true);
	}

	public static boolean wax(Level level, BlockPos pos, BlockState state, boolean playEffect) {
		if (!OxidationConfig.enabled() || !OxidationProperties.isOxidizable(state) || state.getValue(OxidationProperties.WAXED)) {
			return false;
		}
		setState(level, pos, state.setValue(OxidationProperties.WAXED, true));
		if (playEffect) level.levelEvent(3003, pos, 0);
		return true;
	}

	/** One stage forward (spout, fan, sprays); false when waxed or already oxidized. */
	public static boolean oxidize(Level level, BlockPos pos, BlockState state) {
		if (!OxidationConfig.enabled() || !OxidationProperties.isOxidizable(state) || state.getValue(OxidationProperties.WAXED)) {
			return false;
		}
		OxidationStage stage = state.getValue(OxidationProperties.STAGE);
		if (stage == OxidationStage.OXIDIZED) return false;
		setStage(level, pos, state, stage.next());
		return true;
	}

	/** Axe or steel wool: wax off first, else one stage back. */
	public static ScrapeResult scrape(Level level, BlockPos pos, BlockState state) {
		if (!OxidationConfig.enabled() || !OxidationProperties.isOxidizable(state)) return ScrapeResult.NONE;
		if (state.getValue(OxidationProperties.WAXED)) {
			setState(level, pos, state.setValue(OxidationProperties.WAXED, false));
			level.levelEvent(3004, pos, 0);
			level.playSound(null, pos, scrapeSound(ScrapeResult.WAX_REMOVED), SoundSource.BLOCKS, 1.0F, 1.0F);
			return ScrapeResult.WAX_REMOVED;
		}
		OxidationStage stage = state.getValue(OxidationProperties.STAGE);
		if (stage == OxidationStage.UNAFFECTED) return ScrapeResult.NONE;
		setStage(level, pos, state, stage.previous());
		level.levelEvent(3005, pos, 0);
		level.playSound(null, pos, scrapeSound(ScrapeResult.SCRAPED), SoundSource.BLOCKS, 1.0F, 1.0F);
		return ScrapeResult.SCRAPED;
	}

	public static SoundEvent scrapeSound(ScrapeResult result) {
		return result == ScrapeResult.WAX_REMOVED ? SoundEvents.AXE_WAX_OFF : SoundEvents.AXE_SCRAPE;
	}

	/**
	 * After vanilla's LightningRodBlock.clearCopperOnLightningStrike: the block under the rod (or the struck block) goes
	 * back to unaffected, then 3 to 5 random walks of 1 to 8 steps clean one stage per step.
	 */
	public static void lightningStrike(Level level, BlockPos strikePos) {
		if (level.isClientSide || !OxidationConfig.enabled()) return;
		BlockState strikeState = level.getBlockState(strikePos);
		BlockPos first = strikeState.getBlock() instanceof LightningRodBlock
				? strikePos.relative(strikeState.getValue(LightningRodBlock.FACING).getOpposite())
				: strikePos;
		boolean customAnchor = fullyDeoxidize(level, first);
		boolean vanillaAnchor = level.getBlockState(first).getBlock() instanceof WeatheringCopper;
		if (!customAnchor && !vanillaAnchor) return;
		RandomSource random = level.random;
		int walks = random.nextInt(3) + 3;
		for (int walk = 0; walk < walks; walk++) {
			BlockPos.MutableBlockPos cursor = first.mutable();
			int steps = random.nextInt(8) + 1;
			for (int step = 0; step < steps; step++) {
				BlockPos next = randomStepCleaningCopper(level, cursor);
				if (next == null) break;
				cursor.set(next);
			}
		}
	}

	private static boolean fullyDeoxidize(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!OxidationProperties.isOxidizable(state) || state.getValue(OxidationProperties.WAXED)) return false;
		setState(level, pos, state.setValue(OxidationProperties.STAGE, OxidationStage.UNAFFECTED));
		return true;
	}

	private static BlockPos randomStepCleaningCopper(Level level, BlockPos pos) {
		for (BlockPos candidate : BlockPos.randomInCube(level.random, 10, pos, 1)) {
			BlockState state = level.getBlockState(candidate);
			if (OxidationProperties.isOxidizable(state) && !state.getValue(OxidationProperties.WAXED)) {
				OxidationStage stage = state.getValue(OxidationProperties.STAGE);
				if (stage != OxidationStage.UNAFFECTED) setStage(level, candidate, state, stage.previous());
				level.levelEvent(3002, candidate, -1);
				return candidate.immutable();
			}
		}
		return null;
	}

	private static void setStage(Level level, BlockPos pos, BlockState state, OxidationStage stage) {
		setState(level, pos, state.setValue(OxidationProperties.STAGE, stage));
	}

	private static void setState(Level level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, IN_PLACE_UPDATE);
	}

	public enum ScrapeResult {
		NONE,
		WAX_REMOVED,
		SCRAPED
	}
}
