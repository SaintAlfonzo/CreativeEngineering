package com.SaintAlfonzo.creativecompat.surface;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
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
 *       through it at the layer tier's height slowdown ({@link PassThrough#layerWalk}). Leather boots or a light
 *       mob ({@link #light}, G11) above the layer's top and not sneaking stands on it instead, on the full layer height (the outline), at BlockRunner's snow surface speed:
 *       the "on top" speed. Sneaking sinks in, like powder snow. Items, falling blocks, projectiles and any
 *       context without an entity (pathfinding, placement checks) keep vanilla's collision.</li>
 *   <li><b>Full layer to snow block</b> ({@code snowFullBecomesBlock}): vanilla snow that reaches 8 layers becomes a
 *       snow block, on placement ({@code getStateForPlacement}) and on a random tick for one already in the world.
 *       {@link #fullToBlock} is the shared seam for any other code that stacks snow (falling layers merging).</li>
 *   <li><b>Mobs</b> (G11): light mobs stand on top, others sink in. A sunk mob climbs out when it walks
 *       into a block ({@link #mobMove}); path searches plan over the top (vanilla's view) unless the mob is already
 *       sunk, then through the layers ({@link #openForPath}).</li>
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
		if (e == null && openForPath(state)) return Shapes.empty(); // a sunk mob's path search (floor heights)
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

	/**
	 * Stands on snow layers unless sneaking (G11): anything wearing boots that walk on powder snow (leather
	 * boots), or a light mob ({@link #light}). Vanilla's {@code #minecraft:powder_snow_walkable_mobs} no longer counts on
	 * its own: in this pack it holds tigers and bison.
	 */
	public static boolean standsOn(Entity e) {
		if (!(e instanceof LivingEntity le)) return false;
		if (le.getItemBySlot(EquipmentSlot.FEET).canWalkOnPowderedSnow(le)) return true;
		return light(le);
	}

	/**
	 * A light mob (G11): never a player; in {@code #creativecompat:snow_layer_light}, or a hitbox of at most
	 * {@code snowLightVolume} cubic blocks (width x width x height, so babies count at their own size).
	 */
	public static boolean light(LivingEntity e) {
		if (e instanceof Player) return false;
		if (e.getType().is(LIGHT)) return true;
		return lightByBox(e.getBbWidth(), e.getBbHeight(), lightVolume());
	}

	/** Width x width x height at most {@code max}; {@code max <= 0} = none by size. */
	public static boolean lightByBox(double width, double height, double max) {
		return max > 0.0 && width * width * height <= max;
	}

	public static final double DEFAULT_LIGHT_VOLUME = 0.3;

	/** {@code snowLightVolume}: this side's toml (not synced: the client moves only the player, who is never light). */
	static double lightVolume() {
		if (!CompatConfig.SPEC.isLoaded()) return DEFAULT_LIGHT_VOLUME;
		return CompatConfig.SURFACE.passthrough.snowLightVolume.get();
	}

	/** Entity tag of mobs that stand on snow layers whatever their size. */
	public static final TagKey<EntityType<?>> LIGHT =
			TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(CreativeCompat.NS, "snow_layer_light"));

	// ---- sunk mobs: climbing out and pathing (G11) ----

	/** Feet below the layer's top by more than this = sunk in. */
	static final double SUNK_MARGIN = 0.05;

	/** Feet at {@code y} in a snow layer of {@code layers} at block height {@code blockY}: below its top. */
	public static boolean below(double y, int blockY, int layers) {
		return y < blockY + layers / (double) FULL - SUNK_MARGIN;
	}

	/** The entity's feet are inside a walk-through snow layer, below its top. */
	public static boolean sunkIn(LivingEntity e) {
		BlockState s = e.getFeetBlockState();
		if (!(s.getBlock() instanceof SnowLayerBlock)) return false;
		if (!walkThrough(s, PassValues.of(e.level.isClientSide))) return false;
		return below(e.getY(), e.getBlockY(), s.getValue(SnowLayerBlock.LAYERS));
	}

	/**
	 * End of {@code handleRelativeFrictionAndCalculateMovement} for a mob ({@code mixin/SnowMobMixin}): a mob sunk in
	 * snow layers that walks into a block climbs at ladder speed ({@code snowMobClimb}); a sunk mob whose path still
	 * steps up onto a walk-through layer (planned before it sank) asks for a new path ({@code snowSunkPathing}).
	 * Null = keep {@code motion}.
	 */
	@Nullable
	public static Vec3 mobMove(LivingEntity e, Vec3 motion) {
		if (!(e instanceof Mob mob) || e.level.isClientSide || !CompatConfig.SPEC.isLoaded()) return null;
		if (!sunkIn(mob)) return null;
		CompatConfig.Surface.Passthrough c = CompatConfig.SURFACE.passthrough;
		if (c.snowSunkPathing.get()) dropStalePath(mob);
		if (c.snowMobClimb.get() && mob.horizontalCollision) return new Vec3(motion.x, CLIMB_SPEED, motion.z);
		return null;
	}

	/** Vanilla's ladder and powder snow climb speed, blocks per tick. */
	public static final double CLIMB_SPEED = 0.2;

	private static void dropStalePath(Mob mob) {
		PathNavigation nav = mob.getNavigation();
		Path path = nav.getPath();
		if (path == null || path.isDone()) return;
		BlockPos next = path.getNextNodePos();
		if (next.getY() <= mob.getBlockY()) return;
		BlockState under = mob.level.getBlockState(next.below());
		if (walkThrough(under, PassValues.local())) nav.recomputePath();
	}

	/** True while a node evaluator plans for a mob that is sunk in snow layers (server thread; {@link #beginPath}). */
	private static final ThreadLocal<boolean[]> SUNK_PATH = ThreadLocal.withInitial(() -> new boolean[1]);

	/** Start of a path search ({@code mixin/SnowPathMixin}, NodeEvaluator.prepare): remembers whether the mob is sunk. */
	public static void beginPath(@Nullable Mob mob) {
		boolean sunk = false;
		if (mob != null && !mob.level.isClientSide && CompatConfig.SPEC.isLoaded()
				&& CompatConfig.SURFACE.passthrough.snowSunkPathing.get()) {
			sunk = sunkIn(mob);
		}
		SUNK_PATH.get()[0] = sunk;
	}

	/** End of a path search (NodeEvaluator.done). */
	public static void endPath() {
		SUNK_PATH.get()[0] = false;
	}

	/**
	 * For a sunk mob's path search: a walk-through snow layer is open air (the mob paths through it at the ground
	 * below). Other mobs keep vanilla's view, which plans over the top (1 to 4 layers open at the feet, 5 to 7 a block
	 * to stand on). {@code mixin/LeafPilePathMixin} and {@code McaLeafPilePathMixin} call it.
	 */
	public static boolean openForPath(BlockState state) {
		if (!(state.getBlock() instanceof SnowLayerBlock) || !SUNK_PATH.get()[0]) return false;
		return walkThrough(state, PassValues.local());
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
