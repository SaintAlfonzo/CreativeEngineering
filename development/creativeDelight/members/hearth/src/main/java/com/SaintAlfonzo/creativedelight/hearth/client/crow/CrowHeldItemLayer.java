package com.SaintAlfonzo.creativedelight.hearth.client.crow;

import com.SaintAlfonzo.creativedelight.hearth.crow.CrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemStack;

/** The item in the crow's beak (Hearth and Harvest 1.4.1). */
public class CrowHeldItemLayer extends RenderLayer<CrowEntity, CrowModel<CrowEntity>> {
    private static final float BEAK_Y = 0.015625F;
    private static final float BEAK_Z = -0.203125F;
    private static final float ITEM_SCALE = 0.5F;
    private final ItemInHandRenderer itemInHandRenderer;

    public CrowHeldItemLayer(RenderLayerParent<CrowEntity, CrowModel<CrowEntity>> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CrowEntity crow, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = crow.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        getParentModel().translateToBeak(poseStack);
        poseStack.translate(0.0D, BEAK_Y, BEAK_Z);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        itemInHandRenderer.renderItem(crow, stack, ItemTransforms.TransformType.GROUND, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
