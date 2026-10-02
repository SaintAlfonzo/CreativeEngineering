package io.github.mortuusars.exposure_polaroid.world.item.camera;

import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.Shutter;
import io.github.mortuusars.exposure.world.sound.Sound;
import io.github.mortuusars.exposure_polaroid.ExposurePolaroid;
import net.minecraft.world.entity.Entity;

public class InstantCameraShutter extends Shutter {
   public void playOpenSound(CameraHolder holder) {
      Entity entity = holder.asHolderEntity();
      Sound.play(entity, ExposurePolaroid.SoundEvents.INSTANT_CAMERA_RELEASE.get(), entity.getSoundSource(), 1.2F, 1.0F, 0.2F);
   }

   public void playCloseSound(CameraHolder holder) {
   }
}
