package com.SaintAlfonzo.creativecommon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.SaintAlfonzo.creativecommon.melting.Melting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Melting ({@link Melting}, feature_writeup/melting.md) at the block state, so every block class is covered without a
 * mixin per class: BlockStateBase.randomTick ({@code m_222972_}, what ServerLevel.tickChunk calls) runs the melt rules
 * before the block's own random tick and skips it when the block melted; BlockStateBase.onPlace ({@code m_60696_}) turns
 * snow placed in an ultra-warm dimension into air. Other blocks pay one identity-map lookup.
 */
@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class MeltingBlockStateMixin {

	@Inject(method = "m_222972_", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
	private void creativecommon$meltRandomTick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (Melting.onRandomTick((BlockState) (Object) this, level, pos, random)) ci.cancel();
	}

	@Inject(method = "m_60696_", at = @At("TAIL"), remap = false, require = 1)
	private void creativecommon$meltOnPlace(Level level, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
		Melting.onPlaced((BlockState) (Object) this, level, pos);
	}
}
