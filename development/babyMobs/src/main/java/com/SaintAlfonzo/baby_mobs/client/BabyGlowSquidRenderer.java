package com.SaintAlfonzo.baby_mobs.client;

import com.SaintAlfonzo.baby_mobs.BabyMobs;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;

/** {@link BabySquidRenderer} for the glow squid: its texture pair and vanilla's glow light level. */
public class BabyGlowSquidRenderer extends BabySquidRenderer<GlowSquid> {
    private static final ResourceLocation ADULT = new ResourceLocation("textures/entity/squid/glow_squid.png");
    private static final ResourceLocation BABY = new ResourceLocation(BabyMobs.ID, "textures/entity/squid/glow_squid_baby.png");

    public BabyGlowSquidRenderer(EntityRendererProvider.Context context) {
        super(context, new SquidModel<>(context.bakeLayer(ModelLayers.GLOW_SQUID)), new SquidModel<>(context.bakeLayer(BabyModels.GLOW_SQUID)));
    }

    @Override
    protected ResourceLocation babyTexture() {
        return BABY;
    }

    @Override
    public ResourceLocation getTextureLocation(GlowSquid squid) {
        return babyModel(squid) ? BABY : ADULT;
    }

    @Override
    protected int getBlockLightLevel(GlowSquid squid, BlockPos pos) {
        int light = (int) Mth.clampedLerp(0.0F, 15.0F, 1.0F - (float) squid.getDarkTicksRemaining() / 10.0F);
        return light == 15 ? 15 : Math.max(light, super.getBlockLightLevel(squid, pos));
    }
}
