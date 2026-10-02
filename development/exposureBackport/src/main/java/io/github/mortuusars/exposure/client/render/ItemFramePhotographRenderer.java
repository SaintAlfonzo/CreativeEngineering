package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Registry;

public class ItemFramePhotographRenderer {
   public static void render(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, PhotographItem item, ItemStack stack) {
      if (itemFrame.getType() == EntityType.GLOW_ITEM_FRAME) {
         packedLight = 15728880;
      }

      poseStack.pushPose();
      String entityName = Registry.ENTITY_TYPE.getKey(itemFrame.getType()).toString();
      if (entityName.equals("quark:glass_frame")) {
         poseStack.translate(0.0F, 0.0F, 0.475F);
      }

      poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)(45 * itemFrame.getRotation())));
      float pixelSize = 0.0625F;
      float scale = 1.0F - pixelSize * 6.0F;
      poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      poseStack.scale(scale, scale, scale);
      poseStack.translate(-0.5, -0.5, 0.045);
      ExposureClient.photographRenderer().renderPhotograph(poseStack, bufferSource, item, stack, false, false, packedLight, 255, 255, 255, 255);
      poseStack.popPose();
   }
}
