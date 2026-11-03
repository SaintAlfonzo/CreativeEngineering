package com.SaintAlfonzo.baby_mobs.mixin.client;

import com.SaintAlfonzo.baby_mobs.client.kit.BabyKit;
import com.SaintAlfonzo.baby_mobs.client.kit.BabyRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Layer swaps for remodelled babies ({@code BabyKit.layerTexture / layerModel}), keyed by the adult layer texture.
 * m_117359_ coloredCutoutModelCopyLayerRender (layer with its own model: wool, cat collar, drowned outer layer) and
 * m_117376_ renderColoredCutoutModel (parent model, own texture: wolf collar, horse markings, villager clothes).
 */
@Mixin(RenderLayer.class)
public abstract class RenderLayerBabyMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "m_117359_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void babyMobs$copyLayer(EntityModel parent, EntityModel model, ResourceLocation texture, PoseStack pose,
                                           MultiBufferSource buffers, int light, LivingEntity entity, float limbSwing,
                                           float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                           float partialTicks, float red, float green, float blue, CallbackInfo ci) {
        BabyKit.LayerSwap swap = BabyRender.layerSwap(texture);
        if (swap == null) return;
        ci.cancel();
        if (entity.isInvisible()) return;
        EntityModel baby = BabyRender.layerModel(swap);
        EntityModel m = baby != null ? baby : model;
        parent.copyPropertiesTo(m);
        m.young = false;
        m.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        m.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(swap.texture()));
        m.renderToBuffer(pose, vc, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), red, green, blue, 1.0F);
    }

    @ModifyVariable(method = "m_117376_", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private static ResourceLocation babyMobs$layerTexture(ResourceLocation texture) {
        return BabyRender.layerTexture(texture);
    }
}
