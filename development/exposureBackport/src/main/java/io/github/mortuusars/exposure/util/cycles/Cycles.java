package io.github.mortuusars.exposure.util.cycles;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Cycles {
   private final Queue<Task<?>> queuedTasks = new LinkedList<>();
   private final List<Task<?>> parallelTasks = new ArrayList<>();

   public void enqueueTask(Task<?> task) {
      Preconditions.checkState(!this.isInQueue(task), "This task is already in queue.");
      this.queuedTasks.add(task);
   }

   public void addParallelTask(Task<?> task) {
      Preconditions.checkState(!this.isInParallelTaskList(task), "This task is already added.");
      this.parallelTasks.add(task);
   }

   public boolean isInQueue(Task<?> task) {
      return this.queuedTasks.contains(task);
   }

   public boolean isInParallelTaskList(Task<?> task) {
      return this.parallelTasks.contains(task);
   }

   public void tick() {
      this.tickQueuedTasks();
      this.tickParallelTasks();
   }

   private void tickQueuedTasks() {
      Task<?> task = this.queuedTasks.peek();
      if (task != null) {
         if (!task.isStarted()) {
            task.execute();
         }

         task.tick();
         if (task.isDone()) {
            this.queuedTasks.remove(task);
         }
      }
   }

   private void tickParallelTasks() {
      this.parallelTasks.removeIf(task -> {
         if (!task.isStarted()) {
            task.execute();
         }

         task.tick();
         return task.isDone();
      });
   }
}
