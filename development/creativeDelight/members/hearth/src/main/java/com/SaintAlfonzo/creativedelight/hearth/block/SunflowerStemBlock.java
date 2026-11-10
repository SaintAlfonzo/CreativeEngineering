package com.SaintAlfonzo.creativedelight.hearth.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

/** N23-16 (fixes B7): a planted seed grows through 8 stem stages, then the stem turns into a sunflower. One seed, one flower. */
public class SunflowerStemBlock extends BushBlock implements BonemealableBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[] SHAPES = new VoxelShape[MAX_AGE + 1];

    static {
        for (int i = 0; i <= MAX_AGE; i++) {
            SHAPES[i] = Block.box(6.0, 0.0, 6.0, 10.0, 2.0 + i * 2.0, 10.0);
        }
    }

    public SunflowerStemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(BlockTags.DIRT) || ground.is(Blocks.FARMLAND);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) < 9 || !ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(8) == 0)) {
            return;
        }
        grow(state, level, pos, 1);
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    private void grow(BlockState state, ServerLevel level, BlockPos pos, int steps) {
        int age = state.getValue(AGE) + steps;
        if (age <= MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age), 2);
        } else if (level.isEmptyBlock(pos.above())) {
            DoublePlantBlock.placeAt(level, Blocks.SUNFLOWER.defaultBlockState(), pos, 3);
        } else if (state.getValue(AGE) != MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, MAX_AGE), 2);
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        return state.getValue(AGE) < MAX_AGE || level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(state, level, pos, 2 + random.nextInt(3));
    }
}
