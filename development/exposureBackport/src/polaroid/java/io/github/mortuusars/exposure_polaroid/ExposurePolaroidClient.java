package io.github.mortuusars.exposure_polaroid;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderRegistry;
import io.github.mortuusars.exposure.client.capture.template.CameraCaptureTemplate;
import io.github.mortuusars.exposure.client.capture.template.CaptureTemplates;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.SensitiveFilmItem;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import io.github.mortuusars.exposure_polaroid.client.animation.InstantCameraPoses;
import io.github.mortuusars.exposure_polaroid.client.camera.viewfinder.InstantCameraViewfinder;
import io.github.mortuusars.exposure_polaroid.world.item.InstantCameraItem;
import io.github.mortuusars.exposure_polaroid.world.item.camera.InstantCameraAttachment;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public class ExposurePolaroidClient {
   public static void init() {
      CameraModelPoses.register(ExposurePolaroid.Items.INSTANT_CAMERA.get(), new InstantCameraPoses());
      ViewfinderRegistry.register(ExposurePolaroid.Items.INSTANT_CAMERA.get(), InstantCameraViewfinder::new);
      CaptureTemplates.register(InstantCameraItem.CAPTURE_TYPE, new CameraCaptureTemplate());
      ItemProperties.register((Item)ExposurePolaroid.Items.INSTANT_CAMERA.get(), Exposure.resource("camera_state"), (stack, level, entity, seed) -> {
         if (!(stack.getItem() instanceof CameraItem cameraItem) || !cameraItem.isActive(stack)) {
            return 0.0F;
         }

         if (cameraItem.isInSelfieMode(stack)) {
            return entity == Minecrft.player() ? 0.2F : 0.3F;
         } else {
            return 0.1F;
         }
      });
      ItemProperties.register((Item)ExposurePolaroid.Items.INSTANT_CAMERA.get(), ExposurePolaroid.resource("printing"), (stack, level, entity, seed) -> {
         if (stack.getItem() instanceof CameraItem cameraItem) {
            Entity holderEntity = (Entity)(entity != null ? entity : stack.getEntityRepresentation());
            if (holderEntity instanceof CameraHolder holder) {
               return cameraItem.isOnCooldown(holder, stack) ? 1.0F - cameraItem.getCooldownPercent(holder, stack) : 0.0F;
            }
         }

         return 0.0F;
      });
      ItemProperties.register((Item)ExposurePolaroid.Items.INSTANT_CAMERA.get(), ExposurePolaroid.resource("slide_type"), (stack, level, entity, seed) -> {
         if (InstantCameraAttachment.INSTANT_SLIDE.get(stack).getItem() instanceof SensitiveFilmItem film) {
            return film.getType() == ExposureType.COLOR ? 1.0F : 0.5F;
         } else {
            return 0.0F;
         }
      });
   }

   public static class Models {
      // 1.19.2: extra models are plain ResourceLocations ("standalone" variants are 1.21)
      public static final ResourceLocation INSTANT_CAMERA_GUI = ExposurePolaroid.resource("instant_camera_gui");
   }
}
