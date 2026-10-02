package io.github.mortuusars.exposure.client.task;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.util.concurrent.CompletableFuture;

public class ClearStaleRenderedImagesIndefiniteTask extends Task<Void> {
   private final CompletableFuture<Void> future = new CompletableFuture<>();

   @Override
   public CompletableFuture<Void> execute() {
      return this.future;
   }

   @Override
   public void tick() {
      if (Minecrft.get().level != null && Minecrft.get().level.getGameTime() % 2400L == 0L) {
         ExposureClient.renderedExposures().clearStale();
      }
   }
}
