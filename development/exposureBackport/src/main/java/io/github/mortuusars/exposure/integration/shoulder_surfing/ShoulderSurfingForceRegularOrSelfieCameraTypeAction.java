package io.github.mortuusars.exposure.integration.shoulder_surfing;

import io.github.mortuusars.exposure.client.capture.action.ForceRegularOrSelfieCameraTypeAction;
import io.github.mortuusars.exposure.world.entity.CameraHolder;

/** Stub (see {@link ShoulderSurfingCompat}): behaves like the regular action. */
public class ShoulderSurfingForceRegularOrSelfieCameraTypeAction extends ForceRegularOrSelfieCameraTypeAction {
   public ShoulderSurfingForceRegularOrSelfieCameraTypeAction(CameraHolder holder) {
      super(holder);
   }
}
