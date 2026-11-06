package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.family.EquineBabies;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Llama decor on a remodelled baby, as 26.1: no carpet, and a trader llama baby wears the baby trader decor (the baby
 * body inflated by 0.2 with equipment/llama_body/trader_llama_baby.png). 1.19.2 would draw the adult decor model at
 * full size (the young flag stays false while a remodelled baby renders). m_6494_ render.
 */
@Mixin(LlamaDecorLayer.class)
public abstract class EquineLlamaDecorMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "m_6494_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/animal/horse/Llama;FFFFFF)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$babyDecor(PoseStack pose, MultiBufferSource buffers, int light, Llama llama, float limbSwing,
                                    float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                                    float headPitch, CallbackInfo ci) {
        if (!BabyRender.active()) return;
        ci.cancel();
        if (!llama.isTraderLlama() || EquineBabies.traderKit == null) return;
        LlamaModel model = EquineBabies.traderKit.bake(EquineBabies.traderDecor, LlamaModel::new);
        ((RenderLayer) (Object) this).getParentModel().copyPropertiesTo(model);
        model.young = false;
        model.prepareMobModel(llama, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(llama, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(EquineBabies.TRADER_DECOR_TEXTURE));
        model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
