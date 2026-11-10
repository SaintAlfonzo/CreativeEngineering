package com.SaintAlfonzo.creativedelight.hearth.machine;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Water Trough block, {@code hearthandharvest:trough} (N22: id kept, name changed). */
public class TroughBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(box(0, 0, 0, 4, 2, 4), box(12, 0, 0, 16, 2, 4), box(0, 0, 12, 4, 2, 16), box(12, 0, 12, 16, 2, 16),
            box(2, 0, 2, 14, 2, 14), box(0, 2, 0, 16, 8, 2), box(0, 2, 14, 16, 8, 16), box(0, 2, 2, 2, 8, 14), box(14, 2, 2, 16, 8, 14));

    public TroughBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TroughBlockEntity trough ? MachineFluids.comparator(trough.tank()) : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof TroughBlockEntity trough)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            trough.extract(player, player.isShiftKeyDown());
            return InteractionResult.CONSUME;
        }
        if (MachineFluids.useOnTank(level, pos, player, hand, trough.tank())) return InteractionResult.CONSUME;
        if (trough.tank().isEmpty()) {
            ItemStack rest = trough.insert(held.copy());
            if (rest.getCount() < held.getCount()) {
                player.setItemInHand(hand, rest);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 0.8F + level.random.nextFloat() * 0.4F);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TroughBlockEntity trough) trough.dropContents();
        super.onRemove(state, level, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TroughBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, HHMachines.TROUGH_BE.get(), TroughBlockEntity::serverTick);
    }
}
