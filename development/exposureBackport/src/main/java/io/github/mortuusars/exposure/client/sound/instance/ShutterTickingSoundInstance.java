package io.github.mortuusars.exposure.client.sound.instance;

import io.github.mortuusars.exposure.world.camera.CameraId;
import io.github.mortuusars.exposure.world.camera.CameraInHand;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShutterTickingSoundInstance extends EntityBoundSoundInstance {
   protected final CameraId cameraId;
   protected final float fullVolume;
   protected final int durationTicks;
   protected final long endsAtTick;
   protected Entity entity;

   public ShutterTickingSoundInstance(
      Entity entity, CameraId cameraId, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, int durationTicks
   ) {
      super(soundEvent, soundSource, volume, pitch, entity, entity.level.getRandom().nextLong());
      this.entity = entity;
      this.cameraId = cameraId;
      this.fullVolume = volume;
      this.durationTicks = durationTicks;
      this.endsAtTick = entity.level.getGameTime() + (long)durationTicks;
      this.looping = true;
      this.volume = 0.0F;
   }

   public void updateEntity(Entity entity) {
      this.entity = entity;
   }

   public boolean canStartSilent() {
      return true;
   }

   public boolean canPlaySound() {
      return !this.entity.isSilent();
   }

   public void tick() {
      this.x = this.entity.getX();
      this.y = this.entity.getY();
      this.z = this.entity.getZ();
      if (this.endsAtTick - this.entity.level.getGameTime() < 0L) {
         this.stop();
      } else {
         ItemStack stack = ItemStack.EMPTY;
         boolean isOnHotbar = false;
         if (this.entity instanceof CameraHolder holder) {
            CameraInHand cameraInHand = CameraInHand.find(holder);
            if (cameraInHand != null && cameraInHand.idMatches(this.cameraId)) {
               stack = cameraInHand.getItemStack();
            } else if (this.entity instanceof Player player) {
               ItemStack hotbarStack = this.getCameraOnHotbar(player);
               if (!hotbarStack.isEmpty()) {
                  stack = hotbarStack;
                  isOnHotbar = true;
               }
            }
         } else if (this.entity instanceof ItemEntity itemEntity && itemEntity.getItem().getItem() instanceof CameraItem) {
            stack = itemEntity.getItem();
         }

         if (stack.getItem() instanceof CameraItem item && item.getShutter().isOpen(stack)) {
            this.volume = isOnHotbar ? this.fullVolume * 0.35F : this.fullVolume;
            return;
         }

         this.volume = 0.0F;
      }
   }

   protected ItemStack getCameraOnHotbar(Player player) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = player.getInventory().getItem(i);
         if (this.cameraId.matches(stack)) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }
}
