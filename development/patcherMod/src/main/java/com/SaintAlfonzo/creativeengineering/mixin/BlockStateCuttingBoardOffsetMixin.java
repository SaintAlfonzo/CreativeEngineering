package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CuttingBoardOnCounter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Model offset for a Farmer's Delight cutting board on a Cooking for Blockheads counter ({@link CuttingBoardOnCounter},
 * TODO food/kitchen #8). {@code getOffset} is what the chunk mesher (vanilla and Rubidium) moves a block model by; the
 * board's offset type is NONE, so without this it is always zero. One reference compare for every other block.
 * SRG name with {@code remap = false}: m_60824_ getOffset.
 */
@Mixin(BlockStateBase.class)
public abstract class BlockStateCuttingBoardOffsetMixin {

    @Inject(method = "m_60824_", at = @At("HEAD"), cancellable = true, remap = false)
    private void ce$cuttingBoardOffset(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        if (((BlockStateBase) (Object) this).getBlock() == CuttingBoardOnCounter.board()) {
            cir.setReturnValue(CuttingBoardOnCounter.offset(level, pos));
        }
    }
}
