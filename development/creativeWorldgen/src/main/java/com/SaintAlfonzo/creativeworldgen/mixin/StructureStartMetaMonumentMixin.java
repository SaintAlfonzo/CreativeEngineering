package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.MetaStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StructureStart.loadStaticStart (m_226857_) regenerates a lone ocean monument's rooms after load but not a monument
 * that is a part of a meta structure (meta/struck_monument): {@link MetaStructure#repairMonumentsAfterLoad} does the
 * same for those (structures/contents #8, feature_writeup/structure_tuning.md).
 */
@Mixin(value = StructureStart.class, remap = false)
public class StructureStartMetaMonumentMixin {

	@Inject(method = "m_226857_", at = @At("RETURN"), cancellable = true, require = 1, remap = false)
	private static void creativeengineering$metaMonumentRooms(StructurePieceSerializationContext context, CompoundTag tag, long seed,
			CallbackInfoReturnable<StructureStart> cir) {
		StructureStart start = cir.getReturnValue();
		StructureStart fixed = MetaStructure.repairMonumentsAfterLoad(start, seed);
		if (fixed != start) cir.setReturnValue(fixed);
	}
}
