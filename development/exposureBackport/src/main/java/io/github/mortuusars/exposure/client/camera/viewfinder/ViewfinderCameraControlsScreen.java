package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FocalLengthButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FrameCounterButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.ShutterSpeedButton;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraReleaseC2SP;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuide;
import io.github.mortuusars.exposure.world.camera.component.CompositionGuides;
import io.github.mortuusars.exposure.world.camera.component.FlashMode;
import io.github.mortuusars.exposure.world.camera.component.SelfTimer;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.ImageWidget;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;

public class ViewfinderCameraControlsScreen extends BackportScreen {
   public static final WidgetSprites SHUTTER_SPEED_SPRITES = new WidgetSprites(
      Exposure.resource("camera_controls/shutter_speed_dial"),
      Exposure.resource("camera_controls/shutter_speed_dial_disabled"),
      Exposure.resource("camera_controls/shutter_speed_dial_highlighted")
   );
   public static final WidgetSprites FOCAL_LENGTH_SPRITES = new WidgetSprites(
      Exposure.resource("camera_controls/focal_length"),
      Exposure.resource("camera_controls/focal_length_disabled"),
      Exposure.resource("camera_controls/focal_length_highlighted")
   );
   public static final WidgetSprites FRAME_COUNTER_SPRITES = new WidgetSprites(
      Exposure.resource("camera_controls/frame_counter"),
      Exposure.resource("camera_controls/frame_counter_disabled"),
      Exposure.resource("camera_controls/frame_counter_highlighted")
   );
   public static final ResourceLocation SEPARATOR_SPRITE = Exposure.resource("camera_controls/button_separator");
   protected static final int SEPARATOR_WIDTH = 1;
   protected static final int BUTTON_HEIGHT = 18;
   protected static final int SIDE_BUTTONS_WIDTH = 49;
   protected static final int BUTTON_WIDTH = 15;
   protected final Camera camera;
   protected final Viewfinder viewfinder;
   protected final long openedAt;
   protected int leftPos;
   protected int topPos;

   public ViewfinderCameraControlsScreen(Camera camera, Viewfinder viewfinder) {
      super(CommonComponents.EMPTY);
      this.camera = camera;
      this.viewfinder = viewfinder;
      this.openedAt = Minecrft.level().getGameTime();
   }

   public Camera getCamera() {
      return this.camera;
   }

   public Viewfinder getViewfinder() {
      return this.viewfinder;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      this.refreshMovementKeys();
      Minecraft.getInstance().handleKeybinds();
   }

   protected void init() {
      super.init();
      this.refreshMovementKeys();
      this.leftPos = (this.width - 256) / 2;
      this.topPos = Math.round(this.viewfinder.overlay().getOpening().y + this.viewfinder.overlay().getOpening().height - 256.0F);
      boolean hasFlash = this.camera.<Boolean>map((i, s) -> i.getFlash().isAvailable(s)).orElse(false);
      int elementX = this.leftPos + 128 - (82 + (hasFlash ? 16 : 0) + 49) / 2;
      int elementY = this.topPos + 238;
      Button shutterSpeedButton = this.createShutterSpeedButton();
      this.addRenderableWidget(shutterSpeedButton);
      FocalLengthButton focalLengthButton = new FocalLengthButton(elementX, elementY, 49, 18, FOCAL_LENGTH_SPRITES);
      focalLengthButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.focal_length.tooltip")));
      this.addRenderableOnly(focalLengthButton);
      elementX += focalLengthButton.getWidth();
      this.addSeparator(elementX, elementY);
      elementX++;
      Button compositionGuideButton = this.createCompositionGuideButton();
      compositionGuideButton.setX(elementX);
      compositionGuideButton.setY(elementY);
      this.addRenderableWidget(compositionGuideButton);
      elementX += compositionGuideButton.getWidth();
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

      FrameCounterButton frameCounterButton = new FrameCounterButton(elementX, elementY, 49, 18, FRAME_COUNTER_SPRITES);
      this.addRenderableOnly(frameCounterButton);
   }

   @NotNull
   protected Button createShutterSpeedButton() {
      List<ShutterSpeed> shutterSpeeds = this.camera.map((item, stack) -> item.getAvailableShutterSpeeds(), List.of(ShutterSpeed.DEFAULT));
      ShutterSpeed currentShutterSpeed = this.camera.map(CameraSettings.SHUTTER_SPEED::getOrDefault, ShutterSpeed.DEFAULT);
      return new ShutterSpeedButton(this.leftPos + 94, this.topPos + 226, 69, 12, shutterSpeeds, currentShutterSpeed, speed -> SHUTTER_SPEED_SPRITES)
         .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.shutter_speed.tooltip")))
         .onCycle(speed -> CameraSettings.SHUTTER_SPEED.setAndSync(this.camera, speed));
   }

   @NotNull
   protected Button createCompositionGuideButton() {
      List<CompositionGuide> guides = CompositionGuides.getGuides();
      CompositionGuide currentGuide = this.camera.map(CameraSettings.COMPOSITION_GUIDE::getOrDefault, CompositionGuides.NONE);
      Function<CompositionGuide, WidgetSprites> spritesFunc = guide -> Widgets.threeStateSprites(guide.buttonSpriteLocation());
      return new CycleButton<>(0, 0, 15, 18, guides, currentGuide, spritesFunc)
         .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")))
         .setTooltips(
            guide -> Component.translatable("gui.exposure.camera_controls.composition_guide.tooltip")
                  .append(CommonComponents.NEW_LINE)
                  .append(guide.translate().withStyle(ChatFormatting.GRAY))
         )
         .onCycle(guide -> CameraSettings.COMPOSITION_GUIDE.setAndSync(this.camera, guide));
   }

