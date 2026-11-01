package net.satisfy.foundation.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Port: 1.20.5's split of {@code Block#use} into {@code useItemOn} (with the held stack, either hand) and
 * {@code useWithoutItem} (main hand only, runs when {@code useItemOn} passes to it). Ported blocks keep upstream's two
 * methods and route 1.19.2's single {@code use} through {@link #dispatch}, in the same order as 1.21's
 * {@code ServerPlayerGameMode#useItemOn}.
 */
public enum ItemInteractionResult {
    SUCCESS,
    CONSUME,
    CONSUME_PARTIAL,
    /** Not handled: try {@code useWithoutItem} next (main hand only), then the item. */
    PASS_TO_DEFAULT_BLOCK_INTERACTION,
    /** Not handled, skip {@code useWithoutItem}: only the item's own use runs. */
    SKIP_DEFAULT_BLOCK_INTERACTION,
    FAIL;

    public boolean consumesAction() {
        return this == SUCCESS || this == CONSUME || this == CONSUME_PARTIAL;
    }

    public InteractionResult result() {
        return switch (this) {
            case SUCCESS -> InteractionResult.SUCCESS;
            case CONSUME -> InteractionResult.CONSUME;
            case CONSUME_PARTIAL -> InteractionResult.CONSUME_PARTIAL;
            case FAIL -> InteractionResult.FAIL;
            default -> InteractionResult.PASS;
        };
    }

    public static ItemInteractionResult sidedSuccess(boolean clientSide) {
        return clientSide ? SUCCESS : CONSUME;
    }

    @FunctionalInterface
    public interface ItemUse {
        ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);
    }

    @FunctionalInterface
    public interface PlainUse {
        InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit);
    }

    /** Body of a ported block's {@code use}: {@code useItemOn} first, then {@code useWithoutItem} for the main hand. */
    public static InteractionResult dispatch(ItemUse itemUse, PlainUse plainUse, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemInteractionResult itemResult = itemUse == null ? PASS_TO_DEFAULT_BLOCK_INTERACTION : itemUse.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
        if (itemResult.consumesAction()) {
            return itemResult.result();
        }
        if (itemResult == PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND && plainUse != null) {
            InteractionResult plainResult = plainUse.useWithoutItem(state, level, pos, player, hit);
            if (plainResult.consumesAction()) {
                return plainResult;
            }
        }
        return itemResult == FAIL ? InteractionResult.FAIL : InteractionResult.PASS;
    }
}
