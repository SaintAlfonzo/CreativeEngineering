package com.SaintAlfonzo.creativedelight.barbeques.client;

import com.SaintAlfonzo.creativedelight.barbeques.block.GrillBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** Skewers lying on the grill, turned over once flipped (1.3.0 GrillBlockEntityRenderer). */
public class GrillRenderer implements BlockEntityRenderer<GrillBlockEntity> {

    public GrillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GrillBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        if (entity.getLevel() == null) {
            return;
        }
        Direction direction = entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        int lightAbove = LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().above());
        for (int i = 0; i < entity.size(); i++) {
            ItemStack stack = entity.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(0.5, 0.96, 0.5);
            pose.mulPose(Vector3f.YP.rotationDegrees(-direction.toYRot()));
            pose.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            pose.translate(entity.getOffset(i), 0.0, 0.0);
            pose.scale(0.4F, 0.4F, 0.4F);
            pose.mulPose(Vector3f.YP.rotationDegrees(entity.isFlipped(i) ? 180.0F : 0.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, lightAbove, overlay,
                    pose, buffer, (int) entity.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
