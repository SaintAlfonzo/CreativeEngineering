// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SurfaceLayers;
import com.ordana.immersive_weathering.blocks.LayerBlock;
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
 * Immersive Weathering sand, red sand and ash layers ({@link SurfaceLayers}): never replaced by another block
 * ({@code canBeReplaced}, {@code m_6864_}); a 9th sand or red sand layer turns a full pile into the block
 * ({@code onPlace}, {@code m_6807_}, {@link SurfaceLayers#settle}); full fall damage ({@code fallOn},
 * {@code m_142072_}, was 0.3x above 2 layers for ash). Leaf piles override {@code canBeReplaced}
 * ({@code IwLeafPileReplaceMixin}) and have no full block. SRG names, {@code remap = false}; applied only with the mod.
 */
@Mixin(value = LayerBlock.class, remap = false)
public abstract class IwLayerRulesMixin {

	@Inject(method = "m_6864_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$replace(BlockState state, BlockPlaceContext ctx, CallbackInfoReturnable<Boolean> cir) {
		try {
			Boolean r = SurfaceLayers.replaceable(state, ctx);
			if (r != null) cir.setReturnValue(r);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("IwLayerRulesMixin#1", t);
			// the block's own rule
		}
	}

	@Inject(method = "m_6807_", at = @At("TAIL"))
	private void creativecompat$settle(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving,
			CallbackInfo ci) {
		try {
			SurfaceLayers.settle(level, pos, state);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("IwLayerRulesMixin#2", t);
			// stays a full layer pile
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
