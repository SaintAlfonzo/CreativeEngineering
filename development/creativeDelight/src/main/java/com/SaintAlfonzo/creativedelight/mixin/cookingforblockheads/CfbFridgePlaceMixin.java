package com.SaintAlfonzo.creativedelight.mixin.cookingforblockheads;

import com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen.FridgePairs;
import net.blay09.mods.cookingforblockheads.block.FridgeBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Cooking for Blockheads fridge placed next to an unpaired fridge takes the mirrored door side, so the two join as a
 * double fridge (kitchen/FridgePairs, TODO cooking/kitchen #36).
 */
@Mixin(value = FridgeBlock.class, remap = false)
public abstract class CfbFridgePlaceMixin {

    @Inject(method = "m_5573_", at = @At("RETURN"), cancellable = true)
    private void ce$mirrorDoor(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        BlockState mirrored = FridgePairs.mirrorOnPlace(state, context.getLevel(), context.getClickedPos());
        if (mirrored != state) cir.setReturnValue(mirrored);
    }
}
