package io.github.mortuusars.exposure.integration.shoulder_surfing;

import io.github.mortuusars.exposure.world.camera.Camera;

/**
 * Stub: the port leaves Shoulder Surfing out (not in the pack). Callers only reach this when the mod is loaded
 * ({@code Mods.SHOULDER_SURFING}), so with the mod absent these never run. Port upstream's class to support it.
 */
public class ShoulderSurfingCompat {
   public static void onViewfinderSetup(Camera camera) {
   }

   public static void onViewfinderRemove() {
   }
}
