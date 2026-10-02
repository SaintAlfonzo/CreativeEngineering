package io.github.mortuusars.exposure.util.cycles.task;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AcceptTask<T> extends TransformedNestedTask<T, Void> {
   private final Consumer<T> acceptor;
   private final boolean async;

   public AcceptTask(Task<T> task, Consumer<T> acceptor, boolean async) {
      super(task);
      this.acceptor = acceptor;
      this.async = async;
   }

   @Override
   public CompletableFuture<Void> execute() {
      return this.async ? this.getTask().execute().thenAcceptAsync(this.acceptor) : this.getTask().execute().thenAccept(this.acceptor);
   }
}
