package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.FallingLayers;
import com.SaintAlfonzo.creativecompat.surface.FullBambooLeavesBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Twigs bamboo leaves (plan_post_scratch_1115.md G7.5, G7.9): HEAD of {@code updateShape} ({@code m_7417_}) keeps a
 * pile over a free block and queues its fall ({@link FallingLayers}); RETURN of {@code canBeReplaced}
 * ({@code m_6864_}) lets a 5th bamboo leaves go onto a pile of 4, and RETURN of {@code getStateForPlacement}
 * ({@code m_5573_}) makes it the full bamboo leaves ({@link FullBambooLeavesBlock}), or adds a layer to one. Twigs is
 * not on the compile path: {@code @Pseudo}, SRG names, {@code remap = false}; applied only with Twigs loaded.
 */
@Pseudo
@Mixin(targets = "com.ninni.twigs.block.BambooLeavesBlock", remap = false)
public abstract class TwigsBambooLeavesMixin {

	@Inject(method = "m_7417_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$holdToFall(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level,
										   BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
		try {
			if (FallingLayers.holdAndQueue(state, level, pos)) cir.setReturnValue(state);
		} catch (Throwable ignored) {
			// Twigs: an unsupported pile breaks
		}
	}

	@Inject(method = "m_6864_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$fifthLayer(BlockState state, BlockPlaceContext ctx, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) return;
		try {
			if (!ctx.isSecondaryUseActive() && ctx.getItemInHand().is(state.getBlock().asItem())
					&& FallingLayers.layers(state) == FullBambooLeavesBlock.TWIGS_MAX
					&& FullBambooLeavesBlock.state(FullBambooLeavesBlock.TWIGS_MAX + 1) != null) {
				cir.setReturnValue(true);
			}
		} catch (Throwable ignored) {
			// Twigs' answer
		}
	}

	@Inject(method = "m_5573_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$fullPile(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		try {
			BlockState there = ctx.getLevel().getBlockState(ctx.getClickedPos());
			if (!FallingLayers.isBambooLeaves(there)) return;
			int n = FallingLayers.layers(there);
			boolean full = there.getBlock() instanceof FullBambooLeavesBlock;
			if (!full && n < FullBambooLeavesBlock.TWIGS_MAX) return; // Twigs adds its own layer
			if (n >= FullBambooLeavesBlock.MAX) return;
			BlockState to = FallingLayers.withLayers(there, n + 1);
			if (to == null) return;
			if (there.hasProperty(BlockStateProperties.WATERLOGGED) && to.hasProperty(BlockStateProperties.WATERLOGGED)) {
				to = to.setValue(BlockStateProperties.WATERLOGGED, there.getValue(BlockStateProperties.WATERLOGGED));
			}
			cir.setReturnValue(to);
		} catch (Throwable ignored) {
			// Twigs' state
		}
	}
}
