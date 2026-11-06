package com.SaintAlfonzo.creativedelight.roll.content;

import com.SaintAlfonzo.creativedelight.roll.registry.RollItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wrapped Chopsticks (roll_delight.md 3.2): a table decoration holding 1 to 4 wrapped pairs. Right-click with wrapped
 * chopsticks adds a pair; an empty hand takes one out as plain chopsticks, the last one removes the block. Breaking it
 * drops one wrapped pair per pair held (loot table, RollTranslator).
 */
public class WrappedChopsticksBlock extends HorizontalDirectionalBlock {
    public static final int MAX = 4;
    public static final IntegerProperty PAIRS = IntegerProperty.create("chopsticks", 1, MAX);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D);

    public WrappedChopsticksBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH).setValue(PAIRS, 1));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        int pairs = state.getValue(PAIRS);
        if (held.is(RollItems.WRAPPED_CHOPSTICKS.get())) {
            if (pairs >= MAX) return InteractionResult.PASS;
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(PAIRS, pairs + 1), Block.UPDATE_ALL);
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.BAMBOO_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (pairs > 1) {
                level.setBlock(pos, state.setValue(PAIRS, pairs - 1), Block.UPDATE_ALL);
            } else {
                level.removeBlock(pos, false);
            }
            ItemStack chopsticks = new ItemStack(RollItems.CHOPSTICKS.get());
            if (!player.getInventory().add(chopsticks)) player.drop(chopsticks, false);
            level.playSound(null, pos, SoundEvents.BAMBOO_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PAIRS);
    }
}
