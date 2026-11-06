package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.DistantClip;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Distant Horizons' generation clipped below a height (worldgen/DistantClip; feature_writeup/locate.md 3.7).
 *
 *  - createNoiseChunk (m_224256_, first called by the biome step) and fillFromNoise (m_213974_) HEAD: mark the chunk
 *    when the StructureManager reads a DH region.
 *  - fillFromNoise's NoiseSettings#clampToHeightAccessor (m_224530_): a marked chunk's noise starts at the clip (the
 *    noise sampler, NoiseChunkDistantClipMixin, uses the same range).
 *  - doFill (m_224284_) RETURN: the band below the clip is filled with stone, or water over a fake seabed in oceans.
 */
@Mixin(value = NoiseBasedChunkGenerator.class, remap = false)
public class NoiseBasedChunkGeneratorDistantClipMixin {

	@Inject(method = "m_224256_(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/StructureManager;"
			+ "Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;)"
			+ "Lnet/minecraft/world/level/levelgen/NoiseChunk;", at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$markOnSampler(ChunkAccess chunk, StructureManager manager, Blender blender, RandomState randomState,
			CallbackInfoReturnable<NoiseChunk> cir) {
		DistantClip.mark(chunk, manager);
	}

	@Inject(method = "m_213974_(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;"
			+ "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;"
			+ "Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), require = 1, remap = false)
	private void creativeengineering$markOnFill(Executor executor, Blender blender, RandomState randomState, StructureManager manager, ChunkAccess chunk,
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
		DistantClip.mark(chunk, manager);
	}

	@Redirect(method = "m_213974_(Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/levelgen/blending/Blender;"
			+ "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;"
			+ "Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseSettings;m_224530_(Lnet/minecraft/world/level/LevelHeightAccessor;)"
					+ "Lnet/minecraft/world/level/levelgen/NoiseSettings;"), require = 1, remap = false)
	private NoiseSettings creativeengineering$clipFill(NoiseSettings settings, LevelHeightAccessor accessor) {
		return DistantClip.clamp(settings.clampToHeightAccessor(accessor), accessor);
	}

	@Inject(method = "m_224284_(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;"
			+ "Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;II)"
			+ "Lnet/minecraft/world/level/chunk/ChunkAccess;", at = @At("RETURN"), require = 1, remap = false)
	private void creativeengineering$fillBand(Blender blender, StructureManager manager, RandomState randomState, ChunkAccess chunk, int cellMinY,
			int cellCount, CallbackInfoReturnable<ChunkAccess> cir) {
		DistantClip.fillBand(chunk, randomState);
	}
}
