package com.SaintAlfonzo.creativedelight.mixin.immersive_weathering;

import com.ordana.immersive_weathering.blocks.WallRootsBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Weathering's wall hanging roots ({@code immersive_weathering:hanging_roots_wall}) set {@code facing} to the
 * clicked face in getStateForPlacement. Its facing has only the four horizontal values, so placing hanging roots
 * against the top or bottom face of a block threw {@code IllegalArgumentException: Cannot set property ... to up}
 * (flower_pots.md 3, SQ-POT-03: every pot and planter holder hit it). A vertical clicked face now gives no wall
 * state, so the item falls back to its other placements or does nothing, as for any block that cannot go there.
 */
@Mixin(value = WallRootsBlock.class, remap = false)
public abstract class WallRootsVerticalFaceMixin {

    @Inject(method = "m_5573_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativedelight$noVerticalFacing(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
        if (ctx.getClickedFace().getAxis().isVertical()) cir.setReturnValue(null);
    }
}
