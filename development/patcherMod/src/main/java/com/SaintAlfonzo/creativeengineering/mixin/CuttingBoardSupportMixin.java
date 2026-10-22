package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CuttingBoardOnCounter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;

/**
 * Farmer's Delight cutting board (1.19.2-1.2.4) on Cooking for Blockheads counters (TODO food/kitchen #8). FD's
 * {@code canSurvive} wants a rigid or center support face below, and CfB's counter and corner are 15 px tall
 * (shape 0-15, worktop at 15), so the board refused to go on them, although CfB's own {@code CounterBlock.use}
 * steps aside for a cutting board on its top face so it can be placed there. Blocks in
 * {@code #creativeengineering:cutting_board_supports} count as support too; {@link CuttingBoardOnCounter} draws the
 * board down on the worktop.
 * SRG name with {@code remap = false}: m_7898_ canSurvive.
 */
@Mixin(value = CuttingBoardBlock.class, remap = false)
public abstract class CuttingBoardSupportMixin {

    @Inject(method = "m_7898_", at = @At("RETURN"), cancellable = true)
    private void ce$counterSupport(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && level.getBlockState(pos.below()).is(CuttingBoardOnCounter.SUPPORTS)) {
            cir.setReturnValue(true);
        }
    }
}
