package io.github.mortuusars.exposure.client.capture.action;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ForceCameraTypeAction implements CaptureAction {
   private final CameraType forcedCameraType;
   private CameraType cameraTypeBeforeCapture = CameraType.FIRST_PERSON;

   public ForceCameraTypeAction(CameraType forcedCameraType) {
      this.forcedCameraType = forcedCameraType;
   }

   @Override
   public void beforeCapture() {
      this.cameraTypeBeforeCapture = Minecraft.getInstance().options.getCameraType();
      Minecraft.getInstance().options.setCameraType(this.forcedCameraType);
   }

   @Override
   public void afterCapture() {
      Minecraft.getInstance().options.setCameraType(this.cameraTypeBeforeCapture);
   }
}
