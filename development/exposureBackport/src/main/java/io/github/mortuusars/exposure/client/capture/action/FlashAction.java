package io.github.mortuusars.exposure.client.capture.action;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FlashAction implements CaptureAction {
   private final Entity cameraHolder;
   private long initializedAt;

   public FlashAction(Entity cameraHolder) {
      this.cameraHolder = cameraHolder;
   }

   @Override
   public int requiredDelayTicks() {
      return (Integer)Config.Client.FLASH_CAPTURE_DELAY_TICKS.get();
   }

   @Override
   public void initialize() {
      this.initializedAt = this.cameraHolder.level.getGameTime();
   }

   @Override
   public void beforeCapture() {
      long ticksDelay = this.cameraHolder.level.getGameTime() - this.initializedAt;
      if (ticksDelay > 8L) {
         Exposure.LOGGER
            .warn("Capturing with delay of '{}' ticks can be too long for a flash to have an effect. The flash might disappear in that time.", ticksDelay);
      }
   }

   @Override
   public void afterCapture() {
      Level level = this.cameraHolder.level;
      Vec3 pos = this.cameraHolder.position();
      Vec3 lookAngle = this.cameraHolder.getLookAngle();
      pos = pos.add(0.0, 1.1, 0.0).add(lookAngle.scale(0.8));
      RandomSource r = level.getRandom();

      for (int i = 0; i < 3; i++) {
         level.addParticle(
            ParticleTypes.END_ROD,
            pos.x + (double)r.nextFloat() - 0.5,
            pos.y + (double)r.nextFloat() + 0.15F,
            pos.z + (double)r.nextFloat() - 0.5,
            lookAngle.x * 0.025F + (double)(r.nextFloat() * 0.025F),
            lookAngle.y * 0.025F + (double)(r.nextFloat() * 0.025F),
            lookAngle.z * 0.025F + (double)(r.nextFloat() * 0.025F)
         );
      }
   }
}
