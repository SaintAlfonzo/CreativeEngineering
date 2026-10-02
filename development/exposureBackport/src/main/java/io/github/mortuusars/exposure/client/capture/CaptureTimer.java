package io.github.mortuusars.exposure.client.capture;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.util.Minecrft;
import java.util.function.Consumer;

public class CaptureTimer {
   private int ticks;
   private boolean isRunning;
   private long startedAtGameTick = -1L;
   private long lastGameTick = -1L;
   private Runnable onStart;
   private Consumer<Integer> onGameTick;
   private Runnable onEnd;

   public CaptureTimer(int ticks) {
      Preconditions.checkState(ticks >= 0, "Number of ticks cannot be negative. Ticks: %s", ticks);
      this.ticks = ticks;
   }

   public CaptureTimer whenStarted(Runnable whenStarted) {
      this.onStart = whenStarted;
      return this;
   }

   public CaptureTimer onGameTick(Consumer<Integer> onTick) {
      this.onGameTick = onTick;
      return this;
   }

   public CaptureTimer whenEnded(Runnable whenEnded) {
      this.onEnd = whenEnded;
      return this;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public boolean isDone() {
      return !this.isRunning && this.startedAtGameTick >= 0L && this.ticks <= 0;
   }

   public CaptureTimer start() {
      if (!this.isRunning) {
         this.isRunning = true;
         this.startedAtGameTick = this.getCurrentGameTick();
         this.lastGameTick = this.startedAtGameTick;
         this.onStart.run();
      }

      this.tick();
      return this;
   }

   public void pause() {
      this.isRunning = false;
   }

   public void tick() {
      if (this.isRunning) {
         long currentGameTick = this.getCurrentGameTick();
         boolean isNewTick = this.lastGameTick != currentGameTick;
         if (isNewTick) {
            this.ticks--;
            this.onGameTick.accept(this.ticks);
            this.lastGameTick = currentGameTick;
         }

         if (this.ticks <= 0) {
            this.stopAndEnd();
         }
      }
   }

   private void stopAndEnd() {
      this.isRunning = false;
      this.onEnd.run();
   }

   private long getCurrentGameTick() {
      return Minecrft.level().getGameTime();
   }
}
