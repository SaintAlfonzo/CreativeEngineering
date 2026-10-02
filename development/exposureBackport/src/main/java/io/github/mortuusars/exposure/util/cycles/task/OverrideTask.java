package io.github.mortuusars.exposure.util.cycles.task;

import java.util.concurrent.CompletableFuture;

public class OverrideTask<T> extends Task<T> {
   private final Task<T> base;
   private final Task<T> override;

   public OverrideTask(Task<T> base, Task<T> override) {
      this.base = base;
      this.override = override;
   }

   @Override
   public CompletableFuture<T> execute() {
      return this.base.execute().thenCompose(baseResult -> this.override.execute().exceptionally(exception -> (T)baseResult).thenApply(overrideResult -> {
            if (overrideResult instanceof Result<?> result && result.isError()) {
               return (T)(this.isResultSuccessful((T)baseResult) ? baseResult : overrideResult);
            }

            return (T)overrideResult;
         }));
   }

   private boolean isResultSuccessful(T resultValue) {
      return resultValue != null && (!(resultValue instanceof Result<?> result) || result.isSuccessful());
   }

   @Override
   public void tick() {
      this.base.tick();
      this.override.tick();
   }

   @Override
   public boolean isDone() {
      return this.base.isDone() && this.override.isDone();
   }

   @Override
   public boolean isStarted() {
      return this.base.isStarted() || this.override.isStarted();
   }
}
