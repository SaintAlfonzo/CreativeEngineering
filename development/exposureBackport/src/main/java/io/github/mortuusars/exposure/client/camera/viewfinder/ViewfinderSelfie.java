package io.github.mortuusars.exposure.client.camera.viewfinder;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public class ViewfinderSelfie {
   protected final Camera camera;
   protected final Viewfinder viewfinder;
   protected double rawXRot;
   protected double rawYRot;
   protected double xRot;
   protected double yRot;
   protected double prevXRot;
   protected double prevYRot;
   protected Animation xRotAnimation = new Animation(200, EasingFunction.EASE_OUT_EXPO);
   protected Animation yRotAnimation = new Animation(200, EasingFunction.EASE_OUT_EXPO);

   public ViewfinderSelfie(Camera camera, Viewfinder viewfinder) {
      this.camera = camera;
      this.viewfinder = viewfinder;
   }

   public void tick() {
      this.updateSelfieMode();
   }

   public float getMaxCameraDistance() {
      return ((Double)Config.Server.SELFIE_CAMERA_DISTANCE.get()).floatValue();
   }

   public double getCameraXRot() {
      return Mth.lerp(this.xRotAnimation.getValue(), this.prevXRot, this.xRot);
   }

   public double getCameraYRot() {
      return Mth.lerp(this.yRotAnimation.getValue(), this.prevYRot, this.yRot);
   }

   public double getMaxRotationXDegrees() {
      return 40.0;
   }

   public double getMaxRotationYDegrees() {
      return 30.0;
   }

   public double getRotationXStepDegrees() {
      return 5.0;
   }

   public double getRotationYStepDegrees() {
      return 5.0;
   }

   public void toggle() {
      if (this.camera instanceof CameraOnStand) {
         Minecrft.options().setCameraType(CameraType.FIRST_PERSON);
      } else {
         CameraType newCameraType = Minecrft.options().getCameraType() == CameraType.FIRST_PERSON ? CameraType.THIRD_PERSON_FRONT : CameraType.FIRST_PERSON;
         Minecrft.options().setCameraType(newCameraType);
      }
   }

   public void updateSelfieMode() {
      boolean inSelfieMode = Minecrft.options().getCameraType() == CameraType.THIRD_PERSON_FRONT;
      if (this.camera.inSelfieMode() != inSelfieMode) {
         CameraSettings.SELFIE_MODE.setAndSync(this.camera, inSelfieMode);
         CameraSettings.SELFIE_ROTATION_X.setAndSync(this.camera, 0.0);
         CameraSettings.SELFIE_ROTATION_Y.setAndSync(this.camera, 0.0);
         this.xRot = 0.0;
         this.yRot = 0.0;
         this.rawXRot = 0.0;
         this.rawYRot = 0.0;
      }
   }

   public boolean mouseMove(double xRotDelta, double yRotDelta) {
      if (!this.viewfinder.controlsActive() && this.camera.inSelfieMode() && Minecrft.options().keySprint.isDown) {
         this.rotateCamera(xRotDelta, yRotDelta, false);
         return true;
      } else {
         return false;
      }
   }

   public void rotateCamera(double xRotDelta, double yRotDelta, boolean precise) {
      double maxX = this.getMaxRotationXDegrees();
      double maxY = this.getMaxRotationYDegrees();
      this.rawXRot = Mth.clamp(this.rawXRot + xRotDelta * 0.15, -maxX, maxX);
      this.rawYRot = Mth.clamp(this.rawYRot + yRotDelta * 0.15, -maxY, maxY);
      double stepX = this.getRotationXStepDegrees() * (precise ? 0.25 : 1.0);
      double stepY = this.getRotationYStepDegrees() * (precise ? 0.25 : 1.0);
      double xr = (double)((int)this.rawXRot) - (double)((int)this.rawXRot) % stepX;
      double yr = (double)((int)this.rawYRot) - (double)((int)this.rawYRot) % stepY;
      if (xr != this.prevXRot) {
         this.prevXRot = this.getCameraXRot();
         this.xRot = xr;
         this.xRotAnimation.resetProgress();
         CameraSettings.SELFIE_ROTATION_X.setAndSync(this.camera, this.xRot);
      }

      if (yr != this.prevYRot) {
         this.prevYRot = this.getCameraYRot();
         this.yRot = yr;
         this.yRotAnimation.resetProgress();
         CameraSettings.SELFIE_ROTATION_Y.setAndSync(this.camera, this.yRot);
      }
   }
}
