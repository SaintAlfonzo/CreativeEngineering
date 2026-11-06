package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LocatePrediction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StructureCheck#canCreateStructure (m_226755_, private) predicts whether a structure could start in a
 * chunk that hasn't generated yet; /locate, explorer maps, cartographer trades and dolphins trust it
 * before loading the chunk. A slot whose frequency roll fails never gets that structure, so answer "no"
 * up front with the same roll (worldgen/StructureFrequencies) instead of loading the chunk to find out.
 * Before it, the structure set's own frequency and exclusion zone (worldgen/LocatePrediction, stage A).
 */
@Mixin(value = StructureCheck.class, remap = false)
public class StructureCheckFrequencyMixin {

	@Inject(method = "m_226755_(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Z",
			at = @At("HEAD"), cancellable = true, require = 1, remap = false)
	private void creativeengineering$skipRejectedSlot(ChunkPos pos, Structure structure, CallbackInfoReturnable<Boolean> cir) {
		if (LocatePrediction.rejectsAtHead((StructureCheck) (Object) this, structure, pos)) cir.setReturnValue(false);
	}
}
