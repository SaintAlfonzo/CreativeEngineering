package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.SkyIslands;
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
 * ConfiguredFeature#place (m_224953_): Sky Structures' floating islands are placed by worldgen/SkyIslands instead of
 * their own feature (one island per hit, above the ground and the sea). Every other feature goes on unchanged.
 */
@Mixin(value = ConfiguredFeature.class, remap = false)
public class ConfiguredFeatureSkyIslandsMixin {

	@Inject(method = "m_224953_(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;"
			+ "Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$skyIslands(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin,
			CallbackInfoReturnable<Boolean> cir) {
		Boolean placed = SkyIslands.place(((ConfiguredFeature<?, ?>) (Object) this).feature(), level, random, origin);
		if (placed != null) cir.setReturnValue(placed);
	}
}
