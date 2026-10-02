package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.ArrayList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;

public class CameraModel {
   public static BakedModel modifyCameraModel(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, BakedModel original) {
      if (stack.getItem() instanceof CameraItem camera) {
         ArrayList var7 = new ArrayList();
         if (camera.isInSelfieMode(stack)) {
            if (entity != null && entity.equals(Minecrft.get().getCameraEntity())) {
               return PlatformHelperClient.getModel(ExposureClient.Models.SELFIE_STICK);
            }

            original = getModelWithOverrides(ExposureClient.Models.CAMERA_SELFIE, stack, level, entity, seed);
            var7.add(getModelWithOverrides(ExposureClient.Models.CAMERA_SELFIE_STICK, stack, level, entity, seed));
         } else if (camera.isActive(stack)) {
            original = getModelWithOverrides(ExposureClient.Models.CAMERA_ACTIVE, stack, level, entity, seed);
            var7.add(getModelWithOverrides(ExposureClient.Models.CAMERA_VIEWFINDER, stack, level, entity, seed));
         }

         if (Attachment.FLASH.isPresent(stack)) {
            var7.add(getModelWithOverrides(ExposureClient.Models.CAMERA_FLASH, stack, level, entity, seed));
         }

         if (Attachment.LENS.isPresent(stack)) {
            var7.add(getModelWithOverrides(ExposureClient.Models.CAMERA_LENS, stack, level, entity, seed));
         }

         if (!var7.isEmpty()) {
            var7.add(0, original);
            return new CompositeModel(var7);
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   private static BakedModel getModelWithOverrides(
      ResourceLocation location, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed
   ) {
      BakedModel model = PlatformHelperClient.getModel(location);
      return model.getOverrides().resolve(model, stack, level, entity, seed);
   }
}
