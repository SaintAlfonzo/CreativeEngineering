package com.SaintAlfonzo.creativecompat.surface;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pass-through blocks (feature_writeup/plan_passthrough.md): walking and falling through blocks you stand in rather
 * than on. Phase 1 core: which blocks count for an entity, their rule ({@link PassRule}), and what it does to living
 * entities: a walk-speed modifier on land, a horizontal drag while wading, a fall drag, the per-block fall-distance
 * factor and the dense fall reset.
 *
 * <p><b>Which block counts</b> (plan 1): every block whose cell the entity's box overlaps (deflated 1e-7, like vanilla
 * {@code checkInsideBlocks}), that is in a tier tag {@code creativecompat:passthrough/<tier>}, whose outline shape the
 * box touches, that passes its growth rule ({@link PassGrowth}; {@code #creativecompat:passthrough_growth_any} always
 * passes) and its {@code minLayers}, for an entity not exempt from the tier (entity-type tags
 * {@code creativecompat:passthrough_exempt/<tier>} and {@code .../all}, Depth Strider where the tier says so, the
 * owner of a SecurityCraft block). Several at once: the lowest walk and the lowest fall win ({@link PassResult}).
 *
 * <p><b>Entry points.</b> {@link #afterInsideBlocks} runs at TAIL of {@code Entity#checkInsideBlocks}
 * ({@code mixin/InsideBlocksMixin}, after {@link WallSlide}); {@link #cancelsStuck} is the HEAD check of
 * {@code Entity#makeStuckInBlock} ({@code mixin/MakeStuckInBlockMixin}). For later phases: {@link #evaluate} (whole
 * entity), {@link #cellAt} (one block, no entity: WTHIT), {@link #tierOf}, {@link #neighbours}, {@link #state}.
 *
 * <p>Both sides run it: the local player moves itself on the client, mobs on the server, and the server replays the
 * player's moves. Values come from {@link PassValues#of(boolean)} (the server's on a connected client).
 */
public final class PassThrough {

	private PassThrough() {}

	/** The walk modifier (MULTIPLY_TOTAL, {@code walk - 1}); {@link PassFov} takes it out of the field of view. */
	public static final UUID MODIFIER_UUID = UUID.fromString("a1386f64-4583-4798-9b29-f040c59b8574");
	private static final String MODIFIER_NAME = "Pass-through block speed";

	private static final double DEFLATE = 1.0E-7;
	/** Horizontal speed (squared, blocks per tick) below which an entity counts as standing still for walkOnlyMoving. */
	private static final double MOVING_SQR = 1.0E-5;

	/** Tag ids, built on first use (a TagKey needs the registries). */
	private static final class Tags {
		static final Map<PassTier, TagKey<Block>> BLOCK = new EnumMap<>(PassTier.class);
		static final Map<PassTier, TagKey<EntityType<?>>> EXEMPT = new EnumMap<>(PassTier.class);
		static final TagKey<EntityType<?>> EXEMPT_ALL = entityTag("passthrough_exempt/all");
		/** Exempt from the layers tier on leaf layers only (foxes and bees, plan_misc_1117.md 1); snow still slows them. */
		static final TagKey<EntityType<?>> EXEMPT_LEAF_LAYERS = entityTag("passthrough_exempt/leaf_layers");
		/** Leaf layer blocks, walk-through at every height (leaf piles, bamboo leaves; plan_post_scratch_1115.md G7.4). */
		static final TagKey<Block> LAYERS_OPEN = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "passthrough_layers_open"));
		static final TagKey<Block> GROWTH_ANY = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "passthrough_growth_any"));

		static {
			for (PassTier t : PassTier.values()) {
				BLOCK.put(t, BlockTags.create(new ResourceLocation(CreativeCompat.NS, t.tagPath())));
				EXEMPT.put(t, entityTag("passthrough_exempt/" + t.key()));
			}
		}

		private static TagKey<EntityType<?>> entityTag(String path) {
			return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(CreativeCompat.NS, path));
		}
	}

	/** Block tag of a tier ({@code creativecompat:passthrough/<key>}). */
	public static TagKey<Block> blockTag(PassTier tier) {
		return Tags.BLOCK.get(tier);
	}

	/** Entity-type exempt tag of a tier ({@code creativecompat:passthrough_exempt/<key>}). */
	public static TagKey<EntityType<?>> exemptTag(PassTier tier) {
		return Tags.EXEMPT.get(tier);
	}

	public static TagKey<EntityType<?>> exemptAllTag() {
		return Tags.EXEMPT_ALL;
	}

	/** Mobs exempt from leaf layers only ({@code creativecompat:passthrough_exempt/leaf_layers}). */
	public static TagKey<EntityType<?>> exemptLeafLayersTag() {
		return Tags.EXEMPT_LEAF_LAYERS;
	}

	// ---- block -> tier cache ----

	private static final Object NONE = new Object();
	private static final Map<Block, Object> TIER_OF = new ConcurrentHashMap<>();

	/** The block's tier, or null. Cached per block; {@link #clearCache} on every tag reload. */
	@Nullable
	public static PassTier tierOf(BlockState state) {
		if (state instanceof PassTierSlot slot) {
			// field read on the state (mixin/BlockStatePassMixin), valid for this tag generation
			int g = gen;
			int packed = slot.creativecompat$passTier();
			if ((packed >>> 5) == g) {
				int o = packed & 31;
				return o == 0 ? null : TIERS[o - 1];
			}
			PassTier t = tierOf(state.getBlock());
			slot.creativecompat$setPassTier(g << 5 | (t == null ? 0 : t.ordinal() + 1));
			return t;
		}
		return tierOf(state.getBlock());
	}

	private static final PassTier[] TIERS = PassTier.values();
	/** Tag generation of the per-state tier slots: bumped by {@link #clearCache}, starts at 1 (0 = never set). */
	private static volatile int gen = 1;

	@Nullable
	public static PassTier tierOf(Block block) {
		Object t = TIER_OF.get(block);
		if (t == null) {
			t = findTier(block);
			TIER_OF.put(block, t);
		}
		return t == NONE ? null : (PassTier) t;
	}

	private static Object findTier(Block block) {
		BlockState state = block.defaultBlockState();
		for (PassTier t : PassTier.values()) {
			if (state.is(Tags.BLOCK.get(t))) return t;
		}
		return NONE;
	}

	/** Headless tests only (tools/test_passthrough.sh): a tier without tags. */
	static void seedTier(Block block, @Nullable PassTier tier) {
		TIER_OF.put(block, tier == null ? NONE : tier);
	}

	/** TagsUpdatedEvent (both sides): tier membership and growth properties are read again. */
	public static void clearCache() {
		TIER_OF.clear();
		gen = (gen + 1) & 0x07FFFFFF;
		if (gen == 0) gen = 1;
		PassCountCache.get().clear();
		PassCountData.forgetAll();
		PassGrowth.clear();
		PassProjectiles.clearCache();
	}

	// ---- the stuck cancel ----

	/** {@code Entity#makeStuckInBlock} for this state is cancelled: the block is in a tier and the system is on. */
	public static boolean cancelsStuck(Entity entity, BlockState state) {
		return tierOf(state) != null && PassValues.of(entity.level.isClientSide).enabled;
	}

	// ---- vanilla's fast-fall reset ----

	/**
	 * The {@code Level#clip} in {@code Entity#move} ({@code mixin/InsideBlocksMixin}, scratch fixes SQ-PTH-03): moving
	 * a block or more in one tick with some fall distance, vanilla resets it when the path crosses a
	 * {@code #minecraft:fall_damage_resetting} block (cobweb, sweet berry bush, climbables) or water. A web that this
	 * system gives a fall rule, or a bush ({@link #ownsFallReset}), does not reset it: its fall distance factor applies
	 * instead. Any other block or water on the path still does. {@code hit} is vanilla's result, not a miss.
	 */
	public static BlockHitResult fallResetClip(Entity e, Level level, ClipContext ctx, BlockHitResult hit) {
		PassValues values = PassValues.of(level.isClientSide);
		if (!values.enabled || skipsEntity(e)) return hit;
		BlockPos at = hit.getBlockPos();
		if (!ownsFallReset(e, level, at, level.getBlockState(at), values)) return hit;
		return clipFallReset(level, ctx.getFrom(), ctx.getTo(),
				s -> s.is(BlockTags.FALL_DAMAGE_RESETTING), (p, s) -> ownsFallReset(e, level, p, s, values));
	}

	private static boolean ownsFallReset(Entity e, Level level, BlockPos pos, BlockState state, PassValues values) {
		if (state.is(BlockTags.CLIMBABLE)) return false; // ladder rules stay vanilla (plan 3.2)
		PassTier tier = tierOf(state);
		if (tier == null || exempt(e, tier, values.tier(tier), state)) return false;
		if (PassOwner.ownerPasses(level, pos, state, e)) return false;
		return ownsFallReset(level, pos, state, values);
	}

	/**
	 * The block's tier scales the fall distance itself (fall below 1.0) and the block is not dense with
	 * {@code denseResetsFall} (that one resets anyway), or it is a bush (owner ruling: no reset, sweet berry bush
	 * included): vanilla's fast-fall reset must not fire on it. Other tiers without a fall rule (ropes, as
	 * climbables) keep vanilla's reset. No entity: the headless harness tests it.
	 */
	static boolean ownsFallReset(BlockGetter level, BlockPos pos, BlockState state, PassValues values) {
		PassTier tier = tierOf(state);
		if (tier == null) return false;
		PassTier.Values v = values.tier(tier);
		if (v.inert()) return false;
		PassRule.Cell c = rule(level, pos, state, tier, v, values);
		if (c == null) return false;
		if (tier == PassTier.BUSH) return true; // owner ruling: bushes never cancel fall damage
		return c.fall() < 1.0 && !c.resetFall();
	}

	/**
	 * Vanilla's fall-reset clip ({@code ClipContext.Block.FALLDAMAGE_RESETTING}, {@code ClipContext.Fluid.WATER}) from
	 * {@code from} to {@code to}, with the blocks {@code skip} accepts left out. Returns a miss or the first hit.
	 */
	static BlockHitResult clipFallReset(BlockGetter level, Vec3 from, Vec3 to, Predicate<BlockState> resets,
										BiPredicate<BlockPos, BlockState> skip) {
		return BlockGetter.traverseBlocks(from, to, level, (lv, pos) -> {
			BlockState bs = lv.getBlockState(pos);
			BlockHitResult b = resets.test(bs) && !skip.test(pos, bs) ? Shapes.block().clip(from, to, pos) : null;
			FluidState fs = lv.getFluidState(pos);
			BlockHitResult f = fs.is(FluidTags.WATER) ? fs.getShape(lv, pos).clip(from, to, pos) : null;
			if (b == null) return f;
			if (f == null) return b;
			return from.distanceToSqr(b.getLocation()) <= from.distanceToSqr(f.getLocation()) ? b : f;
		}, lv -> {
			Vec3 d = from.subtract(to);
			return BlockHitResult.miss(to, net.minecraft.core.Direction.getNearest(d.x, d.y, d.z), new BlockPos(to));
		});
	}

	// ---- per-block rule ----

	/** Side cells (x, z) and, for {@code FACES}, the cells above and below (plan_post_scratch_1115b.md C1). */
	private static final int[][] SIDE_STEPS = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
	private static final int[][] FACE_STEPS = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};

	static int[][] steps(PassTier.Neighbours mode) {
		return mode.vertical() ? FACE_STEPS : SIDE_STEPS;
	}

	/**
	 * Same-tier neighbors of {@code pos}, counted now: the 4 side-adjacent cells on its layer ({@code SIDES}) or the 6
	 * face-adjacent cells ({@code FACES}). A neighbor counts whatever its growth (a freshly planted crop adds density).
	 * Movement reads the baked count instead ({@link PassCounts#count}).
	 */
	public static int neighbours(BlockGetter level, BlockPos pos, PassTier tier, PassTier.Neighbours mode) {
		if (mode.canonical() == PassTier.Neighbours.NONE) return 0;
		if (PassTiming.on) PassTiming.count(level);
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		int n = 0;
		for (int[] d : steps(mode)) {
			p.set(pos.getX() + d[0], pos.getY() + d[1], pos.getZ() + d[2]);
			if (tierOf(level.getBlockState(p)) == tier) n++;
		}
		return n;
	}

	/** The block passes its tier's growth rule and its layers rule (no entity involved). */
	public static boolean grown(BlockGetter level, BlockPos pos, BlockState state, PassTier tier, PassTier.Values v) {
		if (v.minLayers() > 1) {
			int layers = PassGrowth.layers(state);
			if (layers >= 0 && layers < v.minLayers()) return false;
		}
		if (v.growth() == PassTier.Growth.ANY || state.is(Tags.GROWTH_ANY)) return true;
		return PassGrowth.passes(v.growth(), level, pos, state, tier);
	}

	/**
	 * The rule of one block as anyone standing in it would get it (WTHIT, phase 5; items and projectiles, phase 2),
	 * or null when the block is in no tier or below its growth or layers rule. No entity: no outline, exempt or owner
	 * test.
	 */
	@Nullable
	public static PassRule.Cell cellAt(BlockGetter level, BlockPos pos, BlockState state, PassValues values) {
		PassTier tier = tierOf(state);
		if (tier == null) return null;
		return rule(level, pos, state, tier, values.tier(tier), values);
	}

	/**
	 * The rule of one tiered block: null below its growth or layers rule, or a layer block high enough to act whole.
	 * Neighbor counts come baked ({@link PassCounts}).
	 */
	@Nullable
	static PassRule.Cell rule(BlockGetter level, BlockPos pos, BlockState state, PassTier tier, PassTier.Values v,
							  PassValues values) {
		if (!grown(level, pos, state, tier, v)) return null;
		if (tier.layered()) {
			double w = layerWalk(state, v, values);
			return Double.isNaN(w) ? null : PassRule.layerCell(tier, v, w);
		}
		int n = PassRule.needsCount(v) ? PassCounts.count(level, pos, tier, v.neighbours()) : 0;
		return PassRule.cell(tier, v, n);
	}

	/**
	 * Walk value inside a layer height block (plan_post_scratch_1115.md G7.4), from its own {@code layers} value and
	 * maximum: leaf layers ({@code #creativecompat:passthrough_layers_open}) walk through at every height down to
	 * {@code leafMaxSlow}, snow and any other layer block up to one layer below full down to {@code snowMaxSlow}. NaN
	 * when the block has no layers property, has fewer than {@code minLayers}, or is full.
	 */
	public static double layerWalk(BlockState state, PassTier.Values v, PassValues values) {
		int l = PassGrowth.layers(state);
		int max = PassGrowth.layersMax(state);
		if (l < 0 || max <= 0 || l < v.minLayers()) return Double.NaN;
		boolean open = openLayers(state);
		return PassRule.layerWalk(l, max, open, open ? v.leafMaxSlow() : v.snowMaxSlow());
	}

	/** A leaf layer block (walk-through at every height): {@code #creativecompat:passthrough_layers_open}. */
	public static boolean openLayers(BlockState state) {
		return state.is(Tags.LAYERS_OPEN);
	}

	/** The block's layers and the most it can have, for the WTHIT layer note ({@code {-1, -1}}: none). */
	public static int[] layers(BlockState state) {
		return new int[]{PassGrowth.layers(state), PassGrowth.layersMax(state)};
	}

	// ---- per-entity ----

	/** Never slowed by anything here (plan 1 "Also skipped"): spectators, no-physics, flying, gliding, swimming. */
	public static boolean skipsEntity(Entity e) {
		if (e.isSpectator() || e.noPhysics) return true;
		if (e instanceof Player p && p.getAbilities().flying) return true;
		if (e instanceof LivingEntity l && l.isFallFlying()) return true;
		return e.isSwimming() || e.getPose() == Pose.SWIMMING
				|| e.isEyeInFluid(FluidTags.WATER) || e.isEyeInFluid(FluidTags.LAVA);
	}

	/** The entity ignores this tier: exempt tags, or Depth Strider on a tier with depthStriderPasses. */
	public static boolean exempt(Entity e, PassTier tier, PassTier.Values v) {
		EntityType<?> type = e.getType();
		if (type.is(Tags.EXEMPT_ALL) || type.is(Tags.EXEMPT.get(tier))) return true;
		return v.depthStriderPasses() && e instanceof LivingEntity l && EnchantmentHelper.getDepthStrider(l) > 0;
	}

	/** {@link #exempt(Entity, PassTier, PassTier.Values)}, plus the leaf-layer-only exemption for this block. */
	public static boolean exempt(Entity e, PassTier tier, PassTier.Values v, BlockState state) {
		return exempt(e, tier, v) || leafLayerExempt(e, tier, state);
	}

	/** A leaf layer (leaf pile, bamboo leaves) and a mob in {@code passthrough_exempt/leaf_layers}: not slowed. */
	public static boolean leafLayerExempt(Entity e, PassTier tier, BlockState state) {
		return tier == PassTier.LAYERS && state.is(Tags.LAYERS_OPEN) && e.getType().is(Tags.EXEMPT_LEAF_LAYERS);
	}

	/** Rope climbing (plan 3.2): on a climbable block and moving up or down, sneaking, or off the ground. */
	private static boolean climbing(Entity e) {
		if (!(e instanceof LivingEntity l) || !l.onClimbable()) return false;
		return !e.isOnGround() || l.isShiftKeyDown() || Math.abs(e.getDeltaMovement().y) > 1.0E-3;
	}

	/**
	 * Every counted block for {@code e} this tick, into {@code out} (reset first). Does not change the entity.
	 * Inert tiers (all values 1.0, e.g. grass by default) are skipped.
	 *
	 * <p>Neighbor counts (plan_post_scratch_1115b.md C2): baked into the chunk ({@link PassCounts}), so a counted
	 * block is read once until a neighbor is broken or placed; the first counts of an entity are done together from
	 * one read of the blocks around them ({@link #countRegion}).
	 */
	public static PassResult evaluate(Entity e, PassValues values, PassResult out) {
		out.reset();
		Level level = e.level;
		AABB box = e.getBoundingBox();
		int x0 = Mth.floor(box.minX + DEFLATE), y0 = Mth.floor(box.minY + DEFLATE), z0 = Mth.floor(box.minZ + DEFLATE);
		int x1 = Mth.floor(box.maxX - DEFLATE), y1 = Mth.floor(box.maxY - DEFLATE), z1 = Mth.floor(box.maxZ - DEFLATE);
		if (!level.hasChunksAt(x0, y0, z0, x1, y1, z1)) return out;
		Cand c = CAND.get();
		c.n = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int moving = -1, climbing = -1; // lazily computed: -1 unknown, 0 no, 1 yes
		int exemptKnown = 0, exemptSet = 0; // per tier bit: exempt() asked / answered true
		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {
				for (int z = z0; z <= z1; z++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) continue;
					PassTier tier = tierOf(state);
					if (tier == null) continue;
					PassTier.Values v = values.tier(tier);
					if (v.inert()) continue;
					int bit = 1 << tier.ordinal();
					if ((exemptKnown & bit) == 0) {
						exemptKnown |= bit;
						if (exempt(e, tier, v)) exemptSet |= bit;
					}
					if ((exemptSet & bit) != 0) continue;
					if (leafLayerExempt(e, tier, state)) continue;
					if (!touchesOutline(level, pos, state, e, box)) continue;
					if (!grown(level, pos, state, tier, v)) continue;
					double layerWalk = Double.NaN;
					if (tier.layered()) {
						layerWalk = layerWalk(state, v, values);
						if (Double.isNaN(layerWalk)) continue; // acts whole: normal terrain
					}
					if (PassOwner.ownerPasses(level, pos, state, e)) continue;
					if (tier.layered()) out.addLayer(pos.asLong(), openLayers(state), PassGrowth.layers(state), values.motion);
					boolean walkCounts = true;
					if (v.walkOnlyMoving()) {
						if (moving < 0) moving = e.getDeltaMovement().horizontalDistanceSqr() > MOVING_SQR ? 1 : 0;
						walkCounts = moving == 1;
					}
					if (walkCounts && tier.noWalkWhileClimbing()) {
						if (climbing < 0) climbing = climbing(e) ? 1 : 0;
						walkCounts = climbing == 0;
					}
					c.add(pos.asLong(), tier, v, walkCounts, layerWalk);
				}
			}
		}
		if (c.n == 0) return out;
		counts(level, c);
		for (int i = 0; i < c.n; i++) {
			PassTier.Values v = c.values[i];
			PassRule.Cell rule = Double.isNaN(c.layerWalk[i]) ? PassRule.cell(c.tier[i], v, c.count[i])
					: PassRule.layerCell(c.tier[i], v, c.layerWalk[i]);
			out.add(c.pos[i], rule, c.walk[i], v.projectiles());
		}
		c.clearRefs();
		return out;
	}

	/** The counted blocks of one {@link #evaluate} before their rule is built. One per thread. */
	static final class Cand {
		int n;
		long[] pos = new long[8];
		PassTier[] tier = new PassTier[8];
		PassTier.Values[] values = new PassTier.Values[8];
		boolean[] walk = new boolean[8];
		/** Neighbor count, -1 = still to count. */
		int[] count = new int[8];
		PassTier.Neighbours[] mode = new PassTier.Neighbours[8];
		/** Layer tier: the height-derived walk value; NaN otherwise. */
		double[] layerWalk = new double[8];

		void add(long p, PassTier t, PassTier.Values v, boolean walkCounts, double lw) {
			if (n == pos.length) {
				int m = n * 2;
				pos = java.util.Arrays.copyOf(pos, m);
				tier = java.util.Arrays.copyOf(tier, m);
				values = java.util.Arrays.copyOf(values, m);
				walk = java.util.Arrays.copyOf(walk, m);
				count = java.util.Arrays.copyOf(count, m);
				mode = java.util.Arrays.copyOf(mode, m);
				layerWalk = java.util.Arrays.copyOf(layerWalk, m);
			}
			layerWalk[n] = lw;
			pos[n] = p;
			tier[n] = t;
			values[n] = v;
			walk[n] = walkCounts;
			mode[n] = v.neighbours();
			count[n] = PassRule.needsCount(v) ? -1 : 0;
			n++;
		}

		void clearRefs() {
			java.util.Arrays.fill(tier, 0, n, null);
			java.util.Arrays.fill(values, 0, n, null);
			java.util.Arrays.fill(mode, 0, n, null);
			n = 0;
		}
	}

	private static final ThreadLocal<Cand> CAND = ThreadLocal.withInitial(Cand::new);

	/**
	 * Fills every count still -1: the count baked into the chunk ({@link PassCounts}, plan_post_scratch_1115b.md C2),
	 * else counted now (several at once from one block read, {@link #countRegion}) and baked.
	 */
	private static void counts(Level level, Cand c) {
		// one chunk lookup per chunk, not two per cell (TODO tooling/scratch-1115-follow-ups-2 #11): an entity's cells
		// are nearly always in one chunk
		PassCountData[] data = DATA.get();
		if (data.length < c.n) DATA.set(data = new PassCountData[c.n * 2]);
		int minY = level.getMinBuildHeight();
		int lcx = Integer.MIN_VALUE, lcz = Integer.MIN_VALUE;
		PassCountData ld = null;
		int misses = 0;
		for (int i = 0; i < c.n; i++) {
			if (c.count[i] >= 0) continue;
			long p = c.pos[i];
			int x = BlockPos.getX(p), z = BlockPos.getZ(p);
			if ((x >> 4) != lcx || (z >> 4) != lcz) {
				lcx = x >> 4;
				lcz = z >> 4;
				ld = PassCounts.chunkData(level, x, z);
			}
			data[i] = ld;
			int k = ld == null ? -1 : ld.get(PassCountData.key(x, BlockPos.getY(p), z, minY), c.mode[i].vertical());
			if (k >= 0) c.count[i] = k;
			else misses++;
		}
		if (misses == 0) {
			java.util.Arrays.fill(data, 0, c.n, null);
			return;
		}
		boolean[] miss = MISS.get();
		if (miss.length < c.n) MISS.set(miss = new boolean[c.n * 2]);
		for (int i = 0; i < c.n; i++) miss[i] = c.count[i] < 0;
		if (misses == 1) {
			for (int i = 0; i < c.n; i++) {
				if (c.count[i] >= 0) continue;
				c.count[i] = neighbours(level, BlockPos.of(c.pos[i]), c.tier[i], c.mode[i]);
			}
		} else {
			if (PassTiming.on) for (int i = 0; i < misses; i++) PassTiming.count(level);
			countRegion(level, c.pos, c.tier, c.mode, c.count, c.n);
		}
		for (int i = 0; i < c.n; i++) {
			if (miss[i] && data[i] != null) {
				long p = c.pos[i];
				data[i].put(PassCountData.key(BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p), minY),
						c.mode[i].vertical(), c.count[i]);
			}
		}
		java.util.Arrays.fill(data, 0, c.n, null);
	}

	private static final ThreadLocal<PassCountData[]> DATA = ThreadLocal.withInitial(() -> new PassCountData[8]);
	private static final ThreadLocal<boolean[]> MISS = ThreadLocal.withInitial(() -> new boolean[8]);

	private static final int REGION_MAX = 4096;
	private static final ThreadLocal<byte[]> REGION = ThreadLocal.withInitial(() -> new byte[REGION_MAX]);

	/**
	 * Counts every cell whose {@code count} is -1 from one read of the blocks around them all (each block's tier read
	 * once), with the same rule as {@link #neighbours}: same-tier blocks among the 4 side cells ({@code SIDES}) or the 6
	 * face cells ({@code FACES}). Falls back to {@link #neighbours} per cell when the area is too big. Package-private
	 * for the harness.
	 */
	static void countRegion(BlockGetter level, long[] pos, PassTier[] tier, PassTier.Neighbours[] mode, int[] count, int n) {
		int ax = Integer.MAX_VALUE, ay = Integer.MAX_VALUE, az = Integer.MAX_VALUE;
		int bx = Integer.MIN_VALUE, by = Integer.MIN_VALUE, bz = Integer.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			if (count[i] >= 0) continue;
			long p = pos[i];
			int x = BlockPos.getX(p), y = BlockPos.getY(p), z = BlockPos.getZ(p);
			int dy = mode[i].vertical() ? 1 : 0;
			ax = Math.min(ax, x - 1);
			bx = Math.max(bx, x + 1);
			az = Math.min(az, z - 1);
			bz = Math.max(bz, z + 1);
			ay = Math.min(ay, y - dy);
			by = Math.max(by, y + dy);
		}
		if (ax == Integer.MAX_VALUE) return;
		int sx = bx - ax + 1, sy = by - ay + 1, sz = bz - az + 1;
		if ((long) sx * sy * sz > REGION_MAX) {
			for (int i = 0; i < n; i++) {
				if (count[i] < 0) count[i] = neighbours(level, BlockPos.of(pos[i]), tier[i], mode[i]);
			}
			return;
		}
		byte[] t = REGION.get();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		int k = 0;
		for (int x = ax; x <= bx; x++) {
			for (int y = ay; y <= by; y++) {
				for (int z = az; z <= bz; z++) {
					m.set(x, y, z);
					PassTier tt = tierOf(level.getBlockState(m));
					t[k++] = (byte) (tt == null ? 0 : tt.ordinal() + 1);
				}
			}
		}
		for (int i = 0; i < n; i++) {
			if (count[i] >= 0) continue;
			long p = pos[i];
			int cx = BlockPos.getX(p) - ax, cy = BlockPos.getY(p) - ay, cz = BlockPos.getZ(p) - az;
			byte want = (byte) (tier[i].ordinal() + 1);
			int c = 0;
			for (int[] d : steps(mode[i])) {
				if (t[((cx + d[0]) * sy + (cy + d[1])) * sz + (cz + d[2])] == want) c++;
			}
			count[i] = c;
		}
	}

	/** The entity's box touches the block's outline shape (an empty outline: the cell overlap is enough). */
	private static boolean touchesOutline(Level level, BlockPos pos, BlockState state, Entity e, AABB box) {
		VoxelShape shape = state.getShape(level, pos, CollisionContext.of(e));
		if (shape.isEmpty() || shape == Shapes.block()) return true; // the box already overlaps the cell
		AABB local = box.move(-pos.getX(), -pos.getY(), -pos.getZ());
		if (!shape.bounds().intersects(local)) return false;
		boolean[] hit = {false};
		shape.forAllBoxes((ax, ay, az, bx, by, bz) -> {
			if (!hit[0] && local.intersects(ax, ay, az, bx, by, bz)) hit[0] = true;
		});
		return hit[0];
	}

	/**
	 * The entity's {@link PassState}, created when {@code create}. With InsideBlocksMixin switched off ([mixins] in
	 * creativecompat-common.toml) entities carry no state: a throwaway one when {@code create}, else null.
	 */
	@Nullable
	public static PassState state(Entity e, boolean create) {
		if (e instanceof PassHolder h) return h.creativecompat$passState(create);
		return create ? new PassState() : null;
	}

	// ---- movement ----

	/**
	 * TAIL of {@code Entity#checkInsideBlocks}: apply this tick's rule. Living entities here; items and orbs
	 * ({@link PassItems}), projectiles and every other non-living entity ({@link PassProjectiles}) in phase 2: the stuck
	 * cancel took vanilla's web slowdown from them too (phase 0 brief, implication 19).
	 */
	public static void afterInsideBlocks(Entity e) {
		if (!(e instanceof LivingEntity living)) {
			if (e instanceof ItemEntity || e instanceof ExperienceOrb) {
				PassItems.afterInsideBlocks(e);
			} else if (e instanceof Projectile p) {
				PassProjectiles.afterInsideBlocks(p);
			} else {
				PassProjectiles.otherEntity(e);
			}
			return;
		}
		PassValues values = PassValues.of(e.level.isClientSide);
		PassState st = state(e, false);
		if (!values.enabled || skipsEntity(e)) {
			if (st != null) leave(living, st);
			return;
		}
		if (st == null) {
			// most entities are in no tier block: check with a shared scratch result before giving them a state
			PassResult scratch = SCRATCH.get();
			if (!evaluate(e, values, scratch).any()) return;
			st = state(e, true);
		}
		PassResult r = evaluate(e, values, st.result);
		if (!r.any()) {
			leave(living, st);
			return;
		}
		apply(living, st, r);
		st.rememberCells();
	}

	private static final ThreadLocal<PassResult> SCRATCH = ThreadLocal.withInitial(PassResult::new);

	/**
	 * Before {@code Entity#checkFallDamage} in {@code Entity#move} ({@code mixin/InsideBlocksMixin}; TODO
	 * {@code tooling/scratch-1115-follow-ups-2 #8}): an entity that lands this move with some fall distance gets the
	 * fall factor of the cells it entered this tick before the landing deals damage. Without it a drop that enters a
	 * ground-level web and lands in the same move took full damage: {@link #afterInsideBlocks} runs after the landing,
	 * when the fall distance is already spent. A dense web with {@code denseResetsFall} or a surrounded one resets it.
	 * Cells the entity was in last tick were already counted by {@link #afterInsideBlocks} and do not count again; the
	 * cells counted here are remembered, so the same move's {@link #afterInsideBlocks} skips them.
	 */
	public static void beforeLanding(Entity e) {
		if (!(e instanceof LivingEntity) || !e.isOnGround() || e.fallDistance <= 0.0F) return;
		PassValues values = PassValues.of(e.level.isClientSide);
		if (!values.enabled || skipsEntity(e)) return;
		PassResult r = evaluate(e, values, SCRATCH.get());
		if (!r.any()) return;
		if (r.resetFall || r.surrounded) {
			e.resetFallDistance();
			return;
		}
		PassState st = state(e, true);
		double f = newCellsFallFactor(st, r);
		if (f < 1.0) e.fallDistance = (float) (e.fallDistance * f);
		// counted now: the TAIL hook of this move must not count them again (a server player keeps its fall
		// distance through the move; ServerPlayer#doCheckFallDamage spends it after)
		st.rememberCells(r);
	}

	/**
	 * RETURN of {@code LivingEntity#getJumpPower} ({@code mixin/LayerJumpMixin}): the jump power inside snow and leaf
	 * layers (plan_post_scratch_1115.md G4c, G7.13), from last tick's result. Exempt mobs have no counted cells.
	 */
	public static float jumpPower(LivingEntity e, float power) {
		PassState st = state(e, false);
		if (st == null || st.result.jump >= 1.0) return power;
		PassValues values = PassValues.of(e.level.isClientSide);
		if (!values.enabled || skipsEntity(e)) return power;
		return (float) (power * st.result.jump);
	}

	/** A shared result for a check that needs no per-entity state yet (same thread only, do not keep). */
	static PassResult scratch() {
		return SCRATCH.get();
	}

	/**
	 * RETURN of {@code LivingEntity#handleOnClimbable} ({@code mixin/LivingClimbMixin}), the motion the coming move
	 * uses (plan 4.3): in a surrounded web jumping climbs at ladder speed x the dense walk value, sneaking descends at
	 * ladder slide speed x the same, no input hangs; the fall distance stays 0. Null = leave the motion alone. Reads
	 * last tick's result (the hook runs inside the move).
	 */
	@Nullable
	public static Vec3 surroundedMotion(LivingEntity e, Vec3 motion, boolean jumping) {
		PassState st = state(e, false);
		if (st == null || !st.result.surrounded || st.result.surroundedTier == null) return null;
		PassValues values = PassValues.of(e.level.isClientSide);
		if (!values.enabled || skipsEntity(e)) return null;
		double dw = values.tier(st.result.surroundedTier).denseWalk();
		e.resetFallDistance();
		return new Vec3(motion.x, PassMath.surroundedVertical(jumping, e.isShiftKeyDown(), dw), motion.z);
	}

	private static void apply(LivingEntity e, PassState st, PassResult r) {
		Vec3 v = e.getDeltaMovement();
		double vx = v.x, vy = v.y, vz = v.z;
		if (e.isInWater()) {
			// wading (eyes out, or skipsEntity would have caught it): the speed attribute does nothing in water
			setModifier(e, 1.0);
			if (r.walk < 1.0) {
				double m = PassMath.wadeFactor(r.walk, e.isSprinting() ? 0.9 : 0.8);
				vx *= m;
				vz *= m;
			}
		} else {
			setModifier(e, r.walk);
		}
		if (vy < 0.0 && r.fall < 1.0) {
			vy *= r.fall;
		}
		// inside snow or leaf layers: falling is capped (plan_post_scratch_1115.md G7.13, LayerMotion)
		vy = LayerMotion.capFall(vy, r.fallCap);
		if (vx != v.x || vy != v.y || vz != v.z) {
			e.setDeltaMovement(vx, vy, vz);
		}
		if (r.resetFall || r.surrounded) {
			e.resetFallDistance();
		} else if (e.fallDistance > 0.0F) {
			double f = newCellsFallFactor(st, r);
			if (f < 1.0) e.fallDistance = (float) (e.fallDistance * f);
		}
	}

	/**
	 * Fall-distance factor for the cells entered this tick (plan 1.2: once per block passed). Cells side by side on
	 * one layer are one block passed (the lowest factor of that layer); each new layer multiplies. No state: every cell
	 * is new.
	 */
	static double newCellsFallFactor(@Nullable PassState st, PassResult r) {
		double product = 1.0;
		int n = r.cells();
		for (int i = 0; i < n; i++) {
			long pos = r.cellPos(i);
			if (st != null && st.wasIn(pos)) continue;
			int y = BlockPos.getY(pos);
			boolean seenLayer = false;
			double layerMin = r.cellRule(i).fall();
			for (int j = 0; j < n; j++) {
				if (j == i) continue;
				long other = r.cellPos(j);
				if (BlockPos.getY(other) != y || (st != null && st.wasIn(other))) continue;
				if (j < i) { seenLayer = true; break; }
				layerMin = Math.min(layerMin, r.cellRule(j).fall());
			}
			if (!seenLayer) product *= layerMin;
		}
		return product;
	}

	/** Left every tier block (or skipped): modifier off, cells forgotten. */
	private static void leave(LivingEntity e, PassState st) {
		setModifier(e, 1.0);
		st.forgetCells();
		st.result.reset();
	}

	/** Puts the walk modifier at {@code walk - 1}, or removes it at 1.0. Checks the real modifier (attribute sync can replace it). */
	private static void setModifier(LivingEntity e, double walk) {
		AttributeInstance speed = e.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) return;
		AttributeModifier cur = speed.getModifier(MODIFIER_UUID);
		if (walk >= 1.0) {
			if (cur != null) speed.removeModifier(MODIFIER_UUID);
			return;
		}
		double amount = PassMath.modifierAmount(walk);
		if (cur != null) {
			if (cur.getAmount() == amount && cur.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) return;
			speed.removeModifier(MODIFIER_UUID);
		}
		speed.addTransientModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, amount,
				AttributeModifier.Operation.MULTIPLY_TOTAL));
	}
}
