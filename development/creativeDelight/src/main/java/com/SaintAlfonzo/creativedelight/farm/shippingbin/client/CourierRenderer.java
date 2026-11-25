package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.CourierEntity;
import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * The courier: the villager model with the donor's merchant skin (ruling D5: MCA villager skins once those are in the pack),
 * holding its potion in crossed arms like the wandering trader.
 */
public class CourierRenderer extends MobRenderer<CourierEntity, VillagerModel<CourierEntity>> {
	private static final ResourceLocation TEXTURE = ShippingBin.id("textures/entity/merchant.png");

	public CourierRenderer(EntityRendererProvider.Context context) {
		super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
		addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()));
	}

	@Override
	public ResourceLocation getTextureLocation(CourierEntity entity) {
		return TEXTURE;
	}
}
