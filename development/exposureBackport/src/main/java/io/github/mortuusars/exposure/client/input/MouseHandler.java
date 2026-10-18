package io.github.mortuusars.exposure.client.input;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.util.Minecrft;

public class MouseHandler {
   private static final boolean[] heldMouseButtons = new boolean[12];

   public static boolean isMouseButtonHeld(int button) {
      return button >= 0 && button < heldMouseButtons.length && heldMouseButtons[button];
   }

   public static boolean buttonPressed(int button, int action, int modifiers) {
      if (button >= 0 && button < heldMouseButtons.length) {
         heldMouseButtons[button] = action == 1;
      }

      // Port: Forge's MouseButton.Pre fires before MouseHandler hands the click to an open screen (upstream's hook only
      // sees in-world clicks), so leave every click inside a screen alone.
      return Minecrft.get().screen == null && CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseClicked(button, action);
   }

   public static boolean scrolled(double amount) {
      return CameraClient.viewfinder() != null && CameraClient.viewfinder().mouseScrolled(amount);
   }

   public static double modifySensitivity(double original) {
      return CameraClient.viewfinder() != null ? CameraClient.viewfinder().modifyMouseSensitivity(original) : original;
   }

   public static boolean onTurnPlayer(double xRot, double yRot) {
      return CameraClient.viewfinder() != null && CameraClient.viewfinder().isLookingThrough() && CameraClient.viewfinder().selfie().mouseMove(xRot, yRot);
   }
}
