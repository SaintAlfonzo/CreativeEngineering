package io.github.mortuusars.exposure_polaroid.client.camera.viewfinder;

import io.github.mortuusars.exposure.client.camera.viewfinder.Viewfinder;
import io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderCameraControlsScreen;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button.ExposureSliderButton;
import io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button.SlideCounterWidget;
import io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button.ZoomWidget;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.network.chat.Component;

public class InstantCameraControlsScreen extends ViewfinderCameraControlsScreen {
   public static final WidgetSprites ZOOM_SPRITES = new WidgetSprites(
      ExposurePolaroid.resource("camera_controls/zoom"),
      ExposurePolaroid.resource("camera_controls/zoom_disabled"),
      ExposurePolaroid.resource("camera_controls/zoom_highlighted")
   );
   public static final WidgetSprites SLIDE_COUNTER_SPRITES = new WidgetSprites(
      ExposurePolaroid.resource("camera_controls/slide_counter"),
      ExposurePolaroid.resource("camera_controls/slide_counter_disabled"),
      ExposurePolaroid.resource("camera_controls/slide_counter_highlighted")
   );
   protected static final int SIDE_BUTTONS_WIDTH = 49;

   public InstantCameraControlsScreen(Camera camera, Viewfinder viewfinder) {
      super(camera, viewfinder);
   }

   protected void init() {
      this.refreshMovementKeys();
      this.leftPos = (this.width - 256) / 2;
      this.topPos = Math.round(this.viewfinder.overlay().getOpening().y + this.viewfinder.overlay().getOpening().height - 256.0F);
      boolean hasFlash = this.camera.map((i, s) -> i.getFlash().isAvailable(s)).orElse(false);
      int widgetsWidth = 66 + (hasFlash ? 16 : 0) + 49;
      int elementX = this.leftPos + 128 - widgetsWidth / 2;
      int elementY = this.topPos + 238;
      Button exposureSliderButton = this.createExposureSliderButton(elementY);
      this.addRenderableWidget(exposureSliderButton);
      ZoomWidget zoomWidget = new ZoomWidget(elementX, elementY, 49, 18, ZOOM_SPRITES, this.camera);
      zoomWidget.setTooltip(Tooltip.create(Component.translatable("gui.exposure_polaroid.camera_controls.zoom.tooltip")));
      this.addRenderableOnly(zoomWidget);
      elementX += zoomWidget.getWidth();
      this.addSeparator(elementX, elementY);
      elementX++;
      Button selfTimerButton = this.createSelfTimerButton();
      selfTimerButton.setX(elementX);
      selfTimerButton.setY(elementY);
      this.addRenderableWidget(selfTimerButton);
      elementX += selfTimerButton.getWidth();
      this.addSeparator(elementX, elementY);
      elementX++;
      if (hasFlash) {
         Button flashModeButton = this.createFlashModeButton();
         flashModeButton.setX(elementX);
         flashModeButton.setY(elementY);
         this.addRenderableWidget(flashModeButton);
         elementX += flashModeButton.getWidth();
         this.addSeparator(elementX, elementY);
         elementX++;
      }

      SlideCounterWidget slideCounterWidget = new SlideCounterWidget(elementX, elementY, 49, 18, SLIDE_COUNTER_SPRITES, this.camera);
      this.addRenderableOnly(slideCounterWidget);
   }

   protected Button createExposureSliderButton(int elementY) {
      ExposureSliderButton exposureSliderButton = new ExposureSliderButton(this.leftPos + 88, elementY - 15, this.camera);
      exposureSliderButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure_polaroid.camera_controls.exposure_slider.tooltip")));
      return exposureSliderButton;
   }
}
