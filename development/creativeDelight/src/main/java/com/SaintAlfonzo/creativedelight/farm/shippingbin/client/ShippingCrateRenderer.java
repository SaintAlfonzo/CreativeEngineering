package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** The crate on the bin, on a courier, or falling from a popped balloon: the balloon's crate alone, bottom at the entity's feet. */
public class ShippingCrateRenderer<T extends Entity> extends EntityRenderer<T> {
	private final ShippingModels.Model<T> model;

	public ShippingCrateRenderer(EntityRendererProvider.Context context) {
		super(context);
		model = new ShippingModels.Model<>(context.bakeLayer(ShippingModels.CRATE));
		shadowRadius = 0.4F;
	}

	@Override
	public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		if (entity.isInvisible()) return;
		pose.pushPose();
		pose.mulPose(Vector3f.YP.rotationDegrees(-yaw));
		pose.translate(0.0D, -3.25D / 16.0D, 0.0D);
		model.renderToBuffer(pose, buffers.getBuffer(model.renderType(ShippingBalloonRenderer.TEXTURE)), light, OverlayTexture.NO_OVERLAY,
				1.0F, 1.0F, 1.0F, 1.0F);
		pose.popPose();
		super.render(entity, yaw, partialTick, pose, buffers, light);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return ShippingBalloonRenderer.TEXTURE;
	}
}
