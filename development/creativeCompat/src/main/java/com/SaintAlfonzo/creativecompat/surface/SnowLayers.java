package com.SaintAlfonzo.creativecompat.surface;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Snow layers as walk-through blocks (plan_post_scratch_1115.md G7.4), {@code mixin/SnowLayerPassMixin}:
 * <ul>
 *   <li><b>Walk-through</b> ({@code [surface.passthrough.layers] snowWalkThrough}): a snow layer of 1 to 7 layers in
 *       the layer tier ({@code #creativecompat:passthrough_layers}) has no collision for a living entity, which walks
 *       through it at the layer tier's height slowdown ({@link PassThrough#layerWalk}). An entity that can walk on
 *       powder snow (leather boots, {@code #minecraft:powder_snow_walkable_mobs}) and is above the layer's top and not
 *       sneaking stands on it instead, on the full layer height (the outline), at BlockRunner's snow surface speed:
 *       the "on top" speed. Sneaking sinks in, like powder snow. Items, falling blocks, projectiles and any
 *       context without an entity (pathfinding, placement checks) keep vanilla's collision.</li>
 *   <li><b>Full layer to snow block</b> ({@code snowFullBecomesBlock}): vanilla snow that reaches 8 layers becomes a
 *       snow block, on placement ({@code getStateForPlacement}) and on a random tick for one already in the world.
 *       {@link #fullToBlock} is the shared seam for any other code that stacks snow (falling layers merging).</li>
 * </ul>
 * Both sides: the client moves the local player with the server's values ({@link PassValues#of(boolean)}).
 */
public final class SnowLayers {

	private SnowLayers() {}

	/** Vanilla snow's most layers; a full layer is not walk-through. */
	public static final int FULL = 8;

	/**
	 * The collision shape of a walk-through snow layer for {@code ctx}, or null to keep vanilla's ({@code vanilla} is
	 * not read; the mixin passes the state and leaves the return value alone on null).
	 */
	@Nullable
	public static VoxelShape collision(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		if (!(ctx instanceof EntityCollisionContext ec)) return null;
		Entity e = ec.getEntity();
		if (!(e instanceof LivingEntity)) return null;
		if (!walkThrough(state, PassValues.of(e.level.isClientSide))) return null;
		VoxelShape top = state.getShape(level, pos);
		if (standsOn(e) && ctx.isAbove(top, pos, false) && !ctx.isDescending()) return top;
		return Shapes.empty();
	}

	/**
	 * The state is a snow layer the walk-through rule covers: the system and {@code snowWalkThrough} on, a
	 * {@link SnowLayerBlock} in the layer tier (the leaf layers have no collision of their own), below full.
	 */
	public static boolean walkThrough(BlockState state, PassValues values) {
		if (!values.enabled || !values.snowWalkThrough) return false;
		if (!(state.getBlock() instanceof SnowLayerBlock) || PassThrough.tierOf(state) != PassTier.LAYERS) return false;
		return state.getValue(SnowLayerBlock.LAYERS) < FULL;
	}

	/** Leather boots, or a mob vanilla lets walk on powder snow: stands on snow layers unless sneaking. */
	public static boolean standsOn(Entity e) {
		return PowderSnowBlock.canEntityWalkOnPowderSnow(e);
	}

	/**
	 * The player sees a snow layer from on top (WTHIT shows the surface Run Speed line, not the walk-through line):
	 * it is walk-through, the player can stand on it and is not sneaking.
	 */
	public static boolean viewedFromTop(BlockState state, Entity viewer, PassValues values) {
		return walkThrough(state, values) && standsOn(viewer) && !viewer.isDescending();
	}

	/**
	 * {@code state} with a full vanilla snow layer (8) turned into a snow block when {@code snowFullBecomesBlock} is on;
	 * any other state unchanged. Only {@code minecraft:snow}: other {@link SnowLayerBlock}s (salt layers) stay.
	 */
	public static BlockState fullToBlock(BlockState state, PassValues values) {
		if (!values.snowFullBecomesBlock || !state.is(Blocks.SNOW)) return state;
		return state.getValue(SnowLayerBlock.LAYERS) >= FULL ? Blocks.SNOW_BLOCK.defaultBlockState() : state;
	}

	/** Random tick of a full snow layer already in the world: becomes a snow block. True = done (skip the rest). */
	public static boolean randomTick(BlockState state, ServerLevel level, BlockPos pos) {
		BlockState to = fullToBlock(state, PassValues.local());
		if (to == state) return false;
		level.setBlockAndUpdate(pos, to);
		return true;
	}
}
