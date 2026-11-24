package com.SaintAlfonzo.creativedelight.enders.block;

import com.SaintAlfonzo.creativedelight.enders.EAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Void Pepper Sprout (1.1.4 VoidpepperBush): a vine on the faces of the End plant soil around it. It does not spread
 * (creative-delight-members #4): until it fruits, a random tick that passes the soil tier's chance fruits it
 * ({@code pepper}). {@code spreading} stays in the state definition for the blockstate files and saved worlds; it is
 * set false with the fruit and drives nothing else.
 * <p>
 * The face properties keep 1.1.4's meaning, which the blockstate and shapes depend on: {@code up} and {@code down} name
 * the soil side; {@code north}, {@code south}, {@code east} and {@code west} name the side away from the soil (south =
 * soil to the north). 1.1.4's block entity was never attached to the block and is not ported.
 */
public class VoidpepperBlock extends Block {
    public static final BooleanProperty SPREADING = BooleanProperty.create("spreading");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty PEPPER = BooleanProperty.create("pepper");

    /** Face property, the direction of the soil it sits on, and its 1px shape (1.1.4 values). */
    private record Face(BooleanProperty prop, Direction soil, VoxelShape shape) {
    }

    private static final Face[] FACES = {
            new Face(DOWN, Direction.DOWN, Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)),
            new Face(UP, Direction.UP, Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0)),
            new Face(SOUTH, Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0)),
            new Face(NORTH, Direction.SOUTH, Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0)),
            new Face(EAST, Direction.WEST, Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0)),
            new Face(WEST, Direction.EAST, Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0))};
    private static final VoxelShape NO_FACE_SHAPE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);

    public VoidpepperBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPREADING, true).setValue(EAST, false).setValue(WEST, false)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(UP, false).setValue(DOWN, false).setValue(PEPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPREADING, EAST, WEST, NORTH, SOUTH, UP, DOWN, PEPPER);
    }

    /** A new sprout's state at {@code pos}: one face on each neighboring soil block. */
    private BlockState withFaces(BlockGetter level, BlockPos pos) {
        BlockState state = defaultBlockState();
        for (Face f : FACES) {
            state = state.setValue(f.prop, EndSoil.isSoil(level.getBlockState(pos.relative(f.soil))));
        }
        return state;
    }

    /**
     * Soil this sprout is attached to: 0 none, 1 slow, 2 fast. The faces it has, or the block below when it has none.
     */
    private static int soilTier(BlockState state, BlockGetter level, BlockPos pos) {
        boolean anyFace = false;
        int tier = 0;
        for (Face f : FACES) {
            if (!state.getValue(f.prop)) continue;
            anyFace = true;
            tier = Math.max(tier, tierOf(level.getBlockState(pos.relative(f.soil))));
        }
        return anyFace ? tier : tierOf(level.getBlockState(pos.below()));
    }

    private static int tierOf(BlockState soil) {
        return EndSoil.isFast(soil) ? 2 : EndSoil.isSoil(soil) ? 1 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withFaces(context.getLevel(), context.getClickedPos());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return soilTier(state, level, pos) > 0;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos pos, BlockPos facingPos) {
        return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(PEPPER);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PEPPER)) return;
        int tier = soilTier(state, level, pos);
        if (tier == 0) return;
        double chance = tier == 2 ? EAConfig.VOIDPEPPER_TICK_FAST.get() : EAConfig.VOIDPEPPER_TICK_SLOW.get();
        if (random.nextDouble() >= chance) return;
        level.setBlock(pos, state.setValue(PEPPER, true).setValue(SPREADING, false), Block.UPDATE_CLIENTS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (Face f : FACES) {
            if (state.getValue(f.prop)) shape = Shapes.or(shape, f.shape);
        }
        return shape.isEmpty() ? NO_FACE_SHAPE : shape;
    }
}
