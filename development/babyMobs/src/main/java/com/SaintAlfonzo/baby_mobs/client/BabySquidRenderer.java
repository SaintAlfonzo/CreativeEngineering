package com.SaintAlfonzo.baby_mobs.client;

import com.SaintAlfonzo.baby_mobs.BMConfig;
import com.SaintAlfonzo.baby_mobs.BabyMobs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

/**
 * Vanilla's squid renderer with the 26.1 baby model and texture while the squid is young (26.1 {@code SquidRenderer}:
 * baby texture, half the rotation offsets). With {@code [client] babyModels} off the adult model renders at
 * {@code getScale} like any vanilla baby.
 */
public class BabySquidRenderer<T extends Squid> extends SquidRenderer<T> {
    private static final ResourceLocation BABY = new ResourceLocation(BabyMobs.ID, "textures/entity/squid/squid_baby.png");
    private final SquidModel<T> adult;
    private final SquidModel<T> baby;

    public BabySquidRenderer(EntityRendererProvider.Context context) {
        this(context, new SquidModel<>(context.bakeLayer(ModelLayers.SQUID)), new SquidModel<>(context.bakeLayer(BabyModels.SQUID)));
    }

    protected BabySquidRenderer(EntityRendererProvider.Context context, SquidModel<T> adult, SquidModel<T> baby) {
        super(context, adult);
        this.adult = adult;
        this.baby = baby;
    }

    protected boolean babyModel(T squid) {
        return squid.isBaby() && BMConfig.BABY_MODELS.get();
    }

    protected ResourceLocation babyTexture() {
        return BABY;
    }

    @Override
    public void render(T squid, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffers, int light) {
        this.model = babyModel(squid) ? baby : adult;
        super.render(squid, yaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(T squid) {
        return babyModel(squid) ? babyTexture() : super.getTextureLocation(squid);
    }

    @Override
    protected void scale(T squid, PoseStack pose, float partialTicks) {
        // the baby mesh is already baby-sized; vanilla would shrink it again by getScale through the young flag
        if (babyModel(squid)) this.model.young = false;
    }

    @Override
    protected void setupRotations(T squid, PoseStack pose, float ageInTicks, float bodyYaw, float partialTicks) {
        if (!babyModel(squid)) {
            super.setupRotations(squid, pose, ageInTicks, bodyYaw, partialTicks);
            return;
        }
        float xBody = Mth.lerp(partialTicks, squid.xBodyRotO, squid.xBodyRot);
        float zBody = Mth.lerp(partialTicks, squid.zBodyRotO, squid.zBodyRot);
        pose.translate(0.0D, 0.25D, 0.0D);
        pose.mulPose(Vector3f.YP.rotationDegrees(180.0F - bodyYaw));
        pose.mulPose(Vector3f.XP.rotationDegrees(xBody));
        pose.mulPose(Vector3f.YP.rotationDegrees(zBody));
        pose.translate(0.0D, -0.6D, 0.0D);
    }
}
