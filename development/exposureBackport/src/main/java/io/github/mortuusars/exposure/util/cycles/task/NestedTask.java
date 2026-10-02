package io.github.mortuusars.exposure.util.cycles.task;

public abstract class NestedTask<T> extends Task<T> {
   private final Task<T> task;

   public NestedTask(Task<T> task) {
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
