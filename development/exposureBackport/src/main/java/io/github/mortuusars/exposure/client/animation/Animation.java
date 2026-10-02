package io.github.mortuusars.exposure.client.animation;

public class Animation {
   protected final int duration;
   protected final EasingFunction easing;
   protected long startedAt;

   public Animation(int duration, EasingFunction easing) {
      this.duration = duration;
      this.easing = easing;
      this.startedAt = this.getCurrentTime();
   }

   public Animation(int duration) {
      this.duration = duration;
      this.easing = EasingFunction.LINEAR;
      this.startedAt = this.getCurrentTime();
   }

   public int getDuration() {
      return this.duration;
   }

   public EasingFunction getEasing() {
      return this.easing;
   }

   public double getValue() {
      if (this.isFinished()) {
         return 1.0;
      } else {
         long currentTime = this.getCurrentTime();
         double value = (double)(currentTime - this.startedAt) / (double)this.duration;
         return this.easing.ease(value);
      }
   }

   public boolean isFinished() {
      return this.getCurrentTime() >= this.startedAt + (long)this.duration;
   }

   public void resetProgress() {
      this.startedAt = this.getCurrentTime();
   }

   private long getCurrentTime() {
      return System.currentTimeMillis();
   }
}
