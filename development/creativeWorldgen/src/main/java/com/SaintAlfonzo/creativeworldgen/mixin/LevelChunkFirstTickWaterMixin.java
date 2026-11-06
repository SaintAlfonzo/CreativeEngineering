package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.FirstTickWater;
import com.SaintAlfonzo.creativeworldgen.worldgen.GeneratedFallingBlocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code LevelChunk.postProcessGeneration} (m_62812_) runs when a chunk becomes ticking and ticks its generated water
 * marks. HEAD opens the chunk's settling window before those ticks run ({@link FirstTickWater}); HEAD to RETURN
 * holds falling blocks its shape updates disturb ({@link GeneratedFallingBlocks}).
 */
@Mixin(value = LevelChunk.class, remap = false)
public abstract class LevelChunkFirstTickWaterMixin {

    @Inject(method = "m_62812_", at = @At("HEAD"), remap = false)
    private void creativeengineering$openSettleWindow(CallbackInfo ci) {
        FirstTickWater.onFirstTicking((LevelChunk) (Object) this);
        GeneratedFallingBlocks.beginPostProcess();
    }

    @Inject(method = "m_62812_", at = @At("RETURN"), remap = false)
    private void creativeengineering$endPostProcess(CallbackInfo ci) {
        GeneratedFallingBlocks.endPostProcess();
    }
}
