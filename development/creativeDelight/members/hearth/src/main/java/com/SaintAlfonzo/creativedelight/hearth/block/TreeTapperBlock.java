package com.SaintAlfonzo.creativedelight.hearth.block;

import com.SaintAlfonzo.creativedelight.hearth.HHTags;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

/** Wall block on a tappable log. Upstream's waterlogged state is dropped: it never had a matching fluid state. */
public class TreeTapperBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TreeTapperBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TreeTapperBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> Block.box(6.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D);
            case SOUTH -> Block.box(3.0D, 0.0D, 6.0D, 13.0D, 16.0D, 16.0D);
            case WEST -> Block.box(0.0D, 0.0D, 3.0D, 10.0D, 16.0D, 13.0D);
            default -> Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 10.0D);
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction dir = state.getValue(FACING);
        return level.getBlockState(pos.relative(dir)).isFaceSturdy(level, pos.relative(dir), dir.getOpposite());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                BlockState state = defaultBlockState().setValue(FACING, direction);
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
            }
        }
        return null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TreeTapperBlockEntity tapper ? tapper.comparator() : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, HHBlockEntities.TREE_TAPPER.get(), TreeTapperBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection()) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /** Vanilla honey drips stand in for upstream's own sap particle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction dir = state.getValue(FACING);
        if (random.nextFloat() >= 0.1F || !level.getBlockState(pos.relative(dir)).is(HHTags.TAPPABLE)) return;
        level.addParticle(ParticleTypes.DRIPPING_HONEY, pos.getX() + 0.5D + dir.getStepX() * 0.1D, pos.getY() + 0.75D, pos.getZ() + 0.5D + dir.getStepZ() * 0.1D, 0.0D, 0.0D, 0.0D);
    }
}
