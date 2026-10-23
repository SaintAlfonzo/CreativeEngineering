package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CuttingBoardOnCounter;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.renderer.CuttingBoardRenderer;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

/**
 * The item lying on a Farmer's Delight cutting board moves with the board's model offset on a Cooking for Blockheads
 * counter ({@link CuttingBoardOnCounter}, TODO cooking/kitchen #8): push + translate at the start, pop at every return.
 */
@Mixin(value = CuttingBoardRenderer.class, remap = false)
public abstract class CuttingBoardRendererOffsetMixin {

    @Inject(method = "render(Lvectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"))
    private void ce$offsetStart(CuttingBoardBlockEntity board, float partialTicks, PoseStack poseStack,
                                MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        poseStack.pushPose();
        if (board.getLevel() != null) {
            Vec3 offset = CuttingBoardOnCounter.offset(board.getLevel(), board.getBlockPos());
            poseStack.translate(offset.x, offset.y, offset.z);
        }
    }

    @Inject(method = "render(Lvectorwing/farmersdelight/common/block/entity/CuttingBoardBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("RETURN"))
    private void ce$offsetEnd(CuttingBoardBlockEntity board, float partialTicks, PoseStack poseStack,
                              MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        poseStack.popPose();
    }
}
