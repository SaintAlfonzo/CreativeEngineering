package io.github.mortuusars.exposure.server;

import io.github.mortuusars.exposure.util.TranslatableError;
import io.github.mortuusars.exposure.world.camera.CameraId;
import java.util.Optional;
import net.minecraft.world.level.Level;

public class CameraInstance {
   protected final CameraId cameraId;
   private int deferredCooldownTicks = 0;
   private long projectionDeadline = -1L;
   private CameraInstance.ProjectionState projectionState = CameraInstance.ProjectionState.IDLE;
   private Optional<TranslatableError> projectionError = Optional.empty();

   public CameraInstance(CameraId cameraId) {
      this.cameraId = cameraId;
   }

   public int getDeferredCooldown() {
      return this.deferredCooldownTicks;
   }

   public void setDeferredCooldown(int ticks) {
      this.deferredCooldownTicks = ticks;
   }

   public CameraInstance.ProjectionState getProjectionState(Level level) {
      return this.isWaitingForProjection() && this.isProjectionTimedOut(level) ? CameraInstance.ProjectionState.TIMED_OUT : this.projectionState;
   }

   public Optional<TranslatableError> getProjectionError(Level level) {
      return this.isWaitingForProjection() && this.isProjectionTimedOut(level) ? Optional.of(TranslatableError.TIMED_OUT) : this.projectionError;
   }

   public long getProjectionDeadline() {
      return this.projectionDeadline;
   }

   public void waitForProjection(long deadline) {
      this.projectionDeadline = deadline;
   }

   public boolean isWaitingForProjection() {
      return this.projectionDeadline >= 0L;
   }

   public boolean isProjectionTimedOut(Level level) {
      return level.getGameTime() > this.projectionDeadline;
   }

   public void setProjectionResult(Level level, boolean successful, Optional<TranslatableError> error) {
      if (this.isWaitingForProjection() && !this.isProjectionTimedOut(level)) {
         this.projectionState = successful ? CameraInstance.ProjectionState.SUCCESSFUL : CameraInstance.ProjectionState.FAILED;
         this.projectionDeadline = -1L;
         this.projectionError = error;
      }
   }

   public void stopWaitingForProjection() {
      this.projectionState = CameraInstance.ProjectionState.IDLE;
      this.projectionDeadline = -1L;
      this.projectionError = Optional.empty();
   }

   public static enum ProjectionState {
      IDLE,
      WAITING,
      SUCCESSFUL,
      FAILED,
      TIMED_OUT;
   }
}
