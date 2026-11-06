package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.GeneratedFallingBlocks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code ProtoChunkTicks.schedule} (m_183393_): ticks scheduled during generation (WorldGenRegion). A falling block's
 * tick is dropped, so it does not fall on the chunk's first tick ({@link GeneratedFallingBlocks}). Loading a saved
 * proto chunk does not go through here.
 */
@Mixin(value = ProtoChunkTicks.class, remap = false)
public abstract class ProtoChunkTicksFallingMixin {

    @Inject(method = "m_183393_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$dropFallingTick(ScheduledTick<?> tick, CallbackInfo ci) {
        if (GeneratedFallingBlocks.dropGenerationTick(tick.type())) ci.cancel();
    }
}
