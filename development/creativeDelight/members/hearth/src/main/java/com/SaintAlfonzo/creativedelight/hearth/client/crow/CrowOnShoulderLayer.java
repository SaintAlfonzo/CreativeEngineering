package com.SaintAlfonzo.creativedelight.hearth.client.crow;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowShoulder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/** A crow riding a player's shoulder (Hearth and Harvest 1.4.1). Added to both player skins. */
public class CrowOnShoulderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final CrowModel<?> crowModel;

    public CrowOnShoulderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.crowModel = new CrowModel<>(modelSet.bakeLayer(CrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        renderCrow(poseStack, buffer, packedLight, player, true, netHeadYaw, headPitch);
        renderCrow(poseStack, buffer, packedLight, player, false, netHeadYaw, headPitch);
    }

    private void renderCrow(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player, boolean left, float netHeadYaw, float headPitch) {
        CompoundTag tag = left ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (!CrowShoulder.isCrow(tag)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(left ? 0.4D : -0.35D, player.isCrouching() ? -1.3D : -1.5D, 0.0D);
        crowModel.root().getAllParts().forEach(ModelPart::resetPose);
        crowModel.head.yRot = Mth.clamp(netHeadYaw, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
        crowModel.head.xRot = Mth.clamp(headPitch, -25.0F, 45.0F) * Mth.DEG_TO_RAD;
        VertexConsumer consumer = buffer.getBuffer(crowModel.renderType(CrowRenderer.TEXTURE));
        crowModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