   @NotNull
   protected Button createSelfTimerButton() {
      List<SelfTimer> values = Arrays.asList(SelfTimer.values());
      SelfTimer currentValue = this.camera.map(CameraSettings.SELF_TIMER::getOrDefault, SelfTimer.OFF);
      Function<SelfTimer, WidgetSprites> spritesFunc = mode -> Widgets.threeStateSprites(
            Exposure.resource("camera_controls/self_timer/timer_" + mode.getSerializedName())
         );
      return new CycleButton<>(0, 0, 15, 18, values, currentValue, spritesFunc)
         .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.self_timer.tooltip")))
         .setTooltips(
            mode -> Component.translatable("gui.exposure.camera_controls.self_timer.tooltip")
                  .append(CommonComponents.NEW_LINE)
                  .append(mode.translate().withStyle(ChatFormatting.GRAY))
         )
         .onCycle(value -> CameraSettings.SELF_TIMER.setAndSync(this.camera, value));
   }

   @NotNull
   protected Button createFlashModeButton() {
      List<FlashMode> modes = Arrays.asList(FlashMode.values());
      FlashMode currentMode = this.camera.map(CameraSettings.FLASH_MODE::getOrDefault, FlashMode.OFF);
      Function<FlashMode, WidgetSprites> spritesFunc = mode -> Widgets.threeStateSprites(
            Exposure.resource("camera_controls/flash_mode/flash_" + mode.getSerializedName())
         );
      return new CycleButton<>(0, 0, 15, 18, modes, currentMode, spritesFunc)
         .setDefaultTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")))
         .setTooltips(
            mode -> Component.translatable("gui.exposure.camera_controls.flash_mode.tooltip")
                  .append(CommonComponents.NEW_LINE)
                  .append(mode.translate().withStyle(ChatFormatting.GRAY))
         )
         .onCycle(mode -> CameraSettings.FLASH_MODE.setAndSync(this.camera, mode));
   }

   protected void addSeparator(int x, int y) {
      ImageWidget sprite = ImageWidget.sprite(1, 18, SEPARATOR_SPRITE);
      sprite.setX(x);
      sprite.setY(y);
      this.addRenderableOnly(sprite);
   }

   protected void refreshMovementKeys() {
      Consumer<KeyMapping> update = keyMapping -> {
         if (keyMapping.key.getType() == Type.MOUSE) {
            keyMapping.setDown(MouseHandler.isMouseButtonHeld(keyMapping.key.getValue()));
         } else {
            long windowId = Minecraft.getInstance().getWindow().getWindow();
            keyMapping.setDown(InputConstants.isKeyDown(windowId, keyMapping.key.getValue()));
         }
      };
      update.accept(KeyboardHandler.getCameraControlsKey());
      Options opt = Minecrft.options();
      update.accept(opt.keyUp);
      update.accept(opt.keyDown);
      update.accept(opt.keyLeft);
      update.accept(opt.keyRight);
      update.accept(opt.keyJump);
      update.accept(opt.keySprint);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (!this.viewfinder.isLookingThrough()) {
         this.onClose();
      } else if (!Minecrft.options().hideGui) {
         guiGraphics.pose().pushPose();
         float viewfinderScale = this.viewfinder.overlay().getScale();
         if (viewfinderScale != 1.0F) {
            guiGraphics.pose().translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
            guiGraphics.pose().scale(viewfinderScale, viewfinderScale, viewfinderScale);
            guiGraphics.pose().translate((float)(-this.width) / 2.0F, (float)(-this.height) / 2.0F, 0.0F);
         }

         super.render(guiGraphics, mouseX, mouseY, partialTick);
         guiGraphics.pose().popPose();
      }
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (button == 1) {
         if (this.camera.isActive()) {
            this.camera.release();
            Packets.sendToServer(ActiveCameraReleaseC2SP.INSTANCE);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (KeyboardHandler.getCameraControlsKey().matchesMouse(button) || (Boolean)Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == 2) {
         if (this.isToggleTimeReached()) {
            this.onClose();
         }

         return false;
      } else {
         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
         return true;
      } else if (button != 0) {
         return false;
      } else if (this.camera.inSelfieMode() && Minecrft.options().keySprint.isDown) {
         this.viewfinder.selfie.rotateCamera(dragY, dragX, true);
         return true;
      } else {
         double fov = this.viewfinder.zoom().getCurrentFov();
         int windowWidth = Minecrft.get().getWindow().getHeight();
         int windowHeight = Minecrft.get().getWindow().getHeight();
         double xRot = dragY / (double)windowHeight * fov;
         double yRot = dragX / (double)windowWidth * fov * ((double)windowHeight / (double)windowWidth);
         Minecrft.player().turn(-yRot * 20.0, -xRot * 20.0);
         return true;
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      if (KeyboardHandler.getCameraControlsKey().matches(keyCode, scanCode)) {
         if (this.isToggleTimeReached()) {
            this.onClose();
         }

         return false;
      } else {
         return super.keyReleased(keyCode, scanCode, modifiers);
      }
   }

   protected boolean isToggleTimeReached() {
      return Minecrft.level().getGameTime() - this.openedAt >= 5L;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode == 334 || keyCode == 61) {
         this.viewfinder.zoom().zoom(ZoomDirection.IN, true);
         return true;
      } else if (keyCode != 333 && keyCode != 45) {
         return false;
      } else {
         this.viewfinder.zoom().zoom(ZoomDirection.OUT, true);
         return true;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (!super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
         this.viewfinder.zoom().zoom(scrollY > 0.0 ? ZoomDirection.IN : ZoomDirection.OUT, true);
         return true;
      } else {
         return false;
      }
   }
}
