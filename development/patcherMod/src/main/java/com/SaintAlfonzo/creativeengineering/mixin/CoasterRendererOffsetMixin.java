package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CounterToppers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.client.renderer.CoasterBlockEntityRenderer;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;

/**
 * The Brewin' and Chewin' coaster (the pack's port) on a Cooking for Blockheads counter ({@link CounterToppers}, TODO
 * cooking/kitchen #20). The coaster has no block model: its renderer draws the coaster and the drinks on it, so the whole
 * thing moves here, push + translate at the start, pop at every return. The port jar is a compileOnly lib
 * ({@code libs/backportedBrewin-*.jar}, copied from {@code mods/}).
 */
@Mixin(value = CoasterBlockEntityRenderer.class, remap = false)
public abstract class CoasterRendererOffsetMixin {

    private static final String RENDER = "render(Lumpaz/brewinandchewin/common/block/entity/CoasterBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void ce$offsetStart(CoasterBlockEntity coaster, float tickDelta, PoseStack poseStack,
                                MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        poseStack.pushPose();
        if (coaster.getLevel() != null) {
            Vec3 offset = CounterToppers.offset(coaster.getBlockState().getBlock(), coaster.getLevel(), coaster.getBlockPos());
            poseStack.translate(offset.x, offset.y, offset.z);
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void ce$offsetEnd(CoasterBlockEntity coaster, float tickDelta, PoseStack poseStack,
                              MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        poseStack.popPose();
    }
}
