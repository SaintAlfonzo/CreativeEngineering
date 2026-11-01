package com.SaintAlfonzo.creativedelight.mixin.supplementaries;

import com.SaintAlfonzo.creativedelight.treatedstick.TreatedSticks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Placeable treated sticks (feature_writeup/treated_sticks.md): Supplementaries' tomato vines on its sticks and ropes
 * (FarmersDelightCompatImpl$TomatoLoggedBlock, which has its own attemptRopeClimb) also climb onto a placed treated
 * stick above. Supplementaries 1.19.2-2.4.20.
 */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.integration.forge.FarmersDelightCompatImpl$TomatoLoggedBlock", remap = false)
public abstract class SuppTomatoClimbTreatedStickMixin {

	@Inject(method = "attemptRopeClimb(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V",
			at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void creativeDelight$climbTreatedStick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (TreatedSticks.tryClimb(level, pos, random)) ci.cancel();
	}
}
