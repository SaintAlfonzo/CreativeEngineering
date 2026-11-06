package com.SaintAlfonzo.baby_mobs.client;

import com.SaintAlfonzo.baby_mobs.BMConfig;
import com.SaintAlfonzo.baby_mobs.BabyMobs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;

/** Vanilla's dolphin renderer with the 26.1 baby model and texture while the dolphin is young. */
public class BabyDolphinRenderer extends DolphinRenderer {
    private static final ResourceLocation BABY = new ResourceLocation(BabyMobs.ID, "textures/entity/dolphin/dolphin_baby.png");
    private final DolphinModel<Dolphin> adult;
    private final DolphinModel<Dolphin> baby;

    public BabyDolphinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.adult = this.model;
        this.baby = new DolphinModel<>(context.bakeLayer(BabyModels.DOLPHIN));
    }

    private boolean babyModel(Dolphin dolphin) {
        return dolphin.isBaby() && BMConfig.BABY_MODELS.get();
    }

    @Override
    public void render(Dolphin dolphin, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        this.model = babyModel(dolphin) ? baby : adult;
        super.render(dolphin, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Dolphin dolphin) {
        return babyModel(dolphin) ? BABY : super.getTextureLocation(dolphin);
    }

    @Override
    protected void scale(Dolphin dolphin, PoseStack pose, float partialTicks) {
        if (babyModel(dolphin)) this.model.young = false; // the baby mesh is already baby-sized
    }
}
