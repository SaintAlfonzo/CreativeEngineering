package com.SaintAlfonzo.creativedelight.mixin.supplementaries;

import com.SaintAlfonzo.creativedelight.compat.supplementaries.FaucetFit;
import com.mojang.blaze3d.vertex.PoseStack;

import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FaucetBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Supplementaries 2.4.20 faucet stream fits the containers around it ({@link FaucetFit}, TODO cooking/kitchen #26):
 * the stream moves with the faucet model (FaucetFitModel) toward the drained block, and ends on the top of the block
 * below instead of after a fixed 16 segments. Client only; the renderer runs on the render thread, so the values
 * worked out at the head of a call are kept in fields for the rest of that call.
 */
@Mixin(value = FaucetBlockTileRenderer.class, remap = false)
public abstract class FaucetStreamFitMixin {

	@Unique private double ce$dx;
	@Unique private double ce$dz;
	@Unique private int ce$segments = FaucetFit.UPSTREAM_SEGMENTS;

	private static final String RENDER = "render(Lnet/mehvahdjukaar/supplementaries/common/block/tiles/FaucetBlockTile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

	@Inject(method = RENDER, at = @At("HEAD"), require = 1)
	private void ce$fit(FaucetBlockTile tile, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
		BlockEntity be = tile;
		Level level = be.getLevel();
		if (level == null) {
			ce$dx = 0;
			ce$dz = 0;
			ce$segments = FaucetFit.UPSTREAM_SEGMENTS;
			return;
		}
		double[] shift = FaucetFit.shift(level, be.getBlockPos(), be.getBlockState());
		ce$dx = shift[0];
		ce$dz = shift[1];
		ce$segments = FaucetFit.segments(level, be.getBlockPos());
	}

	@Redirect(method = RENDER, require = 1, at = @At(value = "INVOKE", ordinal = 0, remap = false,
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85837_(DDD)V"))
	private void ce$shiftStream(PoseStack pose, double x, double y, double z) {
		pose.translate(x + ce$dx, y, z + ce$dz);
	}

	@ModifyConstant(method = RENDER, require = 1, constant = @Constant(intValue = 16))
	private int ce$streamLength(int upstream) {
		return ce$segments;
	}
}
