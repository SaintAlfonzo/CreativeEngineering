// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
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
 * Supplementaries ash ({@link SurfaceLayers}): never replaced by another block ({@code canBeReplaced},
 * {@code m_6864_}; was at 1 layer) and full fall damage ({@code fallOn}, {@code m_142072_}; was 0.3x above 2 layers).
 * SRG names, {@code remap = false}; applied only with the mod.
 */
@Mixin(value = AshLayerBlock.class, remap = false)
public abstract class SuppAshRulesMixin {

	@Inject(method = "m_6864_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$replace(BlockState state, BlockPlaceContext ctx, CallbackInfoReturnable<Boolean> cir) {
		try {
			Boolean r = SurfaceLayers.replaceable(state, ctx);
			if (r != null) cir.setReturnValue(r);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("SuppAshRulesMixin", t);
			// the block's own rule
		}
	}

	@Redirect(method = "m_142072_", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;m_142535_(FFLnet/minecraft/world/damagesource/DamageSource;)Z"),
			require = 0)
	private boolean creativecompat$fullFallDamage(Entity self, float distance, float multiplier, DamageSource source,
			Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		return SurfaceLayers.fallDamage(level, state, pos, self, distance, source);
	}
}
