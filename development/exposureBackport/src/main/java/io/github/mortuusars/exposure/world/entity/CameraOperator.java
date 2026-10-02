package io.github.mortuusars.exposure.world.entity;

import io.github.mortuusars.exposure.world.camera.Camera;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.world.entity.CameraOperator;

public interface CameraOperator {
   @Nullable
   default Camera getActiveExposureCamera() {
      throw new IllegalStateException("This method must be implemented.");
   }

   default void setActiveExposureCamera(Camera camera) {
      throw new IllegalStateException("This method must be implemented.");
   }

   default void removeActiveExposureCamera() {
      throw new IllegalStateException("This method must be implemented.");
   }

   default Optional<Camera> getActiveExposureCameraOptional() {
      return Optional.ofNullable(((CameraOperator)this).getActiveExposureCamera());
   }

   default float getExposureCameraActionAnim(float partialTick) {
      return 0.0F;
   }

   default LivingEntity asOperatorEntity() {
      return (LivingEntity)this;
   }
}
