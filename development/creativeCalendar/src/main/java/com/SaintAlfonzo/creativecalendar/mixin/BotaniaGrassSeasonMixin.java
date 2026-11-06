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
 * Crop seasons for Botania's six coloured grasses (BotaniaGrassBlock) (farming/crops #28, feature_writeup/crops.md 2.12.1). Its tick is only the spread onto dirt.
 * Gated on its mod by CalendarMixinPlugin; require = 1 so a missed target fails the boot instead of silently doing nothing
 * (the old four-target mixin with require = 0 never applied to IW, Botania or Vintage, r1103 SQ-PST-08, -10, -11, -11b).
 */
@Pseudo
@Mixin(targets = "vazkii.botania.common.block.BotaniaGrassBlock", remap = false)
public abstract class BotaniaGrassSeasonMixin {

    // m_213897_ = tick; Botania's grass spreads there (the default randomTick calls it) and does nothing else, so HEAD
    @Inject(method = "m_213897_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativecalendar$grassSeason(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                               CallbackInfo ci) {
        if (GrassSeasonHook.refuse("botania:BotaniaGrassBlock", state, level, pos)) ci.cancel();
    }
}
