package com.SaintAlfonzo.creativedelight.hearth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

/** The 3D pitchfork in hand; GUI, ground and frame use the flat model (models/item/pitchfork.json). */
public class PitchforkItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static PitchforkItemRenderer instance;
    private final ThrownPitchforkModel model;

    private PitchforkItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        model = new ThrownPitchforkModel(Minecraft.getInstance().getEntityModels().bakeLayer(ThrownPitchforkModel.LAYER));
    }

    public static PitchforkItemRenderer getInstance() {
        if (instance == null) instance = new PitchforkItemRenderer();
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType type, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        boolean charging = mc.player != null && mc.player.isUsingItem() && mc.player.getUseItem() == stack;
        boolean thirdPerson = type == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || type == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND;
        pose.pushPose();
        if (charging && thirdPerson) {
            pose.translate(0.0D, -1.3D, 0.0D);
        } else {
            pose.scale(1.0F, -1.0F, -1.0F);
        }
        model.renderToBuffer(pose, buffer.getBuffer(RenderType.entityCutout(ThrownPitchforkModel.TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }
}
