package com.SaintAlfonzo.creativedelight.oceanic.content;

import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The sea grape crop (ocean_delight.md 5.6): a column that grows up through water like kelp. It stands in still water, on
 * another sea grape plant or where seagrass could stand (not on kelp). On a random tick, 1 time in 10, it grows one block
 * into the water above; the third plant of a column grows a sea grape fruit on top instead. Bone meal adds one plant
 * (never a fruit). {@code position} is 1 when a plant or fruit sits on top (the model joins up), else 0.
 */
public class SeaGrapePlantBlock extends WaterloggedPlantBlock implements BonemealableBlock {
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, 1);
    private static final VoxelShape TOP = box(0, 0, 0, 16, 12, 16);
    private static final VoxelShape JOINED = box(0, 0, 0, 16, 16, 16);

    public SeaGrapePlantBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POSITION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POSITION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POSITION) == 1 ? JOINED : TOP;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!level.getFluidState(pos).isSource() || !inWater(level, pos)) return false;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) || (seagrassCouldStand(level, pos) && !below.is(Blocks.KELP));
    }

    private boolean joined(Level level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.is(this) || above.is(OceanicBlocks.SEA_GRAPE_FRUIT.get());
    }

    private void updatePosition(BlockState state, Level level, BlockPos pos) {
        int position = joined(level, pos) ? 1 : 0;
        if (state.getValue(POSITION) != position) level.setBlock(pos, state.setValue(POSITION, position), 3);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        updatePosition(state, level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, neighbor, from, moving);
        updatePosition(state, level, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= 0.1F) return;
        BlockPos up = pos.above();
        if (joined(level, pos) || !openWater(level, up)) return;
        boolean third = level.getBlockState(pos.below()).is(this) && level.getBlockState(pos.below(2)).is(this);
        BlockState grown = third
                ? OceanicBlocks.SEA_GRAPE_FRUIT.get().defaultBlockState().setValue(WATERLOGGED, true)
                : defaultBlockState().setValue(WATERLOGGED, true);
        level.setBlock(up, grown, 3);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean client) {
        return level instanceof LevelReader reader && openWater(reader, pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos.above(), defaultBlockState().setValue(WATERLOGGED, true), 3);
    }
}
