package com.SaintAlfonzo.creativedelight.hearth.machine;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bottle Crate: a slab-type block, each half holds 9 bottles or eggs on a 3x3 grid (no tall-bottle rule in this port). */
public class CrateBlock extends Block implements EntityBlock {
    public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty STACKED = BooleanProperty.create("stacked");
    private static final VoxelShape SHAPE_BOTTOM = Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 4, 0, 16, 8, 1), Block.box(0, 4, 15, 16, 8, 16),
            Block.box(0, 4, 0, 1, 8, 16), Block.box(15, 4, 0, 16, 8, 16));
    private static final VoxelShape SHAPE_TOP = Shapes.or(Block.box(0, 8, 0, 16, 12, 16), Block.box(0, 12, 0, 16, 16, 1), Block.box(0, 12, 15, 16, 16, 16),
            Block.box(0, 12, 0, 1, 16, 16), Block.box(15, 12, 0, 16, 16, 16));
    private static final VoxelShape SHAPE_DOUBLE = Shapes.or(SHAPE_BOTTOM, SHAPE_TOP);

    public CrateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, SlabType.BOTTOM).setValue(FACING, Direction.NORTH).setValue(STACKED, false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, FACING, STACKED);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.getValue(TYPE) == SlabType.BOTTOM && direction == Direction.UP
                ? state.setValue(STACKED, neighbour.is(this))
                : super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        SlabType existing = state.getValue(TYPE);
        if (existing == SlabType.DOUBLE || !context.getItemInHand().is(asItem()) || state.getValue(STACKED)) return false;
        Direction face = context.getClickedFace();
        if (face == Direction.UP) return existing == SlabType.BOTTOM;
        if (face == Direction.DOWN) return existing == SlabType.TOP;
        double hitY = context.getClickLocation().y - context.getClickedPos().getY();
        return existing == SlabType.BOTTOM ? hitY > 0.5 : hitY <= 0.5;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState existing = context.getLevel().getBlockState(pos);
        if (existing.is(this) && existing.getValue(TYPE) != SlabType.DOUBLE) return existing.setValue(TYPE, SlabType.DOUBLE).setValue(STACKED, false);
        Direction face = context.getClickedFace();
        SlabType type;
        if (face == Direction.DOWN) {
            type = SlabType.TOP;
        } else if (face == Direction.UP) {
            type = SlabType.BOTTOM;
        } else {
            type = context.getClickLocation().y - pos.getY() > 0.5 ? SlabType.TOP : SlabType.BOTTOM;
        }
        boolean stacked = type == SlabType.BOTTOM && context.getLevel().getBlockState(pos.above()).is(this);
        return defaultBlockState().setValue(TYPE, type).setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(STACKED, stacked);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(TYPE)) {
            case BOTTOM -> SHAPE_BOTTOM;
            case TOP -> SHAPE_TOP;
            case DOUBLE -> SHAPE_DOUBLE;
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(TYPE) == SlabType.DOUBLE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CrateBlockEntity crate)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && held.isEmpty() && hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            pickUp(state, level, pos, player, crate);
            return InteractionResult.CONSUME;
        }
        int slot = slotFromHit(state, hit);
        if (slot < 0 || slot >= CrateBlockEntity.TOTAL || (slot >= CrateBlockEntity.PER_HALF && state.getValue(TYPE) != SlabType.DOUBLE)) return InteractionResult.PASS;
        ItemStack current = crate.getItem(slot);
        if (!current.isEmpty()) {
            if (!level.isClientSide) {
                player.getInventory().placeItemBackInInventory(current.copy());
                crate.setItem(slot, ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty() && CrateBlockEntity.accepts(held)) {
            if (!level.isClientSide) {
                crate.setItem(slot, held);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    private void pickUp(BlockState state, Level level, BlockPos pos, Player player, CrateBlockEntity crate) {
        ItemStack drop;
        if (state.getValue(TYPE) != SlabType.DOUBLE) {
            drop = halfDrop(crate, 0);
            level.removeBlock(pos, false);
        } else {
            Vec3 eyes = player.getEyePosition();
            Vec3 target = eyes.add(player.getLookAngle().scale(player.getReachDistance() + 1.0));
            BlockHitResult look = level.clip(new ClipContext(eyes, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            boolean top = look.getLocation().y - pos.getY() >= 0.5;
            int pickup = top ? CrateBlockEntity.PER_HALF : 0;
            int remain = top ? 0 : CrateBlockEntity.PER_HALF;
            drop = halfDrop(crate, pickup);
            for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) crate.items().set(i, crate.getItem(remain + i).copy());
            for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) crate.items().set(CrateBlockEntity.PER_HALF + i, ItemStack.EMPTY);
            level.setBlock(pos, state.setValue(TYPE, top ? SlabType.BOTTOM : SlabType.TOP), 3);
            crate.sync();
        }
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
        if (!player.addItem(drop)) popResource(level, pos, drop);
    }

    /** Which 3x3 slot a click lands on, in the crate's own frame; -1 if on the rim or under the floor. */
    private static int slotFromHit(BlockState state, BlockHitResult hit) {
        Vec3 local = hit.getLocation().subtract(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
        double x = local.x;
        double z = local.z;
        switch (state.getValue(FACING)) {
            case WEST -> {
                x = 1.0 - local.z;
                z = local.x;
            }
            case SOUTH -> {
                x = 1.0 - local.x;
                z = 1.0 - local.z;
            }
            case EAST -> {
                x = local.z;
                z = 1.0 - local.x;
            }
            default -> {
            }
        }
        int base;
        switch (state.getValue(TYPE)) {
            case BOTTOM -> {
                if (local.y < 0.25) return -1;
                base = 0;
            }
            case TOP -> {
                if (local.y < 0.75) return -1;
                base = 0;
            }
            default -> {
                if (local.y >= 0.75) base = CrateBlockEntity.PER_HALF;
                else if (local.y >= 0.25 && local.y < 0.5) base = 0;
                else return -1;
            }
        }
        int col = coord(x);
        int row = coord(z);
        return col >= 0 && row >= 0 ? base + row * 3 + col : -1;
    }

    private static int coord(double value) {
        int pixel = (int) (value * 16.0);
        if (pixel < 1 || pixel > 14) return -1;
        return (pixel - 1) % 5 < 4 ? (pixel - 1) / 5 : -1;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CrateBlockEntity crate)) return 0;
        int count = 0;
        int max = state.getValue(TYPE) == SlabType.DOUBLE ? CrateBlockEntity.TOTAL : CrateBlockEntity.PER_HALF;
        for (int i = 0; i < max; i++) {
            if (!crate.getItem(i).isEmpty()) count++;
        }
        return Math.min(count, 15);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CrateBlockEntity crate) {
            drops.add(halfDrop(crate, 0));
            if (state.getValue(TYPE) == SlabType.DOUBLE) drops.add(halfDrop(crate, CrateBlockEntity.PER_HALF));
        } else {
            drops.add(new ItemStack(this));
        }
        return drops;
    }

    private ItemStack halfDrop(CrateBlockEntity crate, int offset) {
        ItemStack drop = new ItemStack(this);
        NonNullList<ItemStack> half = NonNullList.withSize(CrateBlockEntity.PER_HALF, ItemStack.EMPTY);
        boolean any = false;
        for (int i = 0; i < CrateBlockEntity.PER_HALF; i++) {
            half.set(i, crate.getItem(offset + i).copy());
            any |= !half.get(i).isEmpty();
        }
        if (any) {
            CompoundTag tag = new CompoundTag();
            ContainerHelper.saveAllItems(tag, half);
            BlockItem.setBlockEntityData(drop, HHMachines.CRATE_BE.get(), tag);
        }
        return drop;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrateBlockEntity(pos, state);
    }
}
