package com.SaintAlfonzo.creativedelight.hearth.nest;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

/** Ground nest with the 1.4.1 behaviour: items dropped or used on it go in, an empty hand takes the last one out (sneaking, all of them). */
public class NestBlock extends Block implements EntityBlock {
    public static final BooleanProperty GENERATED = BooleanProperty.create("generated");
    private static final VoxelShape OUTER = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
    private static final VoxelShape INSIDE = Block.box(4.0D, 1.0D, 4.0D, 12.0D, 3.0D, 12.0D);
    private static final VoxelShape SHAPE = Shapes.join(OUTER, INSIDE, BooleanOp.ONLY_FIRST);

    public NestBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(GENERATED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NestBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GENERATED);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return !level.getFluidState(below).isSource() && !level.getBlockState(below).is(Blocks.AIR);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        if (!state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity item) || !item.isAlive() || item.hasPickUpDelay() || item.getItem().isEmpty()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            ItemStack held = item.getItem();
            ItemStack remaining = nest.insert(held.copy());
            if (remaining.getCount() == held.getCount()) {
                nudgeAside(item, pos);
            } else if (remaining.isEmpty()) {
                item.discard();
            } else {
                item.setItem(remaining);
            }
        }
    }

    private static void nudgeAside(ItemEntity item, BlockPos pos) {
        double dx = item.getX() - (pos.getX() + 0.5D);
        double dz = item.getZ() - (pos.getZ() + 0.5D);
        if (dx * dx + dz * dz < 1.0E-4D) {
            dx = item.level.random.nextDouble() - 0.5D;
            dz = item.level.random.nextDouble() - 0.5D;
        }
        double length = Math.sqrt(dx * dx + dz * dz);
        item.setDeltaMovement(item.getDeltaMovement().add(dx / length * 0.06D, 0.0D, dz / length * 0.06D));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof NestBlockEntity nest)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            ItemStack remaining = nest.insert(stack.copy());
            if (remaining.getCount() == stack.getCount()) {
                return InteractionResult.PASS;
            }
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, remaining);
            }
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.6F, 1.2F);
            return InteractionResult.CONSUME;
        }
        if (hand != InteractionHand.MAIN_HAND || nest.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        do {
            ItemStack taken = nest.takeLast();
            if (taken.isEmpty()) {
                break;
            }
            player.getInventory().placeItemBackInInventory(taken);
        } while (player.isShiftKeyDown());
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.4F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NestBlockEntity nest) {
            nest.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof NestBlockEntity nest ? ItemHandlerHelper.calcRedstoneFromInventory(nest.getInventory()) : 0;
    }
}
