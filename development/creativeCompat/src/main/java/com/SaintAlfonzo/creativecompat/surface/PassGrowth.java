package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Growth and layer rules of a pass-through block (plan 1.1; growth table in briefs/passthrough_phase0.md 2.2).
 *
 * <ul>
 *   <li>Growth property: an integer {@code age} (most crops), else an ordered {@code growth} enum whose values not
 *       starting with {@code top} are the lower block's stages (IE hemp: bottom0..bottom4, top0). A block with neither
 *       counts as grown.</li>
 *   <li>Upper block of a tall crop: {@code upper=true} (Corn Delight), {@code half=upper} (double plants, coffee), a
 *       {@code top*} growth value (hemp), or a different block of the same tier right below (rice panicles on rice).
 *       Under {@code tall_half} an upper block looks down one block and uses that block's growth.</li>
 *   <li>Layers: an integer {@code layers} property (snow 1-8, IW leaf piles 0-8, Twigs bamboo leaves 1-4, IE sawdust
 *       1-9) and its highest value, for the layer height rule ({@link PassRule#layerWalk}).</li>
 * </ul>
 * What a block has is read once per block and cached ({@link #clear} on tag reload, with the tier cache).
 */
final class PassGrowth {

	private PassGrowth() {}

	/** The growth-related properties of one block. */
	private record Info(IntegerProperty age, int ageMin, int ageMax, Property<?> growth, List<String> lowerStages,
						BooleanProperty upper, Property<?> half, IntegerProperty layers, int layersMax) {

		boolean isUpper(BlockState state) {
			if (upper != null && state.getValue(upper)) return true;
			if (half != null && "upper".equals(valueName(state, half))) return true;
			return growth != null && valueName(state, growth).startsWith("top");
		}

		boolean halfGrown(BlockState state) {
			if (age != null) {
				return PassMath.halfGrown(state.getValue(age), ageMin, ageMax);
			}
			if (growth != null) {
				int i = lowerStages.indexOf(valueName(state, growth));
				return i < 0 || PassMath.enumHalfGrown(i, lowerStages.size()); // a top stage: grown
			}
			return true;
		}
	}

	private static final Map<Block, Info> INFO = new ConcurrentHashMap<>();

	static void clear() {
		INFO.clear();
	}

	private static Info info(Block block) {
		return INFO.computeIfAbsent(block, PassGrowth::read);
	}

	private static Info read(Block block) {
		IntegerProperty age = null, layers = null;
		Property<?> growth = null, half = null;
		BooleanProperty upper = null;
		for (Property<?> p : block.getStateDefinition().getProperties()) {
			String name = p.getName();
			if (p instanceof IntegerProperty ip) {
				if (name.equals("age")) age = ip;
				else if (name.equals("layers")) layers = ip;
			} else if (p instanceof BooleanProperty bp && name.equals("upper")) {
				upper = bp;
			} else if (name.equals("half") && names(p).contains("upper")) {
				half = p;
			} else if (name.equals("growth") && !(p instanceof BooleanProperty)) {
				growth = p;
			}
		}
		int min = 0, max = 0;
		if (age != null) {
			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
			for (int v : age.getPossibleValues()) {
				min = Math.min(min, v);
				max = Math.max(max, v);
			}
		}
		List<String> lower = new ArrayList<>();
		if (growth != null && age == null) {
			for (String n : names(growth)) {
				if (!n.startsWith("top")) lower.add(n);
			}
		} else {
			growth = null;
		}
		int lmax = -1;
		if (layers != null) {
			for (int v : layers.getPossibleValues()) lmax = Math.max(lmax, v);
		}
		return new Info(age, min, max, growth, List.copyOf(lower), upper, half, layers, lmax);
	}

	/** Serialized names of a property's values, in its own order (an enum's declaration order). */
	private static <T extends Comparable<T>> List<String> names(Property<T> p) {
		List<String> out = new ArrayList<>();
		for (T v : p.getPossibleValues()) out.add(p.getName(v));
		return out;
	}

	private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> p) {
		return p.getName(state.getValue(p));
	}

	/**
	 * The block passes its tier's growth rule. {@code tierOfBelow} is the tier of the block right below (null = none):
	 * with {@code tall_half}, an upper block (or a different same-tier block on top of another) defers to it.
	 */
	static boolean passes(PassTier.Growth mode, BlockGetter level, BlockPos pos, BlockState state, PassTier tier) {
		if (mode == PassTier.Growth.ANY) return true;
		Info info = info(state.getBlock());
		if (mode == PassTier.Growth.TALL_HALF) {
			BlockPos belowPos = pos.below();
			BlockState below = level.getBlockState(belowPos);
			boolean belowSameTier = PassThrough.tierOf(below) == tier;
			boolean upper = info.isUpper(state) || (belowSameTier && below.getBlock() != state.getBlock());
			if (upper) {
				// an upper block without its lower one (a placed top) counts as grown
				return !belowSameTier || info(below.getBlock()).halfGrown(below);
			}
		}
		return info.halfGrown(state);
	}

	/** The block's {@code layers} value, or -1 when it has none. */
	static int layers(BlockState state) {
		IntegerProperty p = info(state.getBlock()).layers();
		return p == null ? -1 : state.getValue(p);
	}

	/**
	 * The highest value of the block's {@code layers} property, or -1 when it has none. Twigs bamboo leaves (1 to 4)
	 * count on the 8-layer scale of {@code creativecompat:bamboo_leaves_full} (5 to 8), so slowdown, overlay and the
	 * layer note rise steadily from 1 to 8 across both blocks.
	 */
	static int layersMax(BlockState state) {
		if (FallingLayers.isTwigsLeaves(state)) return FullBambooLeavesBlock.MAX;
		return info(state.getBlock()).layersMax();
	}
}
