package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The 26.1 baby models for every mob with a {@code BabyKit} (client/family): while a baby renders, the renderer's
 * model is the baby model, the young flag stays false (no 1.19.2 half-size shrink of an already baby-sized mesh), the
 * kit's texture and pose apply. SRG names, remap = false: m_7392_ render, f_115290_ model, f_102610_ young,
 * m_7546_ scale, m_7225_ getRenderType.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererBabyMixin {

    private static final String RENDER = "m_7392_(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Shadow(remap = false)
    protected EntityModel<?> f_115290_;

    @Unique
    private EntityModel<?> babyMobs$adult;

    @Inject(method = RENDER, at = @At("HEAD"), remap = false)
    private void babyMobs$begin(LivingEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                                int light, CallbackInfo ci) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        EntityModel<?> baby = BabyRender.begin(entity, f_115290_, () -> ((LivingEntityRenderer) (Object) this).getTextureLocation(entity));
        if (baby != null) {
            babyMobs$adult = f_115290_;
            f_115290_ = baby;
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"), remap = false)
    private void babyMobs$end(LivingEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                              int light, CallbackInfo ci) {
        if (babyMobs$adult != null) {
            f_115290_ = babyMobs$adult;
            babyMobs$adult = null;
        }
        BabyRender.end();
    }

    @Inject(method = RENDER, at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/EntityModel;f_102610_:Z",
            opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER), remap = false)
    private void babyMobs$notYoung(LivingEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                                   int light, CallbackInfo ci) {
        if (BabyRender.active()) f_115290_.young = false;
    }

    @Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;m_7546_(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            shift = At.Shift.AFTER), remap = false)
    private void babyMobs$pose(LivingEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                               int light, CallbackInfo ci) {
        BabyRender.pose(pose);
    }

    @ModifyVariable(method = "m_7225_", at = @At("STORE"), ordinal = 0, remap = false)
    private ResourceLocation babyMobs$texture(ResourceLocation texture) {
        return BabyRender.texture(texture);
    }
}
