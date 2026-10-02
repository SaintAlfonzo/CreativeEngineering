package io.github.mortuusars.exposure.client.camera.viewfinder;

import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.component.FocalRange;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public class ViewfinderZoom {
   public static final float ZOOM_STEP = 8.0F;
   public static final float ZOOM_PRECISE_MODIFIER = 0.25F;
   protected final Camera camera;
   protected final Viewfinder viewfinder;
   protected FocalRange focalRange;
   protected Animation animation;
   protected double targetFov;
   protected double currentFov;

   public ViewfinderZoom(Camera camera, Viewfinder viewfinder) {
      this.camera = camera;
      this.viewfinder = viewfinder;
      this.focalRange = camera.<FocalRange>map((cameraItem, cameraStack) -> cameraItem.getFocalRange(Minecrft.registryAccess(), cameraStack))
         .orElse(FocalRange.getDefault());
      this.animation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
      double defaultFov = (double)((Integer)Minecrft.options().fov().get()).intValue();
      this.currentFov = defaultFov;
      this.targetFov = camera.<Float>map(CameraSettings.ZOOM::getOrDefault).map(this.focalRange::fovFromZoom).orElse(defaultFov);
   }

   public double getCurrentFov() {
      return Mth.lerp(this.animation.getValue(), this.currentFov, this.targetFov);
   }

   public void zoom(ZoomDirection direction, boolean precise) {
      this.currentFov = this.getCurrentFov();
      double step = 8.0 * (1.0 - Mth.clamp(((double)this.focalRange.min() - this.currentFov) / (double)this.focalRange.min(), 0.3F, 1.0));
      double inertia = Math.abs(this.targetFov - this.currentFov) * 0.8F;
      double change = step + inertia;
      if (precise) {
         change *= 0.25;
      }

      double prevFov = this.targetFov;
      double fov = this.focalRange.clampFov(this.targetFov + (direction == ZoomDirection.IN ? -change : change));
      if (!Mth.equal(prevFov, fov)) {
         this.targetFov = fov;
         this.animation.resetProgress();
         CameraSettings.ZOOM.setAndSync(this.camera, (float)this.focalRange.zoomFromFov(fov));
      }
   }

   public boolean keyPressed(int key, int scanCode, int action, int modifiers) {
      if (action == 1 || action == 2) {
         if (key == 334 || key == 61) {
            this.zoom(ZoomDirection.IN, Screen.hasShiftDown());
            return true;
         }

         if (key == 333 || key == 45) {
            this.zoom(ZoomDirection.OUT, Screen.hasShiftDown());
            return true;
         }
      }

      return false;
   }

   public boolean mouseScrolled(double amount) {
      this.zoom(amount > 0.0 ? ZoomDirection.IN : ZoomDirection.OUT, Screen.hasShiftDown());
      return true;
   }
}
