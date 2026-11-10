package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

/**
 * Tilled End Stone (N23-12): farmland rules, but it reverts to end stone instead of dirt, and only plants in
 * #hearthandharvest:grows_on_tilled_end_stone may root in it. That list waits for the ender crop audit.
 */
public class TilledEndStoneBlock extends FarmBlock {
    public TilledEndStoneBlock(Properties properties) {
        super(properties);
    }

    private static void revert(BlockState state, Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, pushEntitiesUp(state, Blocks.END_STONE.defaultBlockState(), level, pos));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            revert(state, level, pos);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int moisture = state.getValue(MOISTURE);
        if (!nearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!level.getBlockState(pos.above()).is(HHTags.GROWS_ON_TILLED_END_STONE)) {
                revert(state, level, pos);
            }
        } else if (moisture < MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
        }
    }

    private static boolean nearWater(LevelReader level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(near).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        if (!level.isClientSide && ForgeHooks.onFarmlandTrample(level, pos, Blocks.END_STONE.defaultBlockState(), distance, entity)) {
            revert(state, level, pos);
        }
        entity.causeFallDamage(distance, 1.0F, net.minecraft.world.damagesource.DamageSource.FALL);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        return plantable.getPlant(level, pos.relative(facing)).is(HHTags.GROWS_ON_TILLED_END_STONE);
    }
}
