package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Held item of a remodelled baby fox at the 26.1 place: 26.1 moves to the (baby) head first and scales 0.75 there;
 * 1.19.2 scales 0.75 first and adds a fixed offset (0, 0.5, 0.209375) made for its half-size adult-shaped baby. The
 * baby-only sleeping / awake offsets after the head rotation are the same in both. SRG names, remap = false:
 * m_6494_ render, m_85841_ PoseStack.scale, m_85837_ PoseStack.translate (ordinal 0 = the baby offset), m_28620_ Fox.getHeadRollAngle.
 */
@Mixin(FoxHeldItemLayer.class)
public abstract class PetFoxHeldItemMixin {

    private static final String RENDER = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/Fox;FFFFFF)V";

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85841_(FFF)V", ordinal = 0), remap = false)
    private void babyMobs$babyScale(PoseStack pose, float x, float y, float z) {
        if (!BabyRender.active()) pose.scale(x, y, z);
    }

    @Redirect(method = RENDER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85837_(DDD)V", ordinal = 0), remap = false)
    private void babyMobs$babyOffset(PoseStack pose, double x, double y, double z) {
        if (!BabyRender.active()) pose.translate(x, y, z);
    }

    /** Right after the move to the head (the next call is the head roll lookup), before the head rotations. */
    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Fox;m_28620_(F)F", ordinal = 0),
            remap = false)
    private void babyMobs$scaleAtHead(PoseStack pose, MultiBufferSource buffers, int light, Fox fox, float limbSwing, float limbSwingAmount,
                                      float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (BabyRender.active() && fox.isBaby()) pose.scale(0.75F, 0.75F, 0.75F);
    }
}
