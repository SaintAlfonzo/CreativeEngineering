package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CreativeCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Honey's wall slide for blocks that aren't honey. Vanilla only slides an entity down a
 * {@link HoneyBlock} because honey's collision box is inset 1/16, so an entity pressed against its
 * side is "inside" the block and gets {@code entityInside}. A full block never calls that from the
 * side, so {@code mixin/WallSlideMixin} calls {@link #afterInsideBlocks} at the end of
 * {@code Entity#checkInsideBlocks}, the exact point honey's slide runs, and this checks the blocks
 * touching the entity's sides itself.
 *
 * <p>Blocks: {@code #creativecompat:wall_slide} (kubejs blocktags.js). The numbers are honey's: an
 * entity falling faster than 0.08 b/tick beside the wall, not on the ground and with its feet below
 * the wall's top, is throttled to 0.05 b/tick (horizontal speed cut the same way when falling faster
 * than 0.13), and its fall distance is reset, so it lands without damage. Honey blocks themselves are
 * skipped (vanilla handles them). Slide sound and block particles come from the server, 1 tick in 5,
 * like honey.
 */
public final class WallSlide {

    private WallSlide() {}

    public static final TagKey<Block> TAG = BlockTags.create(new ResourceLocation(CreativeCompat.NS, "wall_slide"));

    private static final double EPS = 1.0E-3;

    /** Called at TAIL of {@code Entity#checkInsideBlocks} (after the move, like honey's entityInside). */
    public static void afterInsideBlocks(Entity entity) {
        Vec3 v = entity.getDeltaMovement();
        if (v.y >= -0.08 || entity.isOnGround() || entity.noPhysics || entity.isSpectator()) {
            return;
        }
        Level level = entity.level;
        AABB box = entity.getBoundingBox();
        AABB reach = box.inflate(EPS, 0, EPS);
        int x0 = Mth.floor(reach.minX), x1 = Mth.floor(reach.maxX);
        int y0 = Mth.floor(box.minY), y1 = Mth.floor(box.maxY - EPS);
        int z0 = Mth.floor(reach.minZ), z1 = Mth.floor(reach.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(TAG) || state.getBlock() instanceof HoneyBlock) {
                        continue;
                    }
                    if (touchesSide(level, pos, state, entity, box)) {
                        slide(entity, v);
                        effects(level, entity, state);
                        return;
                    }
                }
            }
        }
    }

    /** The entity's side touches the block's collision box, and its feet are below the box's top. */
    private static boolean touchesSide(Level level, BlockPos pos, BlockState state, Entity entity, AABB box) {
        VoxelShape shape = state.getCollisionShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.of(entity));
        if (shape.isEmpty()) {
            return false;
        }
        AABB solid = shape.bounds().move(pos);
        if (entity.getY() > solid.maxY - 1.0E-7) {
            return false; // standing on or above it
        }
        return solid.inflate(EPS, 0, EPS).intersects(box);
    }

    /** HoneyBlock#doSlideMovement. */
    private static void slide(Entity entity, Vec3 v) {
        if (v.y < -0.13) {
            double f = -0.05 / v.y;
            entity.setDeltaMovement(new Vec3(v.x * f, -0.05, v.z * f));
        } else {
            entity.setDeltaMovement(new Vec3(v.x, -0.05, v.z));
        }
        entity.resetFallDistance();
    }

    /** HoneyBlock#maybeDoSlideEffects, with the wall's own block particles. Server only; it broadcasts. */
    private static void effects(Level level, Entity entity, BlockState state) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity)) {
            return;
        }
        if (level.random.nextInt(5) == 0) {
            entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
        }
        if (level.random.nextInt(5) == 0) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    entity.getX(), entity.getY(), entity.getZ(), 5, 0.1, 0.1, 0.1, 0.0);
        }
    }
}
