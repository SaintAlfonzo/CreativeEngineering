package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ForceRegularOrSelfieCameraTypeAction implements CaptureAction {
   protected final CameraHolder holder;
   protected CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;

   public ForceRegularOrSelfieCameraTypeAction(CameraHolder holder) {
      this.holder = holder;
   }

   @Override
   public void beforeCapture() {
      this.cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
      if (this.cameraTypeBeforeCapture == CameraType.THIRD_PERSON_BACK) {
         Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
      } else if (this.cameraTypeBeforeCapture == CameraType.THIRD_PERSON_FRONT && !this.cameraInHandInSelfieMode()) {
         Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
      }
   }

   protected boolean cameraInHandInSelfieMode() {
      CameraInHand camera = CameraInHand.find(this.holder);
      return camera != null && camera.map(CameraItem::isInSelfieMode).orElse(false);
   }

   @Override
   public void afterCapture() {
      Minecraft.getInstance().options.setCameraType(this.cameraTypeBeforeCapture);
   }
}
