package com.SaintAlfonzo.creativedelight.farmers.mixin;

import com.SaintAlfonzo.creativedelight.farmers.FDBackportRegistry;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Ordinary fences do not join a rope fence or a rope fence gate (1.3.4 RopeFenceConnectionMixin). */
@Mixin(FenceBlock.class)
public abstract class RopeFenceConnectionMixin {

	@Inject(method = "m_53329_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$noRopeGate(BlockState state, boolean isSideSolid, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(FDBackportRegistry.ROPE_FENCE_GATE.get()) || state.is(FDBackportRegistry.ROPE_FENCE.get())) cir.setReturnValue(false);
	}

	@Inject(method = "m_153254_", at = @At("HEAD"), cancellable = true, remap = false)
	private void creativedelight$noRopeFence(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(FDBackportRegistry.ROPE_FENCE.get())) cir.setReturnValue(false);
	}
}
