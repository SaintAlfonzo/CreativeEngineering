package io.github.mortuusars.exposure_polaroid.client.camera.viewfinder;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import io.github.mortuusars.exposure_polaroid.world.item.InstantCameraItem;
import io.github.mortuusars.exposure_polaroid.world.item.camera.InstantCameraAttachment;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InstantCameraViewfinderOverlay extends ViewfinderOverlay {
   public static final ResourceLocation VIEWFINDER_TEXTURE = ExposurePolaroid.resource("textures/gui/viewfinder/viewfinder.png");
   public static final ResourceLocation NO_SLIDES_ICON_TEXTURE = ExposurePolaroid.resource("textures/gui/viewfinder/no_slides.png");
   public static final ResourceLocation REMAINING_SLIDES_ICON_TEXTURE = ExposurePolaroid.resource("textures/gui/viewfinder/remaining_slides.png");

   public InstantCameraViewfinderOverlay(Camera camera, Viewfinder viewfinder) {
      super(camera, viewfinder);
   }

   protected void drawViewfinderTexture(GuiGraphics guiGraphics) {
      GuiUtil.blit(VIEWFINDER_TEXTURE, guiGraphics.pose(), this.opening, 0, 0, (int)this.opening.width, (int)this.opening.height, 0.0F);
   }

   protected void renderStatusIcons(PoseStack poseStack, ItemStack cameraStack) {
      ItemStack slideStack = InstantCameraAttachment.INSTANT_SLIDE.get(cameraStack).getForReading();
      if (slideStack.isEmpty()) {
         this.renderNoSlidesIcon(poseStack);
      } else {
         this.renderRemainingSlidesIcon(poseStack, cameraStack);
      }
   }

   protected void renderNoSlidesIcon(PoseStack poseStack) {
      RenderSystem.setShaderTexture(0, NO_SLIDES_ICON_TEXTURE);
      int x = (int)(this.opening.x + this.opening.width / 2.0F - 12.0F);
      int y = (int)(this.opening.y + this.opening.height - 18.0F);
      GuiUtil.blit(poseStack, (float)x, (float)y, 23.0F, 18.0F, 0, 0, 23, 18, 0.0F);
   }

   protected void renderRemainingSlidesIcon(PoseStack poseStack, ItemStack cameraStack) {
      if (cameraStack.getItem() instanceof InstantCameraItem instantCamera) {
         int var9 = instantCamera.getMaxSlideCount();
         int remainingSlides = instantCamera.getRemainingSlides(cameraStack);
         if (var9 > 5 && remainingSlides <= 3) {
            RenderSystem.setShaderTexture(0, REMAINING_SLIDES_ICON_TEXTURE);
            float x = (float)((int)(this.opening.x + this.opening.width / 2.0F - 17.0F));
            float y = (float)((int)(this.opening.y + this.opening.height - 15.0F));
            int vOffset = (remainingSlides - 1) * 15;
            GuiUtil.blit(poseStack, x, y, 33.0F, 15.0F, 0, vOffset, 33, 45, 0.0F);
         }
      }
   }
}
