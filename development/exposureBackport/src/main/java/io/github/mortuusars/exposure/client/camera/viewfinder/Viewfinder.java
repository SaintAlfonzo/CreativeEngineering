package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.integration.Mods;
import io.github.mortuusars.exposure.integration.shoulder_surfing.ShoulderSurfingCompat;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.CameraOnStand;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.Optional;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Viewfinder {
   protected final Camera camera;
   protected final ViewfinderZoom zoom;
   protected final ViewfinderOverlay overlay;
   protected final ViewfinderShader shader;
   protected final ViewfinderSelfie selfie;
   @Nullable
   protected CameraType cameraTypeBefore;
   protected KeyBindings keyBindings = KeyBindings.of(
      Key.press(Minecrft.options().keyAttack).executes(() -> !this.canAttack()),
      Key.press(Minecrft.options().keyTogglePerspective).executes(() -> this.selfie().toggle()),
      Key.press(Minecrft.options().keyInventory).or(Key.press(256)).executes(() -> {
         if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen viewfinderControlsScreen) {
            viewfinderControlsScreen.onClose();
            this.controlsScreen = null;
         } else {
            CameraClient.deactivate();
            this.close();
         }
      }),
      Key.press(KeyboardHandler.getCameraControlsKey()).onlyIf(this::isLookingThrough).onlyIf(() -> !this.controlsActive()).executes(() -> {
         this.openControlsScreen();
         return false;
      })
   );
   @Nullable
   protected ViewfinderCameraControlsScreen controlsScreen;

   public Viewfinder(@NotNull Camera camera) {
      this.camera = camera;
      this.zoom = this.createZoom(camera);
      this.overlay = this.createOverlay(camera);
      this.shader = this.createShader(camera);
      this.selfie = this.createSelfie(camera);
   }

   protected ViewfinderZoom createZoom(Camera camera) {
      return new ViewfinderZoom(camera, this);
   }

   protected ViewfinderOverlay createOverlay(Camera camera) {
      return new ViewfinderOverlay(camera, this);
   }

   protected ViewfinderShader createShader(Camera camera) {
      return new ViewfinderShader(camera, this);
   }

   protected ViewfinderSelfie createSelfie(Camera camera) {
      return new ViewfinderSelfie(camera, this);
   }

   protected ViewfinderCameraControlsScreen createControlsScreen(Camera camera) {
      return new ViewfinderCameraControlsScreen(camera, this);
   }

   public Camera camera() {
      return this.camera;
   }

   public ViewfinderZoom zoom() {
      return this.zoom;
   }

   public ViewfinderOverlay overlay() {
      return this.overlay;
   }

   public ViewfinderShader shader() {
      return this.shader;
   }

   public ViewfinderSelfie selfie() {
      return this.selfie;
   }

   public Optional<ViewfinderCameraControlsScreen> controlsScreen() {
      return Optional.ofNullable(this.controlsScreen);
   }

   public void tick() {
      this.shader().update();
      this.selfie().tick();
   }

   public boolean isLookingThrough() {
      CameraType cameraType = Minecrft.options().getCameraType();
      return cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_FRONT;
   }

   public boolean controlsActive() {
      return Minecrft.get().screen instanceof ViewfinderCameraControlsScreen;
   }

   public boolean canAttack() {
      return Minecrft.get().getCurrentServer() != null
         && (Boolean)Config.Server.CAMERA_VIEWFINDER_ATTACK.get()
         && !this.camera.map(CameraItem::isInSelfieMode).orElse(false);
   }

   public void openControlsScreen() {
      Preconditions.checkNotNull(this.camera, "No active camera");
      this.controlsScreen = this.createControlsScreen(this.camera);
      Minecrft.get().setScreen(this.controlsScreen);
   }

   public void setup() {
      if (Mods.SHOULDER_SURFING.isLoaded()) {
         ShoulderSurfingCompat.onViewfinderSetup(this.camera);
      } else {
         this.cameraTypeBefore = Minecrft.options().getCameraType();
         if (this.cameraTypeBefore != CameraType.FIRST_PERSON
            && (this.cameraTypeBefore == CameraType.THIRD_PERSON_BACK || this.camera instanceof CameraOnStand)) {
            Minecrft.options().setCameraType(CameraType.FIRST_PERSON);
         }
      }
   }

   public void close() {
      if (Mods.SHOULDER_SURFING.isLoaded()) {
         ShoulderSurfingCompat.onViewfinderRemove();
      } else if (this.cameraTypeBefore != null) {
         Minecrft.options().setCameraType(this.cameraTypeBefore);
         this.cameraTypeBefore = null;
      }

      if (this.shader != null) {
         this.shader.close();
      }

      if (this.controlsActive()) {
         Minecrft.get().setScreen(null);
      }
   }

   public boolean keyPressed(int key, int scanCode, int action, int modifiers) {
      return action == 1 && this.keyBindings.keyPressed(key, scanCode, modifiers)
         || action == 0 && this.keyBindings.keyReleased(key, scanCode, modifiers)
         || this.zoom().keyPressed(key, scanCode, action, modifiers);
   }

   public boolean mouseClicked(int button, int action) {
      if (!this.isLookingThrough()) {
         return false;
      } else if (this.controlsActive()) {
         return false;
      } else if (!this.canAttack() && Minecrft.options().keyAttack.matchesMouse(button)) {
         return true;
      } else if (KeyboardHandler.getCameraControlsKey().matchesMouse(button)) {
         this.openControlsScreen();
         return false;
      } else if ((Boolean)Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == 2) {
         this.openControlsScreen();
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double amount) {
      return this.isLookingThrough() && !this.controlsActive() ? this.zoom.mouseScrolled(amount) : false;
   }

   public double modifyMouseSensitivity(double original) {
      if (!this.isLookingThrough()) {
         return original;
      } else {
         double scale = original / (double)((Integer)Minecraft.getInstance().options.fov().get()).intValue();
         double scaledSensitivity = this.zoom.getCurrentFov() * scale;
         return Mth.lerp((Double)Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE.get(), original, scaledSensitivity);
      }
   }
}
