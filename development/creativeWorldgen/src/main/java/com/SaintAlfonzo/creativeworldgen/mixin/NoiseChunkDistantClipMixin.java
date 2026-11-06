package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.DistantClip;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * NoiseChunk#forChunk (m_224352_): a chunk Distant Horizons generates with the clip on gets a noise sampler whose
 * range starts at the clip, matching the clipped fillFromNoise (worldgen/DistantClip). Same range in both, or the
 * sampler's cell indices would not line up with the fill's.
 */
@Mixin(value = NoiseChunk.class, remap = false)
public class NoiseChunkDistantClipMixin {

	@Redirect(method = "m_224352_(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/RandomState;"
			+ "Lnet/minecraft/world/level/levelgen/DensityFunctions$BeardifierOrMarker;Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;"
			+ "Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;Lnet/minecraft/world/level/levelgen/blending/Blender;)"
			+ "Lnet/minecraft/world/level/levelgen/NoiseChunk;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;m_224530_(Lnet/minecraft/world/level/LevelHeightAccessor;)"
					+ "Lnet/minecraft/world/level/levelgen/NoiseSettings;"), require = 1, remap = false)
	private static NoiseSettings creativeengineering$clipSampler(NoiseSettings settings, LevelHeightAccessor accessor) {
		return DistantClip.clamp(settings.clampToHeightAccessor(accessor), accessor);
	}
}
