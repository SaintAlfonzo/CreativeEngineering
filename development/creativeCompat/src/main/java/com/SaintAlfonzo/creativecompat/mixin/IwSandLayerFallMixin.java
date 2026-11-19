// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Immersive Weathering sand and red sand layers take full fall damage ({@link SurfaceLayers#fallDamage}; was 0.3x
 * above 2 layers): {@code fallOn} ({@code m_142072_}) overrides the base layer's. SRG names, {@code remap = false};
 * applied only with the mod.
 */
@Mixin(value = SandLayerBlock.class, remap = false)
public abstract class IwSandLayerFallMixin {

	@Redirect(method = "m_142072_", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;m_142535_(FFLnet/minecraft/world/damagesource/DamageSource;)Z"),
			require = 0)
	private boolean creativecompat$fullFallDamage(Entity self, float distance, float multiplier, DamageSource source,
			Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		return SurfaceLayers.fallDamage(level, state, pos, self, distance, source);
	}
}
