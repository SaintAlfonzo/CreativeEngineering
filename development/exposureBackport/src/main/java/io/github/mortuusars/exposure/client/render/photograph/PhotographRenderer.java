package io.github.mortuusars.exposure.client.render.photograph;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.render.texture.TextureRenderer;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.StackedPhotographsItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public class PhotographRenderer {
   public boolean render(ItemStack itemStack, boolean renderPaper, boolean renderBackside, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      return this.render(itemStack, renderPaper, renderBackside, poseStack, bufferSource, packedLight, 255, 255, 255, 255);
   }

   public boolean render(
      ItemStack itemStack,
      boolean renderPaper,
      boolean renderBackside,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      if (itemStack.getItem() instanceof PhotographItem photographItem) {
         return this.renderPhotograph(poseStack, bufferSource, photographItem, itemStack, renderPaper, renderBackside, packedLight, r, g, b, a);
      } else {
         return itemStack.getItem() instanceof StackedPhotographsItem stackedPhotographsItem
            ? this.renderStackedPhotographs(stackedPhotographsItem, itemStack, poseStack, bufferSource, packedLight, r, g, b, a)
            : false;
      }
   }

   public boolean renderPhotograph(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      PhotographItem photographItem,
      ItemStack photographStack,
      boolean renderPaper,
      boolean renderBackside,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      PhotographStyle style = PhotographStyle.of(photographStack);
      Frame frame = photographItem.getFrame(photographStack);
      RenderableImage image = style.process(ExposureClient.renderedExposures().getOrCreate(frame));
      int paperRotation = frame.identifier().hashCode() % 4 * 90;
      if (renderPaper && style.paperTexture() != ExposureClient.Textures.EMPTY) {
         poseStack.pushPose();
         poseStack.translate(0.5F, 0.5F, 0.0F);
         poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)paperRotation));
         poseStack.translate(-0.5F, -0.5F, 0.0F);
         TextureRenderer.render(poseStack, bufferSource, style.paperTexture(), packedLight, r, g, b, a);
         poseStack.popPose();
         if (renderBackside) {
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            poseStack.translate(-0.5, 0.0, -0.5);
            poseStack.translate(0.5F, 0.5F, 0.0F);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)paperRotation));
            poseStack.translate(-0.5F, -0.5F, 0.0F);
            TextureRenderer.render(
               poseStack, bufferSource, style.paperTexture(), packedLight, (int)((float)r * 0.85F), (int)((float)g * 0.85F), (int)((float)b * 0.85F), a
            );
            poseStack.popPose();
         }
      }

      if (renderPaper) {
         poseStack.pushPose();
         float offset = 0.0625F;
         poseStack.translate((double)offset, (double)offset, 0.001);
         poseStack.scale(0.875F, 0.875F, 0.875F);
         ExposureClient.imageRenderer().render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
         poseStack.popPose();
      } else {
         ExposureClient.imageRenderer().render(image, poseStack, bufferSource, RenderCoordinates.DEFAULT, packedLight, r, g, b, a);
      }

      if (renderPaper && style.hasOverlayTexture()) {
         poseStack.pushPose();
         poseStack.translate(0.5F, 0.5F, 0.0F);
         poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)paperRotation));
         poseStack.translate(-0.5F, -0.5F, 0.0F);
         poseStack.translate(0.0, 0.0, 0.002);
         TextureRenderer.render(poseStack, bufferSource, style.overlayTexture(), packedLight, r, g, b, a);
         poseStack.popPose();
      }

      return !image.isEmpty();
   }

   public boolean renderStackedPhotographs(
      StackedPhotographsItem stackedPhotographsItem,
      ItemStack stack,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int r,
      int g,
      int b,
      int a
   ) {
      List<ItemAndStack<PhotographItem>> photographs = stackedPhotographsItem.getPhotographs(stack).photographsItemAndStacks();
      return this.renderStackedPhotographs(photographs, poseStack, bufferSource, packedLight, r, g, b, a);
   }

   public boolean renderStackedPhotographs(
      List<ItemAndStack<PhotographItem>> photographs, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int r, int g, int b, int a
   ) {
      if (photographs.isEmpty()) {
         return false;
      } else {
         boolean photographRendered = false;

         for (int i = 2; i >= 0; i--) {
            if (photographs.size() - 1 >= i) {
               ItemAndStack<PhotographItem> photograph = photographs.get(i);
               if (i == 0) {
                  poseStack.pushPose();
                  poseStack.translate(0.0, 0.0, 0.002);
                  photographRendered = this.renderPhotograph(
                     poseStack, bufferSource, photograph.getItem(), photograph.getItemStack(), true, false, packedLight, r, g, b, a
                  );
                  poseStack.popPose();
                  break;
               }

               float posOffset = this.getStackedPhotographOffset() * (float)i;
               poseStack.pushPose();
               poseStack.translate((double)posOffset, (double)posOffset, 0.002 - (double)((float)i / 1000.0F));
               poseStack.translate(0.5F, 0.5F, 0.0F);
               Frame frame = photograph.getItem().getFrame(photograph.getItemStack());
               int rotation = frame.identifier().hashCode() % 4 * 90;
               poseStack.mulPose(Vector3f.ZP.rotationDegrees((float)rotation));
               poseStack.translate(-0.5F, -0.5F, 0.0F);
               float brightness = 1.0F - this.getStackedBrightnessStep() * (float)i;
               PhotographStyle photographStyle = PhotographStyle.of(photograph.getItemStack());
               TextureRenderer.render(
                  poseStack,
                  bufferSource,
                  photographStyle.paperTexture(),
                  packedLight,
                  (int)((float)r * brightness),
                  (int)((float)g * brightness),
                  (int)((float)b * brightness),
                  a
               );
               poseStack.popPose();
            }
         }

         return photographRendered;
      }
   }

   public float getStackedBrightnessStep() {
      return 0.2F;
   }

   public float getStackedPhotographOffset() {
      return 0.03125F;
   }
}
