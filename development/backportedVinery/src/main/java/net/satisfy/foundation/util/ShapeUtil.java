package net.satisfy.foundation.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.Optional;

/** VoxelShape and placement helpers. */
public final class ShapeUtil {
    private ShapeUtil() {
    }

    /** Checks the block BELOW the pos. */
    public static boolean isFullAndSolid(LevelReader levelReader, BlockPos blockPos) {
        return isFaceFull(levelReader, blockPos) && isSolid(levelReader, blockPos);
    }

    public static boolean isFaceFull(LevelReader levelReader, BlockPos blockPos) {
        BlockPos belowPos = blockPos.below();
        return Block.isFaceFull(levelReader.getBlockState(belowPos).getBlockSupportShape(levelReader, belowPos), Direction.UP);
    }

    public static boolean isSolid(LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).getMaterial().isSolid();
    }

    /** Rotates a shape around y, clockwise from {@code from} to {@code to}. Build shapes facing north, then rotate. */
    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;

        for (int i = 0; i < times; ++i) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.joinUnoptimized(buffer[1], Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX), BooleanOp.OR));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    /**
     * Hit position on the front face as (x, y), 0-1, seen from the front.
     * Empty if a forbidden or side face was hit.
     */
    public static Optional<Tuple<Float, Float>> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction, Direction[] unAllowedDirections) {
        Direction hitFace = blockHitResult.getDirection();
        if (Arrays.asList(unAllowedDirections).contains(hitFace)) {
            return Optional.empty();
        } else if (direction != hitFace && hitFace != Direction.UP && hitFace != Direction.DOWN) {
            return Optional.empty();
        }
        BlockPos blockPos = blockHitResult.getBlockPos().relative(hitFace);
        Vec3 vec3 = blockHitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        float d = (float) vec3.x();
        float f = (float) vec3.z();
        float y = (float) vec3.y();
        if (hitFace == Direction.UP || hitFace == Direction.DOWN) {
            hitFace = direction;
        }

        return switch (hitFace) {
            case NORTH -> Optional.of(new Tuple<>((float) (1.0 - d), y));
            case SOUTH -> Optional.of(new Tuple<>(d, y));
            case WEST -> Optional.of(new Tuple<>(f, y));
            case EAST -> Optional.of(new Tuple<>((float) (1.0 - f), y));
            default -> Optional.empty();
        };
    }
}
