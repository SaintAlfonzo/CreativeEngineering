package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.crops.CanopyProofHeightmap;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code HeightmapPlacement.getPositions}: the heightmap read goes through {@link CanopyProofHeightmap#swap}, which turns
 * {@code MOTION_BLOCKING} into {@code MOTION_BLOCKING_NO_LEAVES} for {@code #creativedelight:canopy_proof} features while
 * {@code [worldgen] wildCropCanopyProofHeightmap} is on (plan_post_scratch_1115 E2). SRG names (1.19.2):
 * getPositions m_213676_, PlacementContext.getHeight m_191824_.
 */
@Mixin(value = HeightmapPlacement.class, remap = false)
public abstract class HeightmapPlacementCanopyMixin {

	@Redirect(method = "m_213676_", remap = false, at = @At(value = "INVOKE", remap = false,
			target = "Lnet/minecraft/world/level/levelgen/placement/PlacementContext;m_191824_(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private int creativedelight$canopyProof(PlacementContext context, Heightmap.Types type, int x, int z) {
		return context.getHeight(CanopyProofHeightmap.swap(context, type), x, z);
	}
}
