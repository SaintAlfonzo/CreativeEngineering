package com.SaintAlfonzo.creativecompat.surface;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Every layer block falls when the block below is gone and stacks where it lands (plan_post_scratch_1115.md G7.5,
 * E4 rules). Covers the layer blocks without gravity of their own, tag {@code creativecompat:falling_layers}: vanilla
 * snow, The Salt's / Vintage Delight's salt layers ({@code vintagedelight:salt}), Twigs bamboo leaves and the full
 * bamboo leaves ({@link FullBambooLeavesBlock}), Immersive Engineering sawdust and Supplementaries fodder. Immersive
 * Weathering's sand, red sand, ash layers and leaf piles, and Supplementaries ash, are {@code FallingBlock}s and stack
 * through their own falling entity; they are left alone.
 *
 * <p>Trigger: the blocks' {@code updateShape} (mixins on each class) sees a free block below
 * ({@link FallingBlock#isFree}: air, fire, fluid or a replaceable block such as a thinner snow layer) and, instead of
 * breaking or floating, keeps the state and queues the position. The queue is drained at the end of the level tick
 * ({@link #onLevelTick}), not with a scheduled tick: Creative Common's melting keeps scheduled ticks on snow, and
 * vanilla keeps only one per position and block. Snow and salt also check on their random tick, so a floating layer
 * in a chunk loaded from an old save falls by itself; the others fall on their first neighbor update. A piston that
 * moves a layer updates its shape on arrival ({@code Block.updateFromNeighbourShapes}), so a pushed layer over a gap
 * falls too.
 *
 * <p>Landing ({@link FallingLayerEntity}): onto the same layer family the layers add up, capped at the block's
 * most, the rest lands on top; a vanilla snow stack reaching 8 becomes a snow block ({@link SnowLayers#fullToBlock});
 * Twigs bamboo leaves above 4 become the full bamboo leaves. Where it cannot land (a fluid, a taken spot, no support)
 * the layers drop as items through the block's loot table, never deleted. Switch {@code [surface.passthrough.layers]
 * layersFall}.
 */
public final class FallingLayers {

	private FallingLayers() {}

	public static final TagKey<Block> TAG = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "falling_layers"));

	/** Twigs bamboo leaves (1 to 4 layers); null until looked up or without Twigs. */
	private static final ResourceLocation TWIGS_BAMBOO_LEAVES = new ResourceLocation("twigs", "bamboo_leaves");
	/** Most queued positions per level drained in one tick; the rest wait for the next tick. */
	private static final int PER_TICK = 512;

	private static final Map<ServerLevel, LongLinkedOpenHashSet> QUEUE = new IdentityHashMap<>();

	/** Snow! Real Magic's mod id: with it loaded, its {@code snowGravity} falls snow (snow_real_magic.md R3). */
	private static final String SRM = "snowrealmagic";
	private static Boolean srmLoaded;

	/** True when Snow! Real Magic is loaded; logs the hand-over once (first call: the mod constructor). */
	public static boolean srmLoaded() {
		if (srmLoaded == null) {
			srmLoaded = ModList.get() != null && ModList.get().isLoaded(SRM);
			if (srmLoaded) CreativeCompat.LOGGER.info("[creativecompat] Snow! Real Magic loaded: snow falls by its "
					+ "snowGravity, not [surface.passthrough.layers] layersFall (salt and other layers keep it)");
		}
		return srmLoaded;
	}

	/**
	 * True when this block falls by these rules: tagged, and not snow while Snow! Real Magic is loaded (its own fall
	 * handles {@code minecraft:snow} and its {@code snowrealmagic} blocks; salt layers, which are also a
	 * {@code SnowLayerBlock}, stay here).
	 */
	public static boolean falls(BlockState state) {
		if (!state.is(TAG)) return false;
		if (!srmLoaded()) return true;
		Block b = state.getBlock();
		if (b == Blocks.SNOW) return false;
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(b);
		return id == null || !SRM.equals(id.getNamespace());
	}

	/** {@code [surface.passthrough.layers] layersFall}, true before the config loads. */
	public static boolean enabled() {
		return !CompatConfig.SPEC.isLoaded() || CompatConfig.SURFACE.passthrough.layersFall.get();
	}

	/**
	 * HEAD of a falling layer block's {@code updateShape}: true when it should keep its state now and fall at the end of
	 * the tick (the caller returns {@code state}). Only on a server level, for a tagged block over a free block.
	 */
	public static boolean holdAndQueue(BlockState state, LevelAccessor level, BlockPos pos) {
		if (!(level instanceof ServerLevel sl)) return false;
		if (!falls(state) || !enabled()) return false;
		if (pos.getY() <= sl.getMinBuildHeight()) return false;
		if (!FallingBlock.isFree(sl.getBlockState(pos.below()))) return false;
		queue(sl, pos);
		return true;
	}

	/** Random tick of a snow or salt layer: falls now when it floats. True = fell (skip the rest of the tick). */
	public static boolean randomTick(BlockState state, ServerLevel level, BlockPos pos) {
		if (!falls(state) || !enabled()) return false;
		return tryFall(level, pos, state);
	}

	public static void queue(ServerLevel level, BlockPos pos) {
		QUEUE.computeIfAbsent(level, l -> new LongLinkedOpenHashSet()).add(pos.asLong());
	}

	/** End of each server level tick: the queued layers that still float start falling. */
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
		LongLinkedOpenHashSet q = QUEUE.get(level);
		if (q == null || q.isEmpty()) return;
		List<BlockPos> now = new ArrayList<>(Math.min(q.size(), PER_TICK));
		while (!q.isEmpty() && now.size() < PER_TICK) now.add(BlockPos.of(q.removeFirstLong()));
		for (BlockPos pos : now) {
			if (!level.isLoaded(pos)) continue;
			BlockState state = level.getBlockState(pos);
			if (falls(state) && enabled()) tryFall(level, pos, state);
		}
	}

	public static void onLevelUnload(LevelEvent.Unload event) {
		if (event.getLevel() instanceof ServerLevel level) QUEUE.remove(level);
	}

	/** Starts the layer at {@code pos} falling when the block below is free. */
	public static boolean tryFall(ServerLevel level, BlockPos pos, BlockState state) {
		if (pos.getY() <= level.getMinBuildHeight()) return false;
		if (!FallingBlock.isFree(level.getBlockState(pos.below()))) return false;
		FallingLayerEntity.fall(level, pos, state);
		return true;
	}

	// ---- layer arithmetic ----

	/** The block's integer {@code layers} property, or null. */
	@Nullable
	public static IntegerProperty layersProperty(BlockState state) {
		for (Property<?> p : state.getProperties()) {
			if (p instanceof IntegerProperty ip && p.getName().equals("layers")) return ip;
		}
		return null;
	}

	public static boolean isTwigsLeaves(BlockState state) {
		Block b = ForgeRegistries.BLOCKS.getValue(TWIGS_BAMBOO_LEAVES);
		return b != null && b != Blocks.AIR && state.is(b);
	}

	@Nullable
	public static Block twigsLeaves() {
		Block b = ForgeRegistries.BLOCKS.getValue(TWIGS_BAMBOO_LEAVES);
		return b == null || b == Blocks.AIR ? null : b;
	}

	/** Bamboo leaves of either block (Twigs' 1 to 4, the full 5 to 8). */
	public static boolean isBambooLeaves(BlockState state) {
		return state.getBlock() instanceof FullBambooLeavesBlock || isTwigsLeaves(state);
	}

	/** Layers of a layer block in the bamboo family's shared count (Twigs 1 to 4, full 5 to 8), or its own count. */
	public static int layers(BlockState state) {
		IntegerProperty p = layersProperty(state);
		return p == null ? 1 : state.getValue(p);
	}

	/** Most layers a stack of this family holds. */
	public static int maxLayers(BlockState state) {
		if (isBambooLeaves(state)) return FullBambooLeavesBlock.MAX;
		IntegerProperty p = layersProperty(state);
		if (p == null) return 1;
		int max = 1;
		for (int v : p.getPossibleValues()) max = Math.max(max, v);
		return max;
	}

	/** Same stack family: same block, or bamboo leaves of either block. */
	public static boolean sameFamily(BlockState a, BlockState b) {
		if (a.getBlock() == b.getBlock()) return layersProperty(a) != null;
		return isBambooLeaves(a) && isBambooLeaves(b);
	}

	/**
	 * {@code like} with {@code n} layers ({@code 1 <= n <= max}): bamboo leaves move between Twigs' block and the full
	 * block; a vanilla snow stack of 8 becomes a snow block when that switch is on. Null when the family has no state
	 * for {@code n}.
	 */
	@Nullable
	public static BlockState withLayers(BlockState like, int n) {
		if (isBambooLeaves(like)) {
			boolean wet = like.hasProperty(BlockStateProperties.WATERLOGGED) && like.getValue(BlockStateProperties.WATERLOGGED);
			BlockState s;
			if (n > FullBambooLeavesBlock.TWIGS_MAX) {
				s = FullBambooLeavesBlock.state(n);
				if (s == null) return null;
			} else {
				Block twigs = twigsLeaves();
				if (twigs == null) return null;
				s = twigs.defaultBlockState();
				IntegerProperty p = layersProperty(s);
				if (p == null || !p.getPossibleValues().contains(n)) return null;
				s = s.setValue(p, n);
			}
			if (s.hasProperty(BlockStateProperties.WATERLOGGED)) s = s.setValue(BlockStateProperties.WATERLOGGED, wet);
			return s;
		}
		IntegerProperty p = layersProperty(like);
		if (p == null || !p.getPossibleValues().contains(n)) return null;
		return SnowLayers.fullToBlock(like.setValue(p, n), PassValues.local());
	}

	/**
	 * Lands {@code falling} at {@code pos} (server). True = placed (maybe partly, the rest on top or dropped); false =
	 * nothing placed, the caller drops it all.
	 */
	public static boolean land(ServerLevel level, BlockPos pos, BlockState falling) {
		BlockState on = level.getBlockState(pos);
		int n = layers(falling);
		if (sameFamily(on, falling)) {
			int max = maxLayers(on);
			int total = layers(on) + n;
			int target = Math.min(total, max);
			BlockState merged = withLayers(on, target);
			if (merged == null) return false;
			level.setBlock(pos, merged, Block.UPDATE_ALL);
			int rest = total - target;
			if (rest > 0) {
				BlockState top = withLayers(falling, Math.min(rest, maxLayers(falling)));
				BlockPos above = pos.above();
				if (top == null || !placeable(level, above, top)) {
					drop(level, above, top != null ? top : falling);
				} else {
					level.setBlock(above, top, Block.UPDATE_ALL);
				}
			}
			return true;
		}
		BlockState place = withLayers(falling, n);
		if (place == null) place = falling;
		if (!placeable(level, pos, place)) return false;
		FluidState fluid = level.getFluidState(pos);
		if (fluid.getType() == Fluids.WATER && fluid.isSource() && place.hasProperty(BlockStateProperties.WATERLOGGED)) {
			place = place.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(pos, place, Block.UPDATE_ALL);
		return true;
	}

	/**
	 * The layer can settle at {@code pos}: the spot is replaceable, holds no fluid (unless the block takes water), it
	 * survives there and the block below is not free.
	 */
	static boolean placeable(ServerLevel level, BlockPos pos, BlockState state) {
		BlockState there = level.getBlockState(pos);
		ItemStack as = new ItemStack(state.getBlock().asItem());
		if (!there.isAir() && !there.canBeReplaced(new DirectionalPlaceContext(level, pos, Direction.DOWN, as, Direction.UP))) {
			return false;
		}
		FluidState fluid = level.getFluidState(pos);
		if (!fluid.isEmpty() && !(fluid.getType() == Fluids.WATER && fluid.isSource()
				&& state.hasProperty(BlockStateProperties.WATERLOGGED))) {
			return false;
		}
		if (FallingBlock.isFree(level.getBlockState(pos.below()))) return false;
		return state.canSurvive(level, pos);
	}

	/**
	 * Drops the layers as items, with the break effect: snow as one snowball per layer (its loot table needs a
	 * breaker), the others through their loot table, or one of the block's item per layer when that gives nothing.
	 */
	public static void drop(Level level, BlockPos pos, BlockState state) {
		if (!(level instanceof ServerLevel sl)) return;
		int n = Math.max(1, layers(state));
		List<ItemStack> stacks;
		if (state.is(Blocks.SNOW)) {
			stacks = List.of(new ItemStack(Items.SNOWBALL, n));
		} else {
			stacks = Block.getDrops(state, sl, pos, null);
			if (stacks.isEmpty() && state.getBlock().asItem() != Items.AIR) {
				stacks = List.of(new ItemStack(state.getBlock().asItem(), n));
			}
		}
		for (ItemStack s : stacks) Block.popResource(sl, pos, s);
		level.levelEvent(2001, pos, Block.getId(state));
	}
}
