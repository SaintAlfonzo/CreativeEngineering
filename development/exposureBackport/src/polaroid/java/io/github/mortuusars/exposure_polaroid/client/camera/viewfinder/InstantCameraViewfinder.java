package io.github.mortuusars.exposure_polaroid.client.camera.viewfinder;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderOverlay;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderZoom;
import io.github.mortuusars.exposure.world.camera.Camera;

public class InstantCameraViewfinder extends Viewfinder {
   public InstantCameraViewfinder(Camera camera) {
      super(camera);
   }

   protected ViewfinderZoom createZoom(Camera camera) {
      return new InstantCameraViewfinderZoom(camera, this);
   }

   protected ViewfinderOverlay createOverlay(Camera camera) {
      return new InstantCameraViewfinderOverlay(camera, this);
   }

   protected ViewfinderCameraControlsScreen createControlsScreen(Camera camera) {
      return new InstantCameraControlsScreen(camera, this);
   }
}
