package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
        Entity ground = entity.getRootVehicle();
        double before = depthAt(ground.level, ground.xo, ground.yo, ground.zo, full);
        double now = depthAt(ground.level, ground.getX(), ground.getY(), ground.getZ(), full);
        return before == now ? now : Mth.lerp(partialTick, before, now);
    }

    /** {@code full} if feet at (x, y, z) rest on the top face of a full sinking block, else 0. */
    private static double depthAt(Level level, double x, double y, double z, double full) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y - 1.0E-3), Mth.floor(z));
        // feet must be on this block's top face: not jumping, and not on a slab/carpet/layer above it
        if (Math.abs(y - (pos.getY() + 1)) > 1.0E-3) {
            return 0;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.isCollisionShapeFullBlock(level, pos)) {
            return 0;
        }
        if (state.is(SINKING)) {
            return full;
        }
        // framed / copycat blocks: sink like their (newest) camo, if [surface.sinking] copyCamo is on
        if (state.is(CamoSpeed.TAG) && CompatConfig.SURFACE.sinking.copyCamo.get()) {
            BlockState camo = CamoSpeed.camo(level.getBlockEntity(pos));
            if (camo != null && camo.is(SINKING)) {
                return full;
            }
        }
        return 0;
    }
}
