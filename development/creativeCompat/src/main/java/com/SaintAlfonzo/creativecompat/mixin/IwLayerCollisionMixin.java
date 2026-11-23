// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.LeafPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Weathering sand, red sand and ash layers stand on the layer's visible top, for every entity
 * ({@link com.SaintAlfonzo.creativecompat.surface.SurfaceLayers}): HEAD of
 * {@code getCollisionShape} ({@code m_5939_}) returns the block's outline shape. Before, ash stood living
 * entities on half its height and let them through ash over ash. Leaf piles (a subclass) keep their own. SRG names,
 * {@code remap = false}; applied only with the mod loaded.
 */
@Mixin(value = LayerBlock.class, remap = false)
public abstract class IwLayerCollisionMixin {

	@Inject(method = "m_5939_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$layerTop(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
										 CallbackInfoReturnable<VoxelShape> cir) {
		if ((Object) this instanceof LeafPileBlock) return; // walk-through, no collision of its own
		try {
			cir.setReturnValue(state.getShape(level, pos, ctx));
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("IwLayerCollisionMixin", t);
			// the mod's own collision
		}
	}
}
