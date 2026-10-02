package io.github.mortuusars.exposure.util;

public class RateLimiter {
   private final int maxTokens;
   private final int refillRate;
   private double currentTokens;
   private long lastRefillTimestamp;

   public RateLimiter(int maxTokens, int refillRatePerSecond) {
      this.maxTokens = maxTokens;
      this.refillRate = refillRatePerSecond;
      this.currentTokens = (double)maxTokens;
      this.lastRefillTimestamp = System.currentTimeMillis();
   }

   private void refillTokens() {
      long now = System.currentTimeMillis();
      long timeElapsed = now - this.lastRefillTimestamp;
      double tokensToAdd = (double)timeElapsed / 1000.0 * (double)this.refillRate;
      this.currentTokens = Math.min((double)this.maxTokens, this.currentTokens + tokensToAdd);
      this.lastRefillTimestamp = now;
   }

   public synchronized boolean tryConsume() {
      this.refillTokens();
      if (this.currentTokens >= 1.0) {
         this.currentTokens--;
         return true;
      } else {
         return false;
      }
   }
}
