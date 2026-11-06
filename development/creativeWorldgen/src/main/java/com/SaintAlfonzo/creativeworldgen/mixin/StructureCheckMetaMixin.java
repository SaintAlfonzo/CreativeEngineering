package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

/**
 * StructureCheck#canCreateStructure (m_226755_, private) predicts whether a structure could start in a chunk that
 * hasn't generated yet by asking Structure#findGenerationPoint (m_214086_). A meta structure that only comes from
 * its anchors' slots (worldgen/MetaSlots) is predicted from those anchors instead of being assembled whole; the
 * chunk load /locate does next confirms it.
 */
@Mixin(value = StructureCheck.class, remap = false)
public class StructureCheckMetaMixin {

	@Redirect(method = "m_226755_(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/Structure;m_214086_("
					+ "Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;)Ljava/util/Optional;"),
			require = 1, remap = false)
	private Optional<Structure.GenerationStub> creativeengineering$predictMeta(Structure structure, Structure.GenerationContext ctx) {
		return MetaSlots.predictGenerationPoint(structure, ctx);
	}
}
