package com.sammy.minersdelight.content.block;

import com.sammy.minersdelight.registry.MDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.WildCropBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

/**
 * Wild cave carrots: grow on stone, dirt or sand ({@link #STONE} picks the stone-backed texture when placed on stone).
 * On Rich Soil they spread slowly by random tick, now and then as Gossypium instead.
 */
public class WildCaveCarrotBlock extends WildCropBlock {
    public static final BooleanProperty STONE = BooleanProperty.create("stone");

    public WildCaveCarrotBlock(Properties properties) {
        super(MobEffects.DIG_SPEED, 10, properties);
        registerDefaultState(stateDefinition.any().setValue(STONE, false));
    }

    public static boolean mayGrowOn(BlockState ground) {
        return ground.is(BlockTags.BASE_STONE_OVERWORLD) || ground.is(BlockTags.DIRT) || ground.is(BlockTags.SAND);
    }

    /** The state to place at {@code pos}, with {@link #STONE} set from the block below. */
    public static BlockState onGround(BlockState state, BlockGetter level, BlockPos pos) {
        return state.setValue(STONE, level.getBlockState(pos.below()).is(BlockTags.BASE_STONE_OVERWORLD));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return mayGrowOn(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 4 ticks in 20 try to spread, and one of those four spreads Gossypium.
        int roll = random.nextInt(20);
        if (roll > 3 || !level.getBlockState(pos.below()).is(ModBlocks.RICH_SOIL.get())) {
            return;
        }
        int nearby = 0;
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(near).is(this) && ++nearby >= 5) {
                return;
            }
        }
        BlockState spread = roll == 1 ? MDBlocks.GOSSYPIUM.get().defaultBlockState() : state;
        BlockPos from = pos;
        BlockPos target = randomNear(from, random);
        for (int step = 0; step < 4; step++) {
            if (level.isEmptyBlock(target) && spread.canSurvive(level, target)) {
                from = target;
            }
            target = randomNear(from, random);
        }
        if (level.isEmptyBlock(target) && spread.canSurvive(level, target)) {
            level.setBlock(target, onGround(spread, level, target), Block.UPDATE_CLIENTS);
        }
    }

    private static BlockPos randomNear(BlockPos pos, RandomSource random) {
        return pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : onGround(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STONE);
    }
}
