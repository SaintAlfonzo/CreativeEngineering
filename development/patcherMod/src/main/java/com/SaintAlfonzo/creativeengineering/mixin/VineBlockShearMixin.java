package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.block.VineShearing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sheared modded vines ({@link VineShearing}). {@code VineBlock.createBlockStateDefinition} (m_7926_) runs for every
 * subclass that calls super, so modded vines get the {@code fertile} state there ({@link VineShearing#addsFertile}
 * decides; vanilla's vine does not). Its default stays true: it is the property's first value, which
 * {@code stateDefinition.any()} picks in the vine constructors. {@code VineBlock.randomTick} (m_213898_) is where a
 * vine spreads; a sheared one skips it.
 */
@Mixin(value = VineBlock.class, remap = false)
public abstract class VineBlockShearMixin {

    @Inject(method = "m_7926_", at = @At("TAIL"), remap = false)
    private void creativeengineering$addFertile(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (VineShearing.addsFertile(((Object) this).getClass())) {
            builder.add(VineShearing.FERTILE);
        }
    }

    @Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$shearedStopsSpreading(BlockState state, ServerLevel level, BlockPos pos,
                                                           RandomSource random, CallbackInfo ci) {
        if (VineShearing.isSheared(state)) {
            ci.cancel();
        }
    }
}
