package com.SaintAlfonzo.creativeengineering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import xfacthd.framedblocks.common.block.interactive.FramedFlowerPotBlock;
import xfacthd.framedblocks.common.blockentity.FramedFlowerPotBlockEntity;

/**
 * FramedBlocks' framed flower pot takes a plant before it has a frame. Without a frame, any block item clicked on it
 * was treated as a frame attempt; a plant is not a valid frame, so the click was used up with a "block not valid"
 * message and never reached the pot. Now a plant the vanilla pot takes, clicked on a pot with no plant, is potted
 * first (same steps as the framed pot's own code: plant set, stat, one item used, game event). flower_pots.md 3.2.
 */
@Mixin(value = FramedFlowerPotBlock.class, remap = false)
public abstract class FramedFlowerPotUnframedMixin {

    @Inject(method = "m_6227_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$plantFirst(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof BlockItem item)) return;
        if (FramedFlowerPotBlock.getFlowerPotState(item.getBlock()).isAir()) return;
        if (!(level.getBlockEntity(pos) instanceof FramedFlowerPotBlockEntity pot) || pot.hasFlowerBlock()) return;
        if (!level.isClientSide) {
            pot.setFlowerBlock(item.getBlock());
            player.awardStat(Stats.POT_FLOWER);
            if (!player.getAbilities().instabuild) held.shrink(1);
        }
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
