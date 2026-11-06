package com.SaintAlfonzo.creativedelight.barbeques.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Ingredients Basin (1.3.0 BasinBlock): two wells, shape by facing axis. Pack change: sneak + right click on a filled
 * well takes one item into the inventory even with items in hand ({@link #onSneakGrab}).
 */
public class BasinBlock extends StorageBlock {
    public static final VoxelShape OUTER = Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);
    public static final VoxelShape SHAPE_X = Shapes.join(OUTER,
            Shapes.or(Block.box(2.0, 1.0, 2.0, 14.0, 4.0, 7.5), Block.box(2.0, 1.0, 8.5, 14.0, 4.0, 14.0)), BooleanOp.ONLY_FIRST);
    public static final VoxelShape SHAPE_Z = Shapes.join(OUTER,
            Shapes.or(Block.box(2.0, 1.0, 2.0, 7.5, 4.0, 14.0), Block.box(8.5, 1.0, 2.0, 14.0, 4.0, 14.0)), BooleanOp.ONLY_FIRST);

    public BasinBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasinBlockEntity(pos, state);
    }

    /**
     * Sneak + right click with anything in either hand: Forge skips {@code Block#use} then (the item gets the click), so
     * the basin's take-one never ran with full hands. Take one item from the clicked well into the inventory instead
     * (dropped when the inventory is full). Empty hands still go through {@code use}, which puts it in the hand.
     */
    public static void onSneakGrab(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!(level.getBlockState(pos).getBlock() instanceof BasinBlock)
                || !(level.getBlockEntity(pos) instanceof BasinBlockEntity basin)) {
            return;
        }
        int slot = basin.getSlotForHitting(event.getHitVec(), level);
        if (slot < 0 || slot >= basin.size() || basin.getStack(slot).isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        if (level.isClientSide) {
            return;
        }
        ItemStack stored = basin.getStack(slot).copy();
        ItemStack one = stored.split(1);
        basin.setStack(slot, stored);
        player.getInventory().placeItemBackInInventory(one);
        level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
