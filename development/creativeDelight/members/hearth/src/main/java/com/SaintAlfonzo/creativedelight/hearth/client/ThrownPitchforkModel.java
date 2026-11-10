package com.SaintAlfonzo.creativedelight.hearth.client;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ThrownPitchforkModel extends Model {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(HearthAndHarvestMod.MODID, "thrown_pitchfork"), "main");
    public static final ResourceLocation TEXTURE = new ResourceLocation(HearthAndHarvestMod.MODID, "textures/entity/pitchfork.png");
    private final ModelPart main;

    public ThrownPitchforkModel(ModelPart root) {
        super(RenderType::entityCutout);
        main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        CubeDeformation none = new CubeDeformation(0.0F);
        mesh.getRoot().addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -24.0F, -0.5F, 1.0F, 24.0F, 1.0F, none)
                .texOffs(5, 0).addBox(-2.5F, -25.0F, -0.5F, 5.0F, 1.0F, 1.0F, none)
                .texOffs(5, 3).addBox(1.5F, -30.0F, -0.5F, 1.0F, 5.0F, 1.0F, none)
                .texOffs(5, 3).addBox(-2.5F, -30.0F, -0.5F, 1.0F, 5.0F, 1.0F, none)
                .texOffs(5, 3).addBox(-0.5F, -30.0F, -0.5F, 1.0F, 5.0F, 1.0F, none), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        main.render(pose, buffer, light, overlay, r, g, b, a);
    }
}
