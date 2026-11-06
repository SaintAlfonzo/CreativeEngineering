package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Slots laid out side by side across a block's facing (1.3.0 BlockSlot): which slot a click hit, and render offsets. */
public interface BlockSlot {

    AABB getBox();

    int size();

    default float getOffset(int index) {
        return -((index + 0.5F) / size() - 0.5F) * 0.8F;
    }

    default int getSlotForHitting(BlockHitResult hit, Level level) {
        if (hit.getType() != HitResult.Type.BLOCK) {
            return -1;
        }
        Vec3 at = hit.getLocation();
        if (!getBox().contains(at)) {
            return -1;
        }
        Direction facing = level.getBlockState(hit.getBlockPos()).getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        BlockPos pos = hit.getBlockPos();
        double index = switch (facing) {
            case NORTH -> at.x - pos.getX();
            case SOUTH -> 1.0 - (at.x - pos.getX());
            case EAST -> at.z - pos.getZ();
            case WEST -> 1.0 - (at.z - pos.getZ());
            default -> 0.0;
        };
        int ans = (int) (index * size());
        return ans >= 0 && ans < size() ? ans : -1;
    }
}
