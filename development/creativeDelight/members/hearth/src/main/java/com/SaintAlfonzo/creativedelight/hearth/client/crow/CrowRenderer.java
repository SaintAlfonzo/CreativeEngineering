package com.SaintAlfonzo.creativedelight.hearth.client.crow;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.crow.CrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Hearth and Harvest 1.4.1 crow renderer: the flight pose tilts and banks the body. */
public class CrowRenderer extends MobRenderer<CrowEntity, CrowModel<CrowEntity>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(HearthAndHarvestMod.MODID, "textures/entity/crow.png");

    public CrowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrowModel<>(context.bakeLayer(CrowModel.LAYER_LOCATION)), 0.15F);
        addLayer(new CrowHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CrowEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(CrowEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick);
        float pitch = entity.getFlightPose().pitch(partialTick);
        float roll = entity.getFlightPose().roll(partialTick);
        if (Math.abs(pitch) >= 0.01F || Math.abs(roll) >= 0.01F) {
            float pivot = entity.getBbHeight() * 0.5F;
            poseStack.translate(0.0D, pivot, 0.0D);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-roll));
            poseStack.translate(0.0D, -pivot, 0.0D);
        }
    }
}
