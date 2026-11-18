// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.SnowLayers;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Path searches of mobs sunk in snow layers (plan_post_scratch_1115.md G11; {@link SnowLayers#beginPath}): TAIL of
 * {@code NodeEvaluator.prepare} ({@code m_6028_}) records whether the mob is sunk, HEAD of {@code done}
 * ({@code m_6802_}) clears it. Every evaluator calls these through super (vanilla's and MCA's
 * {@code VillagerLandPathNodeMaker}). Vanilla-class target, {@code remap = false}.
 */
@Mixin(value = NodeEvaluator.class, remap = false)
public abstract class SnowPathMixin {

	@Inject(method = "m_6028_", at = @At("TAIL"))
	private void creativecompat$sunkStart(PathNavigationRegion region, Mob mob, CallbackInfo ci) {
		try {
			SnowLayers.beginPath(mob);
		} catch (Throwable ignored) {
			SnowLayers.endPath();
		}
	}

	@Inject(method = "m_6802_", at = @At("HEAD"))
	private void creativecompat$sunkEnd(CallbackInfo ci) {
		SnowLayers.endPath();
	}
}
