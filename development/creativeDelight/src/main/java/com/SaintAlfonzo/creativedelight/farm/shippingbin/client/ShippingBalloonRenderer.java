package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBalloonEntity;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** The balloon: crate, ropes and envelope, inflating over its first ticks. */
public class ShippingBalloonRenderer extends EntityRenderer<ShippingBalloonEntity> {
	static final ResourceLocation TEXTURE = ShippingBin.id("textures/entity/shipping_balloon.png");
	private final ShippingModels.Model<ShippingBalloonEntity> model;

	public ShippingBalloonRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new ShippingModels.Model<>(context.bakeLayer(ShippingModels.BALLOON));
		shadowRadius = 0.5F;
	}

	@Override
	public void render(ShippingBalloonEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		pose.pushPose();
		pose.mulPose(Vector3f.YP.rotationDegrees(-yaw));
		model.inflate((entity.tickCount + partialTick) / ShippingBalloonEntity.INFLATE_TICKS);
		model.renderToBuffer(pose, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		pose.popPose();
		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(ShippingBalloonEntity entity) {
		return TEXTURE;
	}
}
