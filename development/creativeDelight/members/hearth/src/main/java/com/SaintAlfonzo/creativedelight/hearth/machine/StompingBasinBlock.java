package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Stomping Basin, a manual juicer (D12, pending the eval): landing in it after a fall of at least 0.3 blocks runs one
 * recipe. Four in a square form a 2x2 multiblock. No block entity ticking; everything happens on the landing.
 */
public class StompingBasinBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<MultiblockPart> PART = EnumProperty.create("multiblock_part", MultiblockPart.class);
    private static final VoxelShape FLOOR = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape WEST = box(0, 1, 0, 2, 12, 16);
    private static final VoxelShape EAST = box(14, 1, 0, 16, 12, 16);
    private static final VoxelShape NORTH = box(2, 1, 0, 14, 12, 2);
    private static final VoxelShape SOUTH = box(2, 1, 14, 14, 12, 16);
    private static final VoxelShape SHAPE = Shapes.or(FLOOR, WEST, EAST, NORTH, SOUTH);
    private static final VoxelShape SHAPE_NW = Shapes.or(FLOOR, WEST, box(2, 1, 0, 16, 12, 2));
    private static final VoxelShape SHAPE_NE = Shapes.or(FLOOR, EAST, box(0, 1, 0, 14, 12, 2));
    private static final VoxelShape SHAPE_SW = Shapes.or(FLOOR, WEST, box(2, 1, 14, 16, 12, 16));
    private static final VoxelShape SHAPE_SE = Shapes.or(FLOOR, EAST, box(0, 1, 14, 14, 12, 16));
    private static final float MIN_FALL = 0.3F;
    private static final float RIM_Y = 0.75F;
    private static final Set<UUID> AIRBORNE = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Double> PEAK = new ConcurrentHashMap<>();

    public StompingBasinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, MultiblockPart.NONE));
    }

    public static void clearAirborneTracking() {
        AIRBORNE.clear();
        PEAK.clear();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        MultiblockPart part = state.getValue(PART);
        if (part == MultiblockPart.NONE || !(level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin)) return SHAPE;
        BlockPos controller = part == MultiblockPart.CONTROLLER ? pos : basin.controllerPos();
        if (controller == null) return SHAPE;
        int dx = pos.getX() - controller.getX();
        int dz = pos.getZ() - controller.getZ();
        if (dx == 0 && dz == 0) return SHAPE_NW;
        if (dx == 1 && dz == 0) return SHAPE_NE;
        if (dx == 0 && dz == 1) return SHAPE_SW;
        return dx == 1 && dz == 1 ? SHAPE_SE : SHAPE;
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
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == MultiblockPart.NONE ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living) || living.getY() - pos.getY() >= RIM_Y) return;
        UUID id = living.getUUID();
        if (!living.isOnGround()) {
            AIRBORNE.add(id);
            PEAK.merge(id, living.getY(), Math::max);
            return;
        }
        if (AIRBORNE.remove(id)) {
            Double peak = PEAK.remove(id);
            double fall = (peak != null ? peak : living.getY()) - living.getY();
            if (fall >= MIN_FALL && level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin) basin.tryProcess(living);
        } else {
            PEAK.remove(id);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin ? MachineFluids.comparator(basin.tank()) : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (player.isShiftKeyDown()) basin.extractAll(player);
            else basin.extractOne(player);
            return InteractionResult.CONSUME;
        }
        if (MachineFluids.useOnTank(level, pos, player, hand, basin.tank())) return InteractionResult.CONSUME;
        ItemStack rest = basin.insert(held.copy());
        if (rest.getCount() < held.getCount()) {
            player.setItemInHand(hand, rest);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 0.8F + level.random.nextFloat() * 0.4F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (level.isClientSide || state.getValue(PART) != MultiblockPart.NONE) return;
        for (int dx = 0; dx >= -1; dx--) {
            for (int dz = 0; dz >= -1; dz--) {
                BlockPos corner = pos.offset(dx, 0, dz);
                if (canForm(level, corner)) {
                    form(level, corner);
                    return;
                }
            }
        }
    }

    private boolean canForm(Level level, BlockPos corner) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockState state = level.getBlockState(corner.offset(dx, 0, dz));
                if (!state.is(this) || state.getValue(PART) != MultiblockPart.NONE) return false;
            }
        }
        return true;
    }

    private void form(Level level, BlockPos nw) {
        BlockPos ne = nw.east();
        BlockPos sw = nw.south();
        BlockPos se = nw.east().south();
        StompingBasinBlockEntity controller = basin(level, nw);
        StompingBasinBlockEntity neBasin = basin(level, ne);
        StompingBasinBlockEntity swBasin = basin(level, sw);
        StompingBasinBlockEntity seBasin = basin(level, se);
        if (controller == null || neBasin == null || swBasin == null || seBasin == null) return;
        level.setBlock(nw, level.getBlockState(nw).setValue(PART, MultiblockPart.CONTROLLER), 3);
        level.setBlock(ne, level.getBlockState(ne).setValue(PART, MultiblockPart.MEMBER), 3);
        level.setBlock(sw, level.getBlockState(sw).setValue(PART, MultiblockPart.MEMBER), 3);
        level.setBlock(se, level.getBlockState(se).setValue(PART, MultiblockPart.MEMBER), 3);
        controller.formAsController(neBasin, swBasin, seBasin);
        neBasin.formAsMember(nw);
        swBasin.formAsMember(nw);
        seBasin.formAsMember(nw);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            MultiblockPart part = state.getValue(PART);
            if (!level.isClientSide && part != MultiblockPart.NONE) dissolve(level, pos, part);
            if (part != MultiblockPart.MEMBER && level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin) basin.dropContents();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    private void dissolve(Level level, BlockPos broken, MultiblockPart brokenPart) {
        BlockPos controllerPos;
        if (brokenPart == MultiblockPart.CONTROLLER) {
            controllerPos = broken;
        } else {
            StompingBasinBlockEntity member = basin(level, broken);
            if (member == null || member.controllerPos() == null) return;
            controllerPos = member.controllerPos();
        }
        List<BlockPos> quadrants = List.of(controllerPos, controllerPos.east(), controllerPos.south(), controllerPos.east().south());
        List<StompingBasinBlockEntity> survivors = new ArrayList<>();
        for (BlockPos p : quadrants) {
            if (!p.equals(broken) && basin(level, p) != null) survivors.add(basin(level, p));
        }
        StompingBasinBlockEntity controller = basin(level, controllerPos);
        if (controller != null) controller.dissolve(survivors);
        for (BlockPos p : quadrants) {
            if (p.equals(broken)) continue;
            BlockState state = level.getBlockState(p);
            if (state.is(this) && state.getValue(PART) != MultiblockPart.NONE) {
                level.setBlock(p, state.setValue(PART, MultiblockPart.NONE), 3);
                StompingBasinBlockEntity basin = basin(level, p);
                if (basin != null) basin.dissolveAsMember();
            }
        }
    }

    @Nullable
    private static StompingBasinBlockEntity basin(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof StompingBasinBlockEntity basin ? basin : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StompingBasinBlockEntity(pos, state);
    }
}
