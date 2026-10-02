package io.github.mortuusars.exposure.util.cycles.task;

public abstract class TransformedNestedTask<T, R> extends Task<R> {
   private final Task<T> task;

   public TransformedNestedTask(Task<T> task) {
      this.task = task;
   }

   public Task<T> getTask() {
      return this.task;
   }

   @Override
   public void tick() {
      this.task.tick();
   }

   @Override
   public boolean isStarted() {
      return this.task.isStarted();
   }

   @Override
   public boolean isDone() {
      return this.task.isDone();
   }
}
