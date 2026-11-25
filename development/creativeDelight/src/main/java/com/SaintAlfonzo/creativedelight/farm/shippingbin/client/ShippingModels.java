package com.SaintAlfonzo.creativedelight.farm.shippingbin.client;

import com.SaintAlfonzo.creativedelight.farm.shippingbin.ShippingBin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

/**
 * The balloon and the crate, built from their shapes for the donor's {@code textures/entity/shipping_balloon.png}
 * (128 x 128): a 13 px crate, four ropes up its corners and the envelope (a 15 px base under a 21 px body). Model units
 * have y up, as the renderers draw them unflipped.
 */
public final class ShippingModels {
	public static final ModelLayerLocation BALLOON = new ModelLayerLocation(ShippingBin.id("shipping_balloon"), "main");
	public static final ModelLayerLocation CRATE = new ModelLayerLocation(ShippingBin.id("shipping_crate"), "main");

	private ShippingModels() {
	}

	private static void crate(PartDefinition root) {
		root.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 57).addBox(-6.5F, 3.25F, -6.5F, 13, 13, 13), PartPose.ZERO);
	}

	private static CubeListBuilder rope(float x, float z) {
		return CubeListBuilder.create()
				.texOffs(2, 12).addBox(x, 16.25F, z, 2, 1, 2)
				.texOffs(2, 12).addBox(x, 19.25F, z, 2, 1, 2)
				.texOffs(2, 12).addBox(x, 23.25F, z, 2, 1, 2)
				.texOffs(28, 0).addBox(x + 0.5F, 17.25F, z + 0.5F, 1, 9, 1);
	}

	public static LayerDefinition balloonLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		crate(root);
		root.addOrReplaceChild("rope", rope(4.5F, -6.5F), PartPose.ZERO);
		root.addOrReplaceChild("rope2", rope(4.5F, 4.5F), PartPose.ZERO);
		root.addOrReplaceChild("rope3", rope(-6.5F, 4.5F), PartPose.ZERO);
		root.addOrReplaceChild("rope4", rope(-6.5F, -6.5F), PartPose.ZERO);
		root.addOrReplaceChild("balloon", CubeListBuilder.create()
				.texOffs(53, 0).addBox(-7.5F, 26.25F, -7.5F, 15, 2, 15)
				.texOffs(21, 21).addBox(-10.5F, 28.25F, -10.5F, 21, 15, 21), PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128);
	}

	public static LayerDefinition crateLayer() {
		MeshDefinition mesh = new MeshDefinition();
		crate(mesh.getRoot());
		return LayerDefinition.create(mesh, 128, 128);
	}

	/** One model class for both layers; {@link #inflate} scales the envelope and ropes (balloon layer only). */
	public static class Model<T extends Entity> extends EntityModel<T> {
		private final ModelPart root;

		public Model(ModelPart root) {
			this.root = root;
		}

		/** 0 = flat in the crate, 1 = full. Scales the parts above the crate up from its top. */
		public void inflate(float amount) {
			float a = Math.max(0.05F, Math.min(1.0F, amount));
			for (String name : new String[]{"balloon", "rope", "rope2", "rope3", "rope4"}) {
				if (!root.hasChild(name)) continue;
				ModelPart part = root.getChild(name);
				part.yScale = a;
				part.xScale = part.zScale = name.equals("balloon") ? 0.4F + 0.6F * a : 1.0F;
				part.y = 16.25F * (1.0F - a); // keep the scaled parts sitting on the crate top
			}
		}

		@Override
		public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		}

		@Override
		public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
			root.render(pose, buffer, light, overlay, r, g, b, a);
		}
	}
}
