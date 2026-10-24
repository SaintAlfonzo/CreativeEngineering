package com.sammy.minersdelight.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

/** Gossypium (cotton flower): the same ground rules and stone look as the wild cave carrots. */
public class GossypiumBlock extends FlowerBlock {
    public GossypiumBlock(Properties properties) {
        super(MobEffects.DIG_SPEED, 10, properties);
        registerDefaultState(stateDefinition.any().setValue(WildCaveCarrotBlock.STONE, false));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return WildCaveCarrotBlock.mayGrowOn(state);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : WildCaveCarrotBlock.onGround(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WildCaveCarrotBlock.STONE);
    }
}
