package com.SaintAlfonzo.creativedelight.hearth.client;

import com.SaintAlfonzo.creativedelight.hearth.entity.HHEntities;
import com.SaintAlfonzo.creativedelight.hearth.entity.ThrownCleaver;
import com.SaintAlfonzo.creativedelight.hearth.entity.ThrownPitchfork;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client-only registration; reached through DistExecutor. */
public final class HHClient {
    private HHClient() {
    }

    public static void init(IEventBus bus) {
        bus.addListener(HHClient::renderers);
        bus.addListener(HHClient::layers);
        com.SaintAlfonzo.creativedelight.hearth.machine.client.HHMachineClient.init(bus);
        com.SaintAlfonzo.creativedelight.hearth.client.crow.CrowClient.init(bus);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HHEntities.THROWN_CLEAVER.get(), CleaverRenderer::new);
        event.registerEntityRenderer(HHEntities.THROWN_PITCHFORK.get(), PitchforkRenderer::new);
        event.registerEntityRenderer(HHEntities.MANURE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(HHEntities.THROWN_HORSESHOE.get(), HorseshoeRenderer::new);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ThrownPitchforkModel.LAYER, ThrownPitchforkModel::createBodyLayer);
    }

    private static final class HorseshoeRenderer extends EntityRenderer<com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe> {
        private final ItemRenderer items;

        HorseshoeRenderer(EntityRendererProvider.Context context) {
            super(context);
            items = context.getItemRenderer();
        }

        @Override
        public void render(com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.scale(0.6F, 0.6F, 0.6F);
            pose.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
            pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            if (!entity.isStuck()) {
                pose.mulPose(Vector3f.ZP.rotationDegrees((entity.tickCount + partialTick) * 36.0F));
            }
            items.renderStatic(entity.getShoe(), ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, pose, buffer, entity.getId());
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(com.SaintAlfonzo.creativedelight.hearth.gear.ThrownHorseshoe entity) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
    }

    private static final class CleaverRenderer extends EntityRenderer<ThrownCleaver> {
        private final ItemRenderer items;

        CleaverRenderer(EntityRendererProvider.Context context) {
            super(context);
            items = context.getItemRenderer();
        }

        @Override
        public void render(ThrownCleaver entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
            float yr = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            float rad = yr * Mth.DEG_TO_RAD;
            pose.pushPose();
            pose.scale(0.5F, 0.5F, 0.5F);
            if (entity.isStuck()) {
                pose.mulPose(Vector3f.YP.rotationDegrees(yr + 90.0F));
                pose.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            } else {
                float spin = (entity.tickCount + partialTick) * 54.0F;
                pose.mulPose(new Quaternion(new Vector3f(Mth.cos(rad), 0.0F, -Mth.sin(rad)), spin, true));
                pose.mulPose(Vector3f.YP.rotationDegrees(yr - 90.0F));
            }
            items.renderStatic(entity.getTool(), ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, pose, buffer, entity.getId());
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(ThrownCleaver entity) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
    }

    private static final class PitchforkRenderer extends EntityRenderer<ThrownPitchfork> {
        private final ThrownPitchforkModel model;

        PitchforkRenderer(EntityRendererProvider.Context context) {
            super(context);
            model = new ThrownPitchforkModel(context.bakeLayer(ThrownPitchforkModel.LAYER));
        }

        @Override
        public void render(ThrownPitchfork entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int light) {
            pose.pushPose();
            pose.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
            pose.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));
            model.renderToBuffer(pose, buffer.getBuffer(RenderType.entityCutout(ThrownPitchforkModel.TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffer, light);
        }

        @Override
        public ResourceLocation getTextureLocation(ThrownPitchfork entity) {
            return ThrownPitchforkModel.TEXTURE;
        }
    }
}
