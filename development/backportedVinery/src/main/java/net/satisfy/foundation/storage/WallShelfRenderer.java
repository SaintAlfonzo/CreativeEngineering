package net.satisfy.foundation.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.satisfy.foundation.render.ClientUtil;

public class WallShelfRenderer implements StorageTypeRenderer {
    @Override
    public void render(StorageBlockEntity entity, PoseStack poseStack, MultiBufferSource buffers, NonNullList<ItemStack> items) {
        poseStack.translate(-0.4, 0.5, 0.25);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.0F, 0.2F * i);
                poseStack.mulPose(Vector3f.YN.rotationDegrees(22.5F));
                ClientUtil.renderItem(stack, poseStack, buffers, entity);
                poseStack.popPose();
            }
        }
    }
}
