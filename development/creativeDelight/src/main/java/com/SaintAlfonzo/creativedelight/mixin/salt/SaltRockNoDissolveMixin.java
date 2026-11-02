package com.SaintAlfonzo.creativedelight.mixin.salt;

import com.SaintAlfonzo.creativedelight.salt.SaltBlockTicks;
import io.github.mortuusars.salt.Dissolving;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Salt's Dissolving, for {@link SaltBlockTicks#noDissolve} (raw rock salt block): never dissolves in water or
 * rain. maybeDissolveInRain gets the dissolved state, not the block, so the block is read at pos. Melting and
 * cluster growth are untouched.
 */
@Mixin(value = Dissolving.class, remap = false)
public abstract class SaltRockNoDissolveMixin {

    @Inject(method = "maybeDissolve", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$keepRockSalt(BlockState state, BlockPos pos, BlockState fluidState,
                                                         BlockPos fluidPos, ServerLevel level,
                                                         CallbackInfoReturnable<Boolean> cir) {
        if (SaltBlockTicks.noDissolve(state)) cir.setReturnValue(false);
    }

    @Inject(method = "maybeDissolveInRain", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$keepRockSaltInRain(BlockState dissolved, ServerLevel level, BlockPos pos,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (SaltBlockTicks.noDissolve(level.getBlockState(pos))) cir.setReturnValue(false);
    }
}
