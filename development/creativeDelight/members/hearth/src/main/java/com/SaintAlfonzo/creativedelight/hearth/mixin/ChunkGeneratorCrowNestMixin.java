package com.SaintAlfonzo.creativedelight.hearth.mixin;

import com.SaintAlfonzo.creativedelight.hearth.nest.RoofNests;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Village roof nests after a chunk's structures and features are placed (plan_hearth_crow_swap.md 5.2). m_213609_ = applyBiomeDecoration. */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorCrowNestMixin {
    @Inject(method = "m_213609_", at = @At("TAIL"), remap = false)
    private void hearthandharvest$roofNests(WorldGenLevel level, ChunkAccess chunk, StructureManager structures, CallbackInfo ci) {
        RoofNests.decorate(level, chunk, structures);
    }
}
