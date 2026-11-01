package com.SaintAlfonzo.creativedelight.mixin.farmersdelight;

import com.SaintAlfonzo.creativedelight.treatedstick.TreatedSticks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

/**
 * Placeable treated sticks (feature_writeup/treated_sticks.md): an FD tomato vine with a placed treated stick above
 * climbs onto it as {@code creative_delight:treated_stick_tomatoes} instead of FD's rope-logged vine.
 */
@Mixin(value = TomatoVineBlock.class, remap = false)
public abstract class TomatoClimbTreatedStickMixin {

	@Inject(method = "attemptRopeClimb", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativeDelight$climbTreatedStick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (TreatedSticks.tryClimb(level, pos, random)) ci.cancel();
	}
}
