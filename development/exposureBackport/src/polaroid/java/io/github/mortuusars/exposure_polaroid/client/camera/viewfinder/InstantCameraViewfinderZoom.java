package io.github.mortuusars.exposure_polaroid.client.camera.viewfinder;

import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderZoom;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.util.Mth;

public class InstantCameraViewfinderZoom extends ViewfinderZoom {
   public InstantCameraViewfinderZoom(Camera camera, Viewfinder viewfinder) {
      super(camera, viewfinder);
      this.animation = new Animation(100, EasingFunction.EASE_OUT_EXPO);
   }

   public void zoom(ZoomDirection direction, boolean precise) {
      this.currentFov = this.getCurrentFov();
      float zoom = (Float)CameraSettings.ZOOM.getOrDefault(this.camera);
      float targetZoom = direction == ZoomDirection.IN ? 1.0F : 0.0F;
      if (!Mth.equal(zoom, targetZoom)) {
         this.targetFov = this.focalRange.fovFromZoom((double)targetZoom);
         this.animation.resetProgress();
         CameraSettings.ZOOM.setAndSync(this.camera, targetZoom);
      }
   }
}
