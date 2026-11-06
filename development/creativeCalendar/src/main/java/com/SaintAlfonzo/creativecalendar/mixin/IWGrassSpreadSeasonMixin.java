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
 * Crop seasons for Immersive Weathering's ModGrassBlock (registered as minecraft:grass_block, extended by rooted_grass_block) (farming/crops #28, feature_writeup/crops.md 2.12.1). After the vanilla spread (rolled in GrassSpreadSeasonMixin) it spreads over mycelium and rooted dirt; the hook sits on the light check that opens that second spread, so it gets its own roll and the path-wear code before it is untouched.
 * Gated on its mod by CalendarMixinPlugin; require = 1 so a missed target fails the boot instead of silently doing nothing
 * (the old four-target mixin with require = 0 never applied to IW, Botania or Vintage, r1103 SQ-PST-08, -10, -11, -11b).
 */
@Pseudo
@Mixin(targets = "com.ordana.immersive_weathering.blocks.soil.ModGrassBlock", remap = false)
public abstract class IWGrassSpreadSeasonMixin {

    // m_213898_ = randomTick; m_46803_ = Level.getMaxLocalRawBrightness, the light check before IW's own spread
    @Inject(method = "m_213898_", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;m_46803_(Lnet/minecraft/core/BlockPos;)I", ordinal = 0), cancellable = true, remap = false, require = 1)
    private void creativecalendar$grassSeason(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                               CallbackInfo ci) {
        if (GrassSeasonHook.refuse("immersive_weathering:ModGrassBlock", state, level, pos)) ci.cancel();
    }
}
