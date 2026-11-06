package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.GroundMatch;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells worldgen/GroundMatch which structure start is placing on this thread, so it knows the
 * structure (exempt tag), the pieces to skip while sampling ground, and that a template placement on
 * the live level comes from /place structure rather than a structure block. placeInChunk = m_226850_.
 */
@Mixin(value = StructureStart.class, remap = false)
public class StructureStartGroundMatchMixin {

	private static final String PLACE_IN_CHUNK = "m_226850_(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;)V";

	@Inject(method = PLACE_IN_CHUNK, at = @At("HEAD"), require = 1)
	private void creativeengineering$beginPlacing(CallbackInfo ci) {
		GroundMatch.beginPlacing((StructureStart) (Object) this);
	}

	@Inject(method = PLACE_IN_CHUNK, at = @At("RETURN"), require = 1)
	private void creativeengineering$endPlacing(CallbackInfo ci) {
		GroundMatch.endPlacing();
	}
}
