package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.DistantClip;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ConfiguredFeature#place (m_224953_): in Distant Horizons' generation, a feature whose origin is below the clip
 * (ores, geodes, cave decoration) is skipped; the band there is plain fill (worldgen/DistantClip).
 */
@Mixin(value = ConfiguredFeature.class, remap = false)
public class ConfiguredFeatureDistantClipMixin {

	@Inject(method = "m_224953_(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;"
			+ "Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$skipBelowClip(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin,
			CallbackInfoReturnable<Boolean> cir) {
		if (DistantClip.skipFeature(level, origin)) cir.setReturnValue(false);
	}
}
