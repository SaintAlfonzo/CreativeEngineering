package com.SaintAlfonzo.creativeworldgen.mixin;

import appeng.worldgen.meteorite.MeteoriteStructure;
import com.SaintAlfonzo.creativeworldgen.worldgen.MeteoriteHeights;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Free-standing AE2 meteorites at any height (plan_worldgen_edit C6): after AE2 builds its one piece on the surface,
 * {@link MeteoriteHeights} may move it underground or into the air. Meteorites that are parts of a meta structure
 * keep AE2's surface placement (MetaStructure marks those builds).
 */
@Mixin(value = MeteoriteStructure.class, remap = false)
public class AeMeteoriteHeightMixin {

	@Inject(method = "generatePieces", remap = false, require = 1, at = @At("TAIL"))
	private static void creativeengineering$anyHeight(StructurePiecesBuilder builder, Structure.GenerationContext ctx, CallbackInfo ci) {
		MeteoriteHeights.apply(builder, ctx);
	}
}
