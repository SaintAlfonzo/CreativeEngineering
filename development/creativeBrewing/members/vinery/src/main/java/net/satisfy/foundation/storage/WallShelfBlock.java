package net.satisfy.foundation.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.foundation.util.ShapeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WallShelfBlock extends StorageBlock {
    public static final int SIZE = 9;
    public static final VoxelShape DEFAULT_SHAPE = Shapes.or(Shapes.box(0, 0.25, 0.5, 1, 0.3125, 1), Shapes.box(0, 0.125, 0.875, 1, 0.25, 1));

    private final Supplier<? extends BlockEntityType<?>> blockEntityType;
    private final ResourceLocation type;
    private final Predicate<ItemStack> canInsert;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public WallShelfBlock(Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType, ResourceLocation type, Predicate<ItemStack> canInsert) {
        this(properties, blockEntityType, type, canInsert, DEFAULT_SHAPE);
    }

    public WallShelfBlock(Properties properties, Supplier<? extends BlockEntityType<?>> blockEntityType, ResourceLocation type, Predicate<ItemStack> canInsert, VoxelShape northShape) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.type = type;
        this.canInsert = canInsert;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            shapes.put(direction, ShapeUtil.rotateShape(Direction.NORTH, direction, northShape));
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        Direction facing = side.getAxis().isHorizontal() ? side : context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public BlockEntityType<?> blockEntityType() {
        return blockEntityType.get();
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public ResourceLocation type() {
        return type;
    }

    @Override
    public Direction[] unAllowedDirections() {
        return new Direction[]{Direction.DOWN};
    }

    @Override
    public boolean canInsertStack(ItemStack stack) {
        return canInsert.test(stack);
    }

    @Override
    public int getSection(Float x, Float y) {
        return SIZE - 1 - Mth.clamp((int) (x * SIZE), 0, SIZE - 1);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return Block.isFaceFull(level.getBlockState(wall).getShape(level, wall), facing);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
