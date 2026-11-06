package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.CropSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Crop seasons for Immersive Weathering's plant growths (farming/crops #28, feature_writeup/crops.md 2.12.1): grass
 * blocks sprouting grass, grass growing tall, ferns growing large run in IW's {@code BlockGrowthHandler}, which fires no
 * crop-grow event. A growth whose owner block is typed and which places a block of the same type (a plant spreading
 * its own kind) is skipped with {@code offPenalty}'s chance while that type is off where the owner stands. Growths
 * that place other blocks (frost, soot, moss) are untouched. Pseudo: Immersive Weathering may be removed.
 */
@Pseudo
@Mixin(targets = "com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth", remap = false)
public abstract class IWGrowthSeasonMixin {

    @Shadow(remap = false) private Set<Block> possibleBlocks;

    /** Crop seasons read Serene Seasons; the mixin config gate only checks Immersive Weathering. */
    @Unique private static final boolean creativeengineering$seasons = LoadingModList.get().getModFileById("sereneseasons") != null;

    @Inject(method = "tryGrowing", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void creativeengineering$growthSeason(BlockPos pos, BlockState state, ServerLevel level,
                                                  Supplier<Holder<Biome>> biome, CallbackInfo ci) {
        if (!creativeengineering$seasons || possibleBlocks == null) return;
        CropSeasons.Type type = CropSeasons.typeOf(state);
        if (type == null || !type.seasonal()) return;
        for (Block placed : possibleBlocks) {
            if (CropSeasons.typeOf(placed.defaultBlockState()) == type) {
                if (CropSeasons.refuseSpread(level, pos, type)) ci.cancel();
                return;
            }
        }
    }
}
