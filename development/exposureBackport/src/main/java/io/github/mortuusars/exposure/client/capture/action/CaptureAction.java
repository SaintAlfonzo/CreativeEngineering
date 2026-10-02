package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.integration.Mods;
import io.github.mortuusars.exposure.integration.shoulder_surfing.ShoulderSurfingForceRegularOrSelfieCameraTypeAction;
import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface CaptureAction {
   CaptureAction EMPTY = new CaptureAction() {
   };

   default int requiredDelayTicks() {
      return 0;
   }

   default void initialize() {
   }

   default void delayTick(int delayTicksLeft) {
   }

   default void beforeCapture() {
   }

   default void onSuccess() {
   }

   default void onFailure(TranslatableError error) {
   }

   default void afterCapture() {
   }

   static CaptureAction forceCamera(CameraType cameraType) {
      return new ForceCameraTypeAction(cameraType);
   }

   static CaptureAction forceRegularOrSelfieCamera(@Nullable CameraHolder holder) {
      return (CaptureAction)(Mods.SHOULDER_SURFING.isLoaded()
         ? new ShoulderSurfingForceRegularOrSelfieCameraTypeAction(holder)
         : new ForceRegularOrSelfieCameraTypeAction(holder));
   }

   static CaptureAction hideGui() {
      return new HideGuiAction();
   }

   static CaptureAction disablePostEffect() {
      return new DisablePostEffectAction();
   }

   static CaptureAction setPostEffect(ResourceLocation postEffect) {
      return new SetPostEffectAction(postEffect);
   }

   static CaptureAction modifyGamma(ShutterSpeed shutterSpeed) {
      return (CaptureAction)(shutterSpeed != ShutterSpeed.DEFAULT ? new ModifyGammaAction(shutterSpeed) : EMPTY);
   }

   static CaptureAction flash(Entity photographer) {
      return new FlashAction(photographer);
   }

   static CaptureAction interplanarProjection(CameraId cameraId) {
      return new InterplanarProjectionAction(cameraId);
   }

   static CaptureAction setCameraEntity(Entity viewEntity) {
      return new SetCameraEntityAction(viewEntity);
   }

   static CaptureAction setFov(double fov) {
      return new SetFovAction(fov);
   }

   static CaptureAction setFilter(Optional<ResourceLocation> filter) {
      return new SetFilterAction(filter);
   }

   default CaptureAction orElse(Supplier<CaptureAction> action) {
      return this.equals(EMPTY) ? action.get() : this;
   }

   static CaptureAction optional(boolean predicate, Supplier<CaptureAction> componentSupplier) {
      return predicate ? componentSupplier.get() : EMPTY;
   }

   static CaptureAction optional(boolean predicate, CaptureAction component) {
      return predicate ? component : EMPTY;
   }

   static CaptureAction optional(Optional<CaptureAction> optional) {
      return optional.orElse(EMPTY);
   }

   static <T> CaptureAction optional(Optional<T> optional, Function<T, CaptureAction> ifPresent) {
      return optional.map(ifPresent).orElse(EMPTY);
   }
}
