package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.GrassSeasonHook;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crop seasons for the Vinery grass slab (SpreadableGrassSlabBlock) (farming/crops #28, feature_writeup/crops.md 2.12.1). Its own random tick never calls the vanilla one.
 * Gated on its mod by CalendarMixinPlugin; require = 1 so a missed target fails the boot instead of silently doing nothing
 * (the old four-target mixin with require = 0 never applied to IW, Botania or Vintage, r1103 SQ-PST-08, -10, -11, -11b).
 */
@Pseudo
@Mixin(targets = "net.satisfy.vinery.core.block.SpreadableGrassSlabBlock", remap = false)
public abstract class VineryGrassSlabSeasonMixin {

    // m_213898_ = randomTick; m_46803_ = the light check that opens the spread (the covered-slab revert before it is untouched)
    @Inject(method = "m_213898_", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;m_46803_(Lnet/minecraft/core/BlockPos;)I", ordinal = 0), cancellable = true, remap = false, require = 1)
    private void creativecalendar$grassSeason(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                               CallbackInfo ci) {
        if (GrassSeasonHook.refuse("vinery:SpreadableGrassSlabBlock", state, level, pos)) ci.cancel();
    }
}
