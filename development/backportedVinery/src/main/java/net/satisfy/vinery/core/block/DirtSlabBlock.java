package net.satisfy.vinery.core.block;

import net.satisfy.vinery.core.util.PortUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.satisfy.foundation.util.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class DirtSlabBlock extends SlabBlock {
    public DirtSlabBlock(Properties properties) {
        super(properties);
    }

        public @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (PortUtil.isShovel(heldItem)) {
            if (!level.isClientSide) {
                BlockState pathState = ObjectRegistry.DIRT_PATH_SLAB.get().defaultBlockState()
                        .setValue(TYPE, state.getValue(TYPE))
                        .setValue(WATERLOGGED, state.getValue(WATERLOGGED));

                level.setBlock(pos, pathState, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.isCreative()) {
                    PortUtil.hurt(heldItem, 1, player, EquipmentSlot.OFFHAND);
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        return ItemInteractionResult.dispatch(this::useItemOn, this::useWithoutItem, state, level, pos, player, hand, hit);
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}
