package com.SaintAlfonzo.more_mooblooms.client;

import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Draws the flowers on a grown, unsheared adult (O10, feature_writeup/mooblooms.md 4.7): one block model
 * per fixed slot, each slot's flower is the moobloom's own roll ({@link Moobloom#getFlowers}, synced). Built the way vanilla's
 * mooshroom mushroom layer works (block models on the cow model, outline-only when invisible but glowing); the
 * positions and angles are ours. Each slot is anchored at the flower's base, so {@code scale} shrinks a flower
 * towards the back instead of floating it.
 *
 * Tuning (D19): x left/right, y up is negative, z towards the tail is positive, in blocks; the cow's back is at
 * about y 0.13 and runs from z -0.5 (shoulders) to 0.6 (rump).
 */
public class MoobloomFlowerLayer extends RenderLayer<Moobloom, MoobloomModel> {

    /** Base position and turn of each slot; HEAD is in head space (moves with the head). */
    private record SlotPose(float x, float y, float z, float yaw) {}

    private static final SlotPose BACK_FRONT = new SlotPose(-0.12F, 0.15F, -0.22F, -40.0F);
    private static final SlotPose BACK_REAR = new SlotPose(0.14F, 0.15F, 0.36F, 35.0F);
    private static final SlotPose HEAD = new SlotPose(0.0F, -0.2F, -0.18F, -70.0F);

    private final BlockRenderDispatcher blockRenderer;

    public MoobloomFlowerLayer(RenderLayerParent<Moobloom, MoobloomModel> parent, BlockRenderDispatcher blockRenderer) {
        super(parent);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Moobloom moobloom, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!moobloom.showsFlowers()) return;
        boolean outlineOnly = Minecraft.getInstance().shouldEntityAppearGlowing(moobloom) && moobloom.isInvisible();
        if (moobloom.isInvisible() && !outlineOnly) return;

        List<BlockState> flowers = moobloom.getFlowers();
        if (flowers.size() < MoobloomVariant.Slot.COUNT) return;
        int overlay = LivingEntityRenderer.getOverlayCoords(moobloom, 0.0F);
        float scale = moobloom.getVariant().scale();

        renderSlot(pose, buffers, light, overlay, outlineOnly, BACK_FRONT, flowers.get(MoobloomVariant.Slot.BACK_FRONT.ordinal()), scale);
        renderSlot(pose, buffers, light, overlay, outlineOnly, BACK_REAR, flowers.get(MoobloomVariant.Slot.BACK_REAR.ordinal()), scale);

        pose.pushPose();
        getParentModel().getHead().translateAndRotate(pose);
        renderSlot(pose, buffers, light, overlay, outlineOnly, HEAD, flowers.get(MoobloomVariant.Slot.HEAD.ordinal()), scale);
        pose.popPose();
    }

    private void renderSlot(PoseStack pose, MultiBufferSource buffers, int light, int overlay, boolean outlineOnly,
                            SlotPose slot, BlockState state, float scale) {
        if (state == null || state.isAir()) return;
        pose.pushPose();
        pose.translate(slot.x(), slot.y(), slot.z());
        pose.mulPose(Vector3f.YP.rotationDegrees(slot.yaw()));
        // flip to block space (the entity model is upside down), grow up from the base, centred on x/z
        pose.scale(-scale, -scale, scale);
        pose.translate(-0.5, 0.0, -0.5);
        if (outlineOnly) {
            blockRenderer.getModelRenderer().renderModel(pose.last(),
                    buffers.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), state,
                    blockRenderer.getBlockModel(state), 0.0F, 0.0F, 0.0F, light, overlay);
        } else {
            blockRenderer.renderSingleBlock(state, pose, buffers, light, overlay);
        }
        pose.popPose();
    }
}
