// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import com.SaintAlfonzo.creativecompat.surface.SnowLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow! Real Magic's snowy plant ({@code snowrealmagic:snow}, {@code EntitySnowLayerBlock.getCollisionShape},
 * {@code m_5939_}) walks through like a snow layer (snow_real_magic.md 4 step 5, bug B7). SRM caches the shape per
 * state and layer count, not per entity, so the walk-through shape ({@link SnowLayers#collision}, which depends on the
 * entity) is answered here before the cache: the snow part by our rule, joined with the held plant's own collision.
 * Contexts our rule leaves alone (items, projectiles, plain path searches) go on to SRM's cached shape. Gated on
 * {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.block.EntitySnowLayerBlock", remap = false)
public abstract class SrmSnowCollisionMixin {

	@Inject(method = "m_5939_", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void creativecompat$walkThrough(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
											CallbackInfoReturnable<VoxelShape> cir) {
		try {
			VoxelShape snow = SnowLayers.collision(state, level, pos, ctx);
			if (snow == null) return;
			BlockState held = SnowRealMagic.held(level, pos, state);
			cir.setReturnValue(held == null ? snow : Shapes.or(snow, held.getCollisionShape(level, pos, ctx)));
		} catch (Throwable t) {
			HookFaults.once("SrmSnowCollisionMixin", t);
			// SRM's shape
		}
	}
}
