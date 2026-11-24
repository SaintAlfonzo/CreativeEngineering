package com.SaintAlfonzo.creativecompat.surface;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks that slow you while you're inside them and are in no pass-through tier ({@link PassThrough}, which shows its
 * own lines): their {@code entityInside} calls {@code Entity#makeStuckInBlock}. That multiplies the move and also
 * zeroes your momentum every tick, so the speed you really get is {@code multiplier * (1 - 0.91 * friction)} of normal
 * ({@link #walkRatio}), about 0.45x the multiplier on a normal floor.
 *
 * <p>Only untiered blocks are listed (cobwebs, barbed wire, berry bushes, propelplant, leaf piles and bamboo leaves
 * are tiered). Listed: powder snow and bamboo spikes (out of scope), and
 * the plants left outside the system (plan_passthrough.md seventh pass; phase 0 brief 1.2): Floral Enchantment vine
 * bushes, Farmer's Respite wither roots, Neapolitan beanstalk thorns, Upgrade Aquatic pickerelweed; other mods add
 * theirs with {@link #register}. Keyed by block class
 * (superclasses count). Multipliers were read from each class's {@code entityInside} bytecode; only the horizontal
 * part is kept and per-entity conditions are not modelled. Display only: nothing here changes movement.
 */
public final class StuckInBlock {

	private StuckInBlock() {}

	/** Horizontal multiplier for this state, or {@link Double#NaN} when it doesn't slow you. */
	interface Rule {
		double horizontal(BlockState state, @Nullable Player player, boolean standingOnIt);
	}

	private static final double NONE = Double.NaN;

	/** Class name -> rule. Vanilla by class, modded by name (not on the compile classpath). */
	private static final Map<String, Rule> RULES = new ConcurrentHashMap<>();

	static {
		// only while you're in it; standing on top (leather boots) is the snow tier instead
		RULES.put(PowderSnowBlock.class.getName(), (s, p, on) -> on ? NONE : 0.9);
		RULES.put("net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock",
				(s, p, on) -> p != null && p.isCreative() ? NONE : 0.95);
		// untiered plants with their own makeStuckInBlock (phase 0 brief 1.2)
		RULES.put("com.cozary.floralench.blocks.base.VineBushBlock", (s, p, on) -> 0.9);
		RULES.put("umpaz.farmersrespite.common.block.WitherRootsBlock", (s, p, on) -> 0.8);
		RULES.put("com.teamabnormals.neapolitan.common.block.BeanstalkThornsBlock", (s, p, on) -> 0.95);
		RULES.put("com.teamabnormals.upgrade_aquatic.common.block.PickerelweedBlock", (s, p, on) -> 0.6);
		RULES.put("com.teamabnormals.upgrade_aquatic.common.block.PickerelweedPlantBlock", (s, p, on) -> 0.75);
		RULES.put("com.teamabnormals.upgrade_aquatic.common.block.PickerelweedDoublePlantBlock", (s, p, on) -> 0.75);
		// other blocks that slow you from inside, display only (owner)
		RULES.put("com.sammy.minersdelight.content.block.sticky_basket.StickyBasketBlock", (s, p, on) -> 0.25);
		RULES.put("com.teamabnormals.upgrade_aquatic.common.block.MulberryJamBlock", (s, p, on) -> 0.2);
		RULES.put("com.teamabnormals.upgrade_aquatic.common.block.GuardianSpineBlock", (s, p, on) -> 0.25);
	}

	/**
	 * Registers a block class (by its binary name; subclasses count) whose {@code entityInside} slows you by a constant
	 * horizontal multiplier, for the display. Call it from the mod's constructor or setup.
	 */
	public static void register(String className, double horizontal) {
		RULES.put(className, (s, p, on) -> horizontal);
		RULE_OF.clear();
	}

	/** Block -> its rule (empty = none), resolved once per block. */
	private static final Map<Block, Optional<Rule>> RULE_OF = new ConcurrentHashMap<>();

	/** Horizontal stuck multiplier for this state, or NaN when the block doesn't slow you. */
	static double horizontal(BlockState state, @Nullable Player player, boolean standingOnIt) {
		return RULE_OF.computeIfAbsent(state.getBlock(), StuckInBlock::findRule)
				.map(r -> r.horizontal(state, player, standingOnIt))
				.orElse(NONE);
	}

	/**
	 * Walk speed through the block relative to normal, for a stuck multiplier over a floor with this
	 * friction. Normal steady speed is {@code a / (1 - 0.91f)}; stuck, each tick is just {@code a * m}.
	 */
	static double walkRatio(double multiplier, float friction) {
		return multiplier * (1.0 - 0.91 * friction);
	}

	private static Optional<Rule> findRule(Block block) {
		for (Class<?> c = block.getClass(); c != null && c != Block.class; c = c.getSuperclass()) {
			Rule r = RULES.get(c.getName());
			if (r != null) {
				return Optional.of(r);
			}
		}
		return Optional.empty();
	}
}
