package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Purely visual soul-sand-style sinking. Soul sand's real sink is its 14/16 collision box; this
 * leaves collision alone and draws the camera and entity models lower while they stand on a full
 * block in {@link #SINKING} (kubejs blocktags.js: BlockRunner's soft, dust, spongy and
 * very_sticky tiers). Movement, hitboxes, mob AI and spawning are untouched.
 *
 * <p>The depth is worked out at the entity's previous-tick and current-tick positions and blended
 * by partial tick, just like the rendered position itself. A real sink is part of the position, so
 * landing on, stepping onto or stepping off a sinking block eases in over one tick the same way.
 *
 * <p>The block that actually holds the feet decides, not just the one under the centre, and a
 * sinking block with a real sink of its own (honey, mud) only adds what is left of the depth, so
 * walking from one sunken block to another keeps a steady eye height. Dropped items lie on top
 * at their real height unless the block is in {@link #HIDES_ITEMS}.
 *
 * <p>Framed / copycat blocks (full ones) sink like their camo, the newest one in a hybrid
 * ({@link CamoSpeed#camo}), when CompatConfig {@code [surface.sinking] copyCamo} is on.
 *
 * <p>Used by {@code mixin/CameraSinkMixin} and {@code mixin/EntitySinkRenderMixin}. Riders sink
 * with their mount.
 */
public final class SinkingRender {

    private SinkingRender() {}

    public static final TagKey<Block> SINKING = BlockTags.create(
            new ResourceLocation(CreativeCompat.NS, "sinking"));

    /**
     * Blocks that swallow dropped items: an item lying on one of these sinks like any other entity.
     * Everywhere else items are drawn on top of the block at their real position (the default), so
     * ItemPhysic's flat-lying items stay visible. Only matters for blocks that also sink.
     */
    public static final TagKey<Block> HIDES_ITEMS = BlockTags.create(
            new ResourceLocation(CreativeCompat.NS, "sinking_hides_items"));

    /** Feet within this of a block's collision top count as standing on it. */
    private static final double EPS = 1.0E-3;

    /** How far to draw {@code entity} below its real position this frame, in blocks (0 = not sinking). */
    public static double depth(Entity entity, float partialTick) {
        if (entity == null) {
            return 0;
        }
        int pixels = CompatConfig.SURFACE.sinking.depthPixels.get();
        if (pixels <= 0) {
            return 0;
        }
        double full = pixels / 16.0;
        boolean item = entity instanceof ItemEntity;
        Entity ground = entity.getRootVehicle();
        double half = ground.getBbWidth() / 2.0;
        double before = depthAt(ground.level, ground.xo, ground.yo, ground.zo, half, full, item);
        double now = depthAt(ground.level, ground.getX(), ground.getY(), ground.getZ(), half, full, item);
        return before == now ? now : Mth.lerp(partialTick, before, now);
    }

    /**
     * Visual depth for feet at (x, y, z). The block under the centre decides when it is the one
     * holding the feet. When it isn't (the centre is already over a lower block, a real-sink block
     * like honey or mud the feet haven't dropped onto yet, or a gap), the blocks under the hitbox
     * corners that do hold the feet decide instead. So the depth only changes once the next block
     * really carries the entity, and crossing from one sunken block to another doesn't pop the
     * camera up for a tick.
     */
    private static double depthAt(Level level, double x, double y, double z, double half, double full,
            boolean item) {
        int by = Mth.floor(y - EPS);
        int cx = Mth.floor(x);
        int cz = Mth.floor(z);
        double centre = support(level, cx, by, cz, y, full, item);
        if (centre >= 0) {
            return centre;
        }
        double w = Math.max(0, half - EPS);
        double best = 0;
        long seen = BlockPos.asLong(cx, by, cz);
        long[] done = new long[4];
        int n = 0;
        for (int i = 0; i < 4; i++) {
            int bx = Mth.floor(x + ((i & 1) == 0 ? -w : w));
            int bz = Mth.floor(z + ((i & 2) == 0 ? -w : w));
            long key = BlockPos.asLong(bx, by, bz);
            if (key == seen || contains(done, n, key)) {
                continue;
            }
            done[n++] = key;
            best = Math.max(best, support(level, bx, by, bz, y, full, item));
        }
        return best;
    }

    private static boolean contains(long[] keys, int n, long key) {
        for (int i = 0; i < n; i++) {
            if (keys[i] == key) {
                return true;
            }
        }
        return false;
    }

    /**
     * -1 if the block at (bx, by, bz) doesn't hold feet at height {@code y}; otherwise the extra
     * visual depth it adds. A full sinking block adds {@code full}. A sinking block that already
     * sinks for real by up to {@code full} (honey 1/16, mud 2/16) adds only the rest, so every
     * sunken block looks equally deep and stepping between them keeps the same eye height.
     */
    private static double support(Level level, int bx, int by, int bz, double y, double full, boolean item) {
        BlockPos pos = new BlockPos(bx, by, bz);
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return -1;
        }
        double top = shape.max(Direction.Axis.Y);
        if (Math.abs(y - (by + top)) > EPS) {
            return -1;
        }
        boolean fullBlock = state.isCollisionShapeFullBlock(level, pos);
        BlockState sinking = sinkingState(level, pos, state, fullBlock);
        if (sinking == null || (item && !sinking.is(HIDES_ITEMS) && !state.is(HIDES_ITEMS))) {
            return 0;
        }
        if (fullBlock) {
            return full;
        }
        // own real sink (honey, mud); slabs, sensors and other low blocks sink deeper than this and stay put
        double own = 1.0 - top;
        return own > EPS && own <= full + EPS ? Math.max(0, full - own) : 0;
    }

    /** The block whose sinking applies at {@code pos}: the block itself, its camo, or null (doesn't sink). */
    private static BlockState sinkingState(Level level, BlockPos pos, BlockState state, boolean fullBlock) {
        if (state.is(SINKING)) {
            return state;
        }
        // framed / copycat blocks: sink like their (newest) camo, if [surface.sinking] copyCamo is on;
        // only frames that are full blocks themselves
        if (fullBlock && state.is(CamoSpeed.TAG) && CompatConfig.SURFACE.sinking.copyCamo.get()) {
            BlockState camo = CamoSpeed.camo(level.getBlockEntity(pos));
            if (camo != null && camo.is(SINKING)) {
                return camo;
            }
        }
        return null;
    }
}
