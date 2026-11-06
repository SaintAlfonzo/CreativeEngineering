package com.SaintAlfonzo.creativeengineering.fan;

import java.util.Optional;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Direction helpers for the dragon-head fan catalyst, ported from Create: Halitosis
 * ({@code com.outurnate.createhalitosis.CatalystUtils}) to Create 0.5.1.i / MC 1.19.2.
 *
 * <p>A dragon head is only a valid catalyst when it points along a cardinal axis
 * <em>away</em> from an Encased Fan sitting directly behind it, i.e. the fan blows
 * through the back of the skull and out of its mouth.
 */
public final class CatalystUtils {

    private CatalystUtils() {}

    /**
     * The horizontal direction a placed skull faces, or empty when it is a wall
     * skull on a non-horizontal face / a standing skull rotated off a cardinal.
     */
    public static Optional<Direction> getDirection(BlockState state) {
        Optional<Direction> wall = state.getOptionalValue(WallSkullBlock.FACING)
                .filter(d -> d == Direction.NORTH || d == Direction.EAST
                        || d == Direction.SOUTH || d == Direction.WEST);
        if (wall.isPresent()) {
            return wall;
        }
        return state.getOptionalValue(SkullBlock.ROTATION).flatMap(rotation -> switch (rotation) {
            case 0 -> Optional.of(Direction.NORTH);
            case 4 -> Optional.of(Direction.EAST);
            case 8 -> Optional.of(Direction.SOUTH);
            case 12 -> Optional.of(Direction.WEST);
            default -> Optional.empty();
        });
    }

    /**
     * True when the block behind {@code pos} (opposite {@code facing}) is a running
     * air-current source blowing in {@code facing}: the stricter "the fan is
     * actually driving this head right now" test used to gate the mouth animation.
     */
    public static boolean testFan(Level level, BlockPos pos, Direction facing) {
        BlockEntity behind = level.getBlockEntity(pos.relative(facing.getOpposite()));
        if (behind instanceof IAirCurrentSource source) {
            AirCurrent current = source.getAirCurrent();
            if (current != null) {
                return current.direction == facing && current.maxDistance != 0.0F;
            }
        }
        return false;
    }
}
