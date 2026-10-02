package io.github.mortuusars.exposure.world.sound;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public record SoundEffect(Supplier<SoundEvent> sound, float volume, float pitch, float pitchVariability) {
   public SoundEffect(Supplier<SoundEvent> sound, float volume, float pitch) {
      this(sound, volume, pitch, 0.0F);
   }

   public SoundEffect(Supplier<SoundEvent> sound, float volume) {
      this(sound, volume, 1.0F, 0.0F);
   }

   public SoundEffect(Supplier<SoundEvent> sound) {
      this(sound, 1.0F, 1.0F, 0.0F);
   }

   public SoundEvent get() {
      return this.sound().get();
   }

   public float getFinalPitch() {
      return this.pitch - this.pitchVariability / 2.0F + ThreadLocalRandom.current().nextFloat() * this.pitchVariability;
   }
}
