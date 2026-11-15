package com.SaintAlfonzo.creativecompat.surface;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
        static final TagKey<Block> GROWTH_ANY = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "passthrough_growth_any"));

        static {
            for (PassTier t : PassTier.values()) {
                BLOCK.put(t, BlockTags.create(new ResourceLocation(CreativeCompat.NS, "passthrough/" + t.key())));
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

    // ---- block -> tier cache ----

    private static final Object NONE = new Object();
    private static final Map<Block, Object> TIER_OF = new ConcurrentHashMap<>();

    /** The block's tier, or null. Cached per block; {@link #clearCache} on every tag reload. */
    @Nullable
    public static PassTier tierOf(BlockState state) {
        return tierOf(state.getBlock());
    }

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
        PassGrowth.clear();
    }

    // ---- the stuck cancel ----

    /** {@code Entity#makeStuckInBlock} for this state is cancelled: the block is in a tier and the system is on. */
    public static boolean cancelsStuck(Entity entity, BlockState state) {
        return tierOf(state) != null && PassValues.of(entity.level.isClientSide).enabled;
    }

    // ---- per-block rule ----

    /** Same-tier neighbours of {@code pos}: the 8 around it on its layer (ring) or all 26 (cube). */
    public static int neighbours(BlockGetter level, BlockPos pos, PassTier tier, PassTier.Neighbours mode) {
        if (mode == PassTier.Neighbours.NONE) return 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int n = 0;
        int dy0 = mode == PassTier.Neighbours.CUBE ? -1 : 0, dy1 = mode == PassTier.Neighbours.CUBE ? 1 : 0;
        for (int dy = dy0; dy <= dy1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    p.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (tierOf(level.getBlockState(p)) == tier) n++;
                }
            }
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
        PassTier.Values v = values.tier(tier);
        if (!grown(level, pos, state, tier, v)) return null;
        int n = PassRule.needsCount(v) ? neighbours(level, pos, tier, v.neighbours()) : 0;
        return PassRule.cell(tier, v, n);
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

    /** Rope climbing (plan 3.2): on a climbable block and moving up or down, sneaking, or off the ground. */
    private static boolean climbing(Entity e) {
        if (!(e instanceof LivingEntity l) || !l.onClimbable()) return false;
        return !e.isOnGround() || l.isShiftKeyDown() || Math.abs(e.getDeltaMovement().y) > 1.0E-3;
    }

    /**
     * Every counted block for {@code e} this tick, into {@code out} (reset first). Does not change the entity.
     * Inert tiers (all values 1.0, e.g. grass by default) are skipped.
     */
    public static PassResult evaluate(Entity e, PassValues values, PassResult out) {
        out.reset();
        Level level = e.level;
        AABB box = e.getBoundingBox();
        int x0 = Mth.floor(box.minX + DEFLATE), y0 = Mth.floor(box.minY + DEFLATE), z0 = Mth.floor(box.minZ + DEFLATE);
        int x1 = Mth.floor(box.maxX - DEFLATE), y1 = Mth.floor(box.maxY - DEFLATE), z1 = Mth.floor(box.maxZ - DEFLATE);
        if (!level.hasChunksAt(x0, y0, z0, x1, y1, z1)) return out;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int moving = -1, climbing = -1; // lazily computed: -1 unknown, 0 no, 1 yes
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    PassTier tier = tierOf(state);
                    if (tier == null) continue;
                    PassTier.Values v = values.tier(tier);
                    if (v.inert() || exempt(e, tier, v)) continue;
                    if (!touchesOutline(level, pos, state, e, box)) continue;
                    if (!grown(level, pos, state, tier, v)) continue;
                    if (PassOwner.ownerPasses(level, pos, state, e)) continue;
                    int n = PassRule.needsCount(v) ? neighbours(level, pos, tier, v.neighbours()) : 0;
                    boolean walkCounts = true;
                    if (v.walkOnlyMoving()) {
                        if (moving < 0) moving = e.getDeltaMovement().horizontalDistanceSqr() > MOVING_SQR ? 1 : 0;
                        walkCounts = moving == 1;
                    }
                    if (walkCounts && tier.noWalkWhileClimbing()) {
                        if (climbing < 0) climbing = climbing(e) ? 1 : 0;
                        walkCounts = climbing == 0;
                    }
                    out.add(pos.asLong(), PassRule.cell(tier, v, n), walkCounts, v.projectiles());
                }
            }
        }
        return out;
    }

    /** The entity's box touches the block's outline shape (an empty outline: the cell overlap is enough). */
    private static boolean touchesOutline(Level level, BlockPos pos, BlockState state, Entity e, AABB box) {
        VoxelShape shape = state.getShape(level, pos, CollisionContext.of(e));
        if (shape.isEmpty()) return true;
        AABB local = box.move(-pos.getX(), -pos.getY(), -pos.getZ());
        if (!shape.bounds().intersects(local)) return false;
        boolean[] hit = {false};
        shape.forAllBoxes((ax, ay, az, bx, by, bz) -> {
            if (!hit[0] && local.intersects(ax, ay, az, bx, by, bz)) hit[0] = true;
        });
        return hit[0];
    }

    /** The entity's {@link PassState}, created when {@code create}. */
    @Nullable
    public static PassState state(Entity e, boolean create) {
        return ((PassHolder) e).creativecompat$passState(create);
    }

    // ---- movement (phase 1: living entities) ----

    /** TAIL of {@code Entity#checkInsideBlocks}: apply this tick's rule to a living entity. */
    public static void afterInsideBlocks(Entity e) {
        if (!(e instanceof LivingEntity living)) {
            return; // items, orbs and projectiles: phase 2
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
        if (vx != v.x || vy != v.y || vz != v.z) {
            e.setDeltaMovement(vx, vy, vz);
        }
        if (r.resetFall) {
            e.resetFallDistance();
        } else if (e.fallDistance > 0.0F) {
            double f = newCellsFallFactor(st, r);
            if (f < 1.0) e.fallDistance = (float) (e.fallDistance * f);
        }
    }

    /**
     * Fall-distance factor for the cells entered this tick (plan 1.2: once per block passed). Cells side by side on
     * one layer are one block passed (the lowest factor of that layer); each new layer multiplies.
     */
    static double newCellsFallFactor(PassState st, PassResult r) {
        double product = 1.0;
        int n = r.cells();
        for (int i = 0; i < n; i++) {
            long pos = r.cellPos(i);
            if (st.wasIn(pos)) continue;
            int y = BlockPos.getY(pos);
            boolean seenLayer = false;
            double layerMin = r.cellRule(i).fall();
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                long other = r.cellPos(j);
                if (BlockPos.getY(other) != y || st.wasIn(other)) continue;
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
