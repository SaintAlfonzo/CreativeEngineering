package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.GeneratedFallingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code FallingBlock.updateShape} (m_7417_) schedules the fall after a neighbour change. Skipped while a chunk's
 * generation leftovers run ({@link GeneratedFallingBlocks}); the state is returned unchanged, as vanilla's own return.
 * Subclasses that override it and call super (concrete powder) are covered.
 */
@Mixin(value = FallingBlock.class, remap = false)
public abstract class FallingBlockHoldMixin {

    @Inject(method = "m_7417_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$holdGenerated(BlockState state, Direction dir, BlockState neighbour, LevelAccessor level,
                                                   BlockPos pos, BlockPos neighbourPos, CallbackInfoReturnable<BlockState> cir) {
        if (GeneratedFallingBlocks.hold(level)) cir.setReturnValue(state);
    }
}
