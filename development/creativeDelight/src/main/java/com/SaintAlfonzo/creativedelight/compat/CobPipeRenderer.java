package com.SaintAlfonzo.creativedelight.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * The Cob Pipe drawn on the head from a Curios slot, as the vanilla head slot draws it (its model's "head" transform).
 * Ported from Corn Delight 1.2.11's CobpipeRenderer (MIT) to 1.19.2. Client only, and only when Curios is loaded.
 */
public final class CobPipeRenderer implements ICurioRenderer {

    public static void register() {
        Item pipe = ForgeRegistries.ITEMS.getValue(CornDelightCompat.COB_PIPE);
        if (pipe != null && CornDelightCompat.COB_PIPE.equals(ForgeRegistries.ITEMS.getKey(pipe))) {
            CuriosRendererRegistry.register(pipe, CobPipeRenderer::new);
        }
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext,
            PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity entity = slotContext.entity();
        poseStack.pushPose();
        if (entity.isBaby() && !(entity instanceof Villager)) {
            poseStack.translate(0.0F, 0.03125F, 0.0F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            poseStack.translate(0.0F, 1.0F, 0.0F);
        }
        followHead(entity, poseStack);
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(entity, stack, ItemTransforms.TransformType.HEAD, false,
                poseStack, buffer, entity.level, light, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), entity.getId());
        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    private static void followHead(LivingEntity entity, PoseStack poseStack) {
        if (Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer<?, ?> renderer
                && renderer.getModel() instanceof HumanoidModel<?> model) {
            // rotationZYX(z, y, x): z, then y, then x
            poseStack.mulPose(Vector3f.ZP.rotation(model.head.zRot));
            poseStack.mulPose(Vector3f.YP.rotation(model.head.yRot));
            poseStack.mulPose(Vector3f.XP.rotation(model.head.xRot));
        }
    }
}
