package io.github.mortuusars.exposure.world.item.camera;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.sound.Sound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class Timer {
   public void set(CameraHolder holder, ItemStack stack, int ticks) {
      long gameTime = holder.asHolderEntity().level.getGameTime();
      this.setStartTick(stack, gameTime);
      this.setEndTick(stack, gameTime + (long)ticks);
   }

   public void stop(ItemStack stack) {
      this.setStartTick(stack, -1L);
      this.setEndTick(stack, -1L);
   }

   public boolean isTicking(CameraHolder holder, ItemStack stack) {
      return this.getEndTick(stack) > holder.asHolderEntity().level.getGameTime();
   }

   public int getRemainingTicks(CameraHolder holder, ItemStack stack) {
      return (int)Math.max(-1L, this.getEndTick(stack) - holder.asHolderEntity().level.getGameTime());
   }

   public long getTicksSinceLastRelease(CameraHolder holder, ItemStack stack) {
      return holder.asHolderEntity().level.getGameTime() - this.getLastReleaseTick(stack);
   }

   public long getStartTick(ItemStack stack) {
      return (Long)Exposure.DataComponents.TIMER_START_TICK.getOrDefault(stack, -1L);
   }

   public void setStartTick(ItemStack stack, long tick) {
      Exposure.DataComponents.TIMER_START_TICK.set(stack, tick);
   }

   public long getEndTick(ItemStack stack) {
      return (Long)Exposure.DataComponents.TIMER_END_TICK.getOrDefault(stack, -1L);
   }

   public void setEndTick(ItemStack stack, long tick) {
      Exposure.DataComponents.TIMER_END_TICK.set(stack, tick);
   }

   public long getLastReleaseTick(ItemStack stack) {
      return (Long)Exposure.DataComponents.TIMER_LAST_RELEASE_TICK.getOrDefault(stack, -1L);
   }

   public void setLastReleaseTick(ItemStack stack, long tick) {
      Exposure.DataComponents.TIMER_LAST_RELEASE_TICK.set(stack, tick);
   }

   public boolean tick(CameraHolder holder, ServerLevel level, ItemStack stack) {
      long releaseTick = this.getEndTick(stack);
      if (releaseTick <= -1L) {
         return false;
      } else {
         long currentTick = level.getGameTime();
         long remainingTicks = releaseTick - currentTick;
         if (remainingTicks < -5L) {
            this.stop(stack);
            return true;
         } else if (remainingTicks == 0L) {
            this.setEndTick(stack, currentTick);
            this.setLastReleaseTick(stack, currentTick);
            if (stack.getItem() instanceof CameraItem cameraItem) {
               cameraItem.release(holder, stack);
            }

            this.stop(stack);
            return true;
         } else {
            if (remainingTicks % (long)this.getTickingInterval(remainingTicks) == 0L) {
               this.playTickSound(holder);
            }

            return false;
         }
      }
   }

   protected void playTickSound(CameraHolder holder) {
      Sound.play(holder.asHolderEntity(), Exposure.SoundEvents.CAMERA_TIMER_TICK.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
   }

   protected int getTickingInterval(long remainingTicks) {
      if (remainingTicks > 100L) {
         return 10;
      } else if (remainingTicks > 50L) {
         return 6;
      } else {
         return remainingTicks > 25L ? 4 : 2;
      }
   }
}
