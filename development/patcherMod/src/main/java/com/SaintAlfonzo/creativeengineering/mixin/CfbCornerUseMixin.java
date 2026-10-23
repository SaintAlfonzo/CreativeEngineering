package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CfbCornerStorage;

import net.blay09.mods.cookingforblockheads.block.CornerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Right-clicking Cooking for Blockheads' kitchen corner opens its storage as a chest ({@link CfbCornerStorage}, TODO
 * cooking/kitchen #15). CfB's own {@code use} only recolours with dye and answers FAIL otherwise; that FAIL is kept for
 * a block item on the top face, so a cutting board (or anything else) can still be placed on the corner, as on a
 * counter. SRG name with {@code remap = false}: m_6227_ use.
 */
@Mixin(value = CornerBlock.class, remap = false)
public abstract class CfbCornerUseMixin {

    @Inject(method = "m_6227_", at = @At("RETURN"), cancellable = true)
    private void ce$openStorage(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.FAIL) return;
        if (hit.getDirection() == Direction.UP && player.getItemInHand(hand).getItem() instanceof BlockItem) return;
        if (!(level.getBlockEntity(pos) instanceof CfbCornerStorage.Holder holder)) return;
        if (!level.isClientSide) {
            player.openMenu(CfbCornerStorage.menu(holder.ce$cornerStorage(), state.getBlock().getName()));
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
