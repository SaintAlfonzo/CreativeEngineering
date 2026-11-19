// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
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
 * Immersive Engineering sawdust piles stand on the layer's visible top, for every entity
 * ({@link com.SaintAlfonzo.creativecompat.surface.SurfaceLayers}, owner notes 2026-11-18): HEAD of
 * {@code getCollisionShape} ({@code m_5939_}) returns the block's outline shape. Before, the collision was
 * 2 px under the outline (a single pile had none; 1 px at full height). SRG names,
 * {@code remap = false}; applied only with the mod loaded.
 */
@Mixin(value = SawdustBlock.class, remap = false)
public abstract class IeSawdustCollisionMixin {

	@Inject(method = "m_5939_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$layerTop(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
										 CallbackInfoReturnable<VoxelShape> cir) {
		try {
			cir.setReturnValue(state.getShape(level, pos, ctx));
		} catch (Throwable ignored) {
			// the mod's own collision
		}
	}
}
