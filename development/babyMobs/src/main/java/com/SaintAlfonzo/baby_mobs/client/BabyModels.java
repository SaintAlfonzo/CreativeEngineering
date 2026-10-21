package com.SaintAlfonzo.baby_mobs.client;

import com.SaintAlfonzo.baby_mobs.BabyMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * The 26.1 (Tiny Takeover) baby geometry, part names as vanilla 1.19.2's {@code SquidModel} / {@code DolphinModel}
 * expect them, so those model classes render it unchanged. Textures: 32x32 squid, 64x64 dolphin (26.1's own files).
 */
public final class BabyModels {
    public static final ModelLayerLocation SQUID = new ModelLayerLocation(new ResourceLocation(BabyMobs.ID, "squid"), "baby");
    public static final ModelLayerLocation GLOW_SQUID = new ModelLayerLocation(new ResourceLocation(BabyMobs.ID, "glow_squid"), "baby");
    public static final ModelLayerLocation DOLPHIN = new ModelLayerLocation(new ResourceLocation(BabyMobs.ID, "dolphin"), "baby");

    private BabyModels() {}

    /** 26.1 {@code BabySquidModel.createBodyLayer}. */
    public static LayerDefinition squid() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        CubeListBuilder tentacle = CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F);
        for (int i = 0; i < 8; i++) {
            double angle = (double) i * Math.PI * 2.0 / 8.0;
            float x = (float) Math.cos(angle) * 3.0F;
            float z = (float) Math.sin(angle) * 3.0F;
            float yRot = (float) ((double) i * Math.PI * -2.0 / 8.0 + (Math.PI / 2));
            root.addOrReplaceChild("tentacle" + i, tentacle, PartPose.offsetAndRotation(x, 18.5F, z, 0.0F, yRot, 0.0F));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    /** 26.1 {@code BabyDolphinModel.createBodyLayer}. */
    public static LayerDefinition dolphin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(20, 0).addBox(-3.0F, -2.5F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 21.5F, 0.0F));
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.5F, -4.0F, 6.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, -4.0F));
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.5F, -4.0F));
        body.addOrReplaceChild("left_fin",
                CubeListBuilder.create().texOffs(34, 18).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, 1.7017F));
        body.addOrReplaceChild("right_fin",
                CubeListBuilder.create().texOffs(48, 18).mirror().addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-1.8F, 0.85F, -2.6F, 0.8727F, 0.0F, -1.7017F));
        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.0F, 4.0F));
        tail.addOrReplaceChild("tail_fin",
                CubeListBuilder.create().texOffs(22, 13).addBox(-4.0F, -0.5F, -1.0F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 6.0F));
        body.addOrReplaceChild("back_fin",
                CubeListBuilder.create().texOffs(42, 0).addBox(-0.5F, -1.0F, 1.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, -2.7F, 0.8727F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
