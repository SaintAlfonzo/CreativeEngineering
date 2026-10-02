package io.github.mortuusars.exposure_polaroid.client.gui.screen.camera.button;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import java.util.List;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ExposureSliderButton extends Button {
   public static final ResourceLocation BASE = ExposurePolaroid.resource("camera_controls/exposure_slider_base");
   public static final WidgetSprites SLIDER = new WidgetSprites(
      ExposurePolaroid.resource("camera_controls/exposure_slider"),
      ExposurePolaroid.resource("camera_controls/exposure_slider_disabled"),
      ExposurePolaroid.resource("camera_controls/exposure_slider_highlighted")
   );
   protected static final int SLIDER_WIDTH = 122;
   protected static final int SLIDER_HEIGHT = 15;
   protected static final int SLIDER_MOVEMENT_RANGE = 58;
   protected final Camera camera;

   public ExposureSliderButton(int x, int y, Camera camera) {
      super(x, y, 79, 15, Component.empty(), b -> {
      }, n -> Component.empty());
      this.camera = camera;
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      guiGraphics.blitSprite(BASE, this.getX(), this.getY() + 1, this.width, this.height - 1);
      List<ShutterSpeed> shutterSpeeds = this.camera.map((i, s) -> i.getAvailableShutterSpeeds()).orElse(List.of(ShutterSpeed.DEFAULT));
      if (shutterSpeeds.isEmpty()) {
         shutterSpeeds = List.of(ShutterSpeed.DEFAULT);
      }

      ShutterSpeed shutterSpeed = (ShutterSpeed)CameraSettings.SHUTTER_SPEED.getOrDefault(this.camera);
      int indexOf = shutterSpeeds.indexOf(shutterSpeed);
      int index = indexOf == -1 ? shutterSpeeds.size() / 2 : indexOf;
      float position = 1.0F - (float)index / (float)(shutterSpeeds.size() - 1);
      int offset = Math.round(58.0F * position);
      ResourceLocation sliderSprite = SLIDER.get(this.isActive(), this.isHoveredOrFocused());
      guiGraphics.blitSprite(sliderSprite, 122, 15, offset, 0, this.getX() + 8, this.getY(), 64, 15);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (scrollY != 0.0) {
         this.moveSlider(Mth.sign(scrollY));
         return true;
      } else {
         return false;
      }
   }

   protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
      this.setSliderToMousePosition(mouseX);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.clicked(mouseX, mouseY)) {
         if (this.isValidClickButton(button)) {
            this.setSliderToMousePosition(mouseX);
            return true;
         }

         if (button == 1) {
            List<ShutterSpeed> shutterSpeeds = this.camera.map((i, s) -> i.getAvailableShutterSpeeds()).orElse(List.of(ShutterSpeed.DEFAULT));
            if (shutterSpeeds.isEmpty()) {
               return true;
            }

            int defaultIndex = shutterSpeeds.indexOf(ShutterSpeed.DEFAULT);
            int index = defaultIndex != -1 ? defaultIndex : shutterSpeeds.size() / 2;
            ShutterSpeed shutterSpeed = (ShutterSpeed)CameraSettings.SHUTTER_SPEED.getOrDefault(this.camera);
            ShutterSpeed newShutterSpeed = shutterSpeeds.get(index);
            if (!shutterSpeed.equals(newShutterSpeed)) {
               CameraSettings.SHUTTER_SPEED.setAndSync(this.camera, newShutterSpeed);
            }

            return true;
         }
      }

      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 263) {
         this.moveSlider(-1);
         return true;
      } else if (keyCode == 262) {
         this.moveSlider(1);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   protected void setSliderToMousePosition(double mouseX) {
      List<ShutterSpeed> shutterSpeeds = this.camera.map((i, s) -> i.getAvailableShutterSpeeds()).orElse(List.of(ShutterSpeed.DEFAULT));
      if (!shutterSpeeds.isEmpty()) {
         int start = this.getX() + 8;
         int end = this.getX() + 8 + 64;
         int range = end - start;
         int distancePerNotch = range / shutterSpeeds.size();
         int index = (int)Mth.clamp((mouseX - (double)start) / (double)distancePerNotch, 0.0, (double)(shutterSpeeds.size() - 1));
         ShutterSpeed shutterSpeed = (ShutterSpeed)CameraSettings.SHUTTER_SPEED.getOrDefault(this.camera);
         ShutterSpeed newShutterSpeed = shutterSpeeds.get(index);
         if (!shutterSpeed.equals(newShutterSpeed)) {
            CameraSettings.SHUTTER_SPEED.setAndSync(this.camera, newShutterSpeed);
         }
      }
   }

   protected boolean moveSlider(int direction) {
      direction = Mth.sign((double)direction);
      List<ShutterSpeed> shutterSpeeds = this.camera.map((i, s) -> i.getAvailableShutterSpeeds()).orElse(List.of(ShutterSpeed.DEFAULT));
      ShutterSpeed currentShutterSpeed = (ShutterSpeed)CameraSettings.SHUTTER_SPEED.getOrDefault(this.camera);
      int indexOf = shutterSpeeds.indexOf(currentShutterSpeed);
      int index = indexOf == -1 ? shutterSpeeds.size() / 2 : indexOf;
      index = Mth.clamp(index + direction, 0, shutterSpeeds.size() - 1);
      ShutterSpeed newShutterSpeed = shutterSpeeds.get(index);
      if (!currentShutterSpeed.equals(newShutterSpeed)) {
         CameraSettings.SHUTTER_SPEED.setAndSync(this.camera, newShutterSpeed);
         return true;
      } else {
         return false;
      }
   }
}
