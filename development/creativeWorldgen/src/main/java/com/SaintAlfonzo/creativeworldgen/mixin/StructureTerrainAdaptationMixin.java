package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.TerrainAdaptation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Structure#terrainAdaptation (m_226620_) answers from the creativeworldgen:terrain_adaptation/* tags (see
 * worldgen/TerrainAdaptation), so the Beardifier and the 12-block reference margin of terrain-adapted starts
 * both follow the tag. Subclasses that override the method (YUNG's jigsaw structures) are not affected.
 */
@Mixin(value = Structure.class, remap = false)
public class StructureTerrainAdaptationMixin {

	@Inject(method = "m_226620_()Lnet/minecraft/world/level/levelgen/structure/TerrainAdjustment;", at = @At("HEAD"), cancellable = true, require = 1)
	private void creativeengineering$taggedAdaptation(CallbackInfoReturnable<TerrainAdjustment> cir) {
		TerrainAdjustment tagged = TerrainAdaptation.override((Structure) (Object) this);
		if (tagged != null) cir.setReturnValue(tagged);
	}
}
