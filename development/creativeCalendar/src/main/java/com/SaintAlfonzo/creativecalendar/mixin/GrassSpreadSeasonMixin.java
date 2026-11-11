package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.CropSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crop seasons for grass spread (farming/crops #28, feature_writeup/crops.md 2.12.1): a grass block's random tick
 * spreads it onto dirt without a crop-grow event, so {@link CropSeasons} never saw it. When the block is typed
 * ({@code creativecalendar:crop_seasons/grass}, the json is the switch) and its season is off, the tick is skipped
 * with {@code offPenalty}'s chance. A grass block that can no longer be grass (covered) always runs its tick, so
 * turning back to dirt is not slowed. Mycelium and nylium share the class and are untyped: unchanged.
 */
@Mixin(value = SpreadingSnowyDirtBlock.class, remap = false)
public abstract class GrassSpreadSeasonMixin {

    // m_56823_ = SpreadingSnowyDirtBlock.canBeGrass(BlockState, LevelReader, BlockPos)
    @Shadow(remap = false)
    private static boolean m_56823_(BlockState state, LevelReader level, BlockPos pos) {
        throw new AssertionError();
    }

    // m_213898_ = randomTick(BlockState, ServerLevel, BlockPos, RandomSource)
    @Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$grassSeason(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
                                                 CallbackInfo ci) {
        CropSeasons.Type type = CropSeasons.typeOf(state);
        if (type != null && m_56823_(state, level, pos) && CropSeasons.refuseSpread(level, pos, type)) ci.cancel();
    }
}
