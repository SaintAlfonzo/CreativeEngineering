package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.client.BreakOverlayClip;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Break overlay clip (R4, plan_small_fixes_1116.md): in Forge's {@code renderBreakingTexture} (the crack overlay pass),
 * the model it looks up (the first BakedModel local) loses its quads outside the block box for blocks in
 * {@code #creativedelight:clip_break_overlay} (straw, hay and canvas rugs). The method is Forge's own, so no remap.
 */
@Mixin(BlockRenderDispatcher.class)
public abstract class BreakOverlayClipMixin {
	@ModifyVariable(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraftforge/client/model/data/ModelData;)V",
			at = @At("STORE"), ordinal = 0, remap = false)
	private BakedModel creativedelight$clipBreakOverlay(BakedModel model, BlockState state) {
		return BreakOverlayClip.wrap(state, model);
	}
}
