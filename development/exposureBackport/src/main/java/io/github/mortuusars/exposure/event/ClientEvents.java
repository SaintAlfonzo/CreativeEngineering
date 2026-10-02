package io.github.mortuusars.exposure.event;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.ItemFramePhotographRenderer;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.decoration.ItemFrame;

public class ClientEvents {
   public static void levelUnloaded() {
   }

   public static void login() {
   }

   public static void disconnect() {
      resetRenderData();
   }

   public static void resetRenderData() {
      ExposureClient.exposureStore().clear();
      ExposureClient.renderedExposures().clearCache();
      ExposureClient.imageRenderer().clearCache();
   }

   public static boolean renderItemFrameItem(ItemFrame itemFrame, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      if (!(Boolean)Config.Client.PHOTOGRAPH_RENDERS_IN_ITEM_FRAME.get()) {
         return false;
      } else if (itemFrame.getItem().getItem() instanceof PhotographItem photographItem) {
         if (photographItem.getFrame(itemFrame.getItem()).identifier().isEmpty()) {
            return false;
         } else {
            poseStack.pushPose();
            poseStack.scale(2.0F, 2.0F, 2.0F);
            ItemFramePhotographRenderer.render(itemFrame, poseStack, buffer, packedLight, photographItem, itemFrame.getItem());
            poseStack.popPose();
            return true;
         }
      } else {
         return false;
      }
   }

   public static void resourcesReloaded() {
      ExposureClient.exposureStore().clear();
      ExposureClient.renderedExposures().clearCache();
      ExposureClient.imageRenderer().clearCache();
   }
}
