package io.github.mortuusars.exposure.client.sound;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class UniqueSoundManager {
   private static final Table<String, ResourceLocation, SoundInstance> SOUNDS = HashBasedTable.create();

   public static void play(String id, SoundInstance instance) {
      stop(id, instance.getLocation());
      SOUNDS.put(id, instance.getLocation(), instance);
      Minecrft.get().getSoundManager().play(instance);
   }

   public static void play(String id, Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
      SoundInstance soundInstance = createEntityBoundInstance(entity, sound, source, volume, pitch);
      play(id, soundInstance);
   }

   public static void play(Entity entity, SoundEvent sound, SoundSource source, float volume, float pitch) {
      String id = entity.getScoreboardName();
      play(id, entity, sound, source, volume, pitch);
   }

   public static void stop(String id, ResourceLocation location) {
      SoundInstance instance = (SoundInstance)SOUNDS.remove(id, location);
      if (instance != null) {
         Minecrft.get().getSoundManager().stop(instance);
      }
   }

   public static void stop(String id, SoundEvent sound) {
      stop(id, sound.getLocation());
   }

   public static void stopAllOf(SoundEvent sound) {
      ResourceLocation location = sound.getLocation();
      SOUNDS.cellSet().removeIf(cell -> {
         if (((ResourceLocation)cell.getColumnKey()).equals(location)) {
            Minecrft.get().getSoundManager().stop((SoundInstance)cell.getValue());
            return true;
         } else {
            return false;
         }
      });
   }

   private static SoundInstance createEntityBoundInstance(Entity entity, SoundEvent soundEvent, SoundSource source, float volume, float pitch) {
      return new EntityBoundSoundInstance(soundEvent, source, volume, pitch, entity, entity.level.getRandom().nextLong());
   }
}
