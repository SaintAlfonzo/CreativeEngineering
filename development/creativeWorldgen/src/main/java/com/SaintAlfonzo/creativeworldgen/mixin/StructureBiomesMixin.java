package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeFits;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Biome fits (see worldgen/StructureBiomeFits).
 *
 * Structure#biomes() (m_226559_) returns the structure's own settings list; every consumer of a structure's biomes
 * goes through it (ChunkGenerator's valid-biome check and placement table, StructureCheck, MetaStructure, the biome
 * rules). A structure with a fitted list answers with that instead.
 */
@Mixin(value = Structure.class, remap = false)
public class StructureBiomesMixin {

	@Inject(method = "m_226559_()Lnet/minecraft/core/HolderSet;", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$fittedBiomes(CallbackInfoReturnable<HolderSet<Biome>> cir) {
		HolderSet<Biome> fitted = StructureBiomeFits.biomesFor((Structure) (Object) this);
		if (fitted != null) cir.setReturnValue(fitted);
	}
}
