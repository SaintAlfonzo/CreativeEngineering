package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassValues;
import com.SaintAlfonzo.creativecompat.surface.SnowLayers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snow layers as walk-through blocks (plan_post_scratch_1115.md G7.4, {@link SnowLayers}): HEAD of
 * {@code getCollisionShape} ({@code m_5939_}) gives a living entity no collision in snow layers 1 to 7 (or the full
 * layer height when it stands on top in leather boots); RETURN of {@code getStateForPlacement} ({@code m_5573_}) and
 * HEAD of {@code randomTick} ({@code m_213898_}) turn a full snow layer into a snow block. Other
 * {@link SnowLayerBlock}s (salt layers) are not in the layer tier and stay vanilla. Vanilla-class target,
 * {@code remap = false}.
 */
@Mixin(value = SnowLayerBlock.class, remap = false)
public abstract class SnowLayerPassMixin {

	@Inject(method = "m_5939_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$walkThrough(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx,
											CallbackInfoReturnable<VoxelShape> cir) {
		VoxelShape shape;
		try {
			shape = SnowLayers.collision(state, level, pos, ctx);
		} catch (Throwable t) {
			shape = null; // vanilla collision if the lookup breaks
		}
		if (shape != null) cir.setReturnValue(shape);
	}

	@Inject(method = "m_5573_", at = @At("RETURN"), cancellable = true)
	private void creativecompat$fullToBlock(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockState s = cir.getReturnValue();
		if (s == null) return;
		try {
			BlockState to = SnowLayers.fullToBlock(s, PassValues.of(ctx.getLevel().isClientSide));
			if (to != s) cir.setReturnValue(to);
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("SnowLayerPassMixin#1", t);
			// place the layer as vanilla
		}
	}

	@Inject(method = "m_213898_", at = @At("HEAD"), cancellable = true)
	private void creativecompat$fullToBlockTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random,
												CallbackInfo ci) {
		try {
			if (SnowLayers.randomTick(state, level, pos)) ci.cancel();
		} catch (Throwable t) {
			com.SaintAlfonzo.creativecompat.compat.HookFaults.once("SnowLayerPassMixin#2", t);
			// tick as vanilla
		}
	}
}
