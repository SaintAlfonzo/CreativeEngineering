// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.HookFaults;
import com.SaintAlfonzo.creativecompat.compat.SnowRealMagic;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow! Real Magic's right-click on snow ({@code snownee.snow.GameEvents.onItemUse}, called from its
 * {@code RightClickBlock} handler before any block's own click), snow_real_magic.md 4c: the shovel scoop takes 2 layers
 * (R10), salt and other snow-layer blocks are never scooped (step 11), and a plain empty-hand click turns the block's
 * snowy look off (R19) instead of SRM's overlay toggle. {@link SnowRealMagic#onUse} decides; null leaves the click to
 * SRM (the 1-layer sneak scoop). Gated on {@code snowrealmagic}; {@code require = 0}.
 */
@Pseudo
@Mixin(targets = "snownee.snow.GameEvents", remap = false)
public abstract class SrmUseMixin {

	@Inject(method = "onItemUse(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;"
			+ "Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)"
			+ "Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void creativecompat$use(Player player, Level level, InteractionHand hand, BlockHitResult hit,
										   CallbackInfoReturnable<InteractionResult> cir) {
		try {
			InteractionResult result = SnowRealMagic.onUse(player, level, hand, hit);
			if (result != null) cir.setReturnValue(result);
		} catch (Throwable t) {
			HookFaults.once("SrmUseMixin", t);
			// SRM's click
		}
	}
}
