package io.github.mortuusars.exposure.client.gui.component;

import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import net.minecraft.util.Mth;

public class SteppedZoom {
   protected double defaultZoom = 1.0;
   protected int zoomInSteps = 4;
   protected int zoomOutSteps = 4;
   protected double zoomPerStep = 1.4;
   protected Animation animation = new Animation(300, EasingFunction.EASE_OUT_EXPO);
   protected double target;
   protected double current;

   public void zoom(ZoomDirection direction) {
      if (direction == ZoomDirection.IN) {
         this.zoomIn();
      } else {
         this.zoomOut();
      }
   }

   public void zoomIn() {
      this.setTarget(this.target * this.zoomPerStep);
   }

   public void zoomOut() {
      this.setTarget(this.target / this.zoomPerStep);
   }

   public double getTarget() {
      return this.target;
   }

   public void setTarget(double target) {
      this.current = this.get();
      this.target = Mth.clamp(target, this.getMin(), this.getMax());
      this.animation.resetProgress();
   }

   public double get() {
      return Mth.lerp(this.animation.getValue(), this.current, this.target);
   }

   public double getMin() {
      return this.defaultZoom / Math.pow(this.zoomPerStep, (double)this.zoomOutSteps);
   }

   public double getMax() {
      return this.defaultZoom * Math.pow(this.zoomPerStep, (double)this.zoomInSteps);
   }

   public double getDefaultZoom() {
      return this.defaultZoom;
   }

   public SteppedZoom defaultZoom(double defaultZoom) {
      this.defaultZoom = defaultZoom;
      this.setTarget(defaultZoom);
      return this;
   }

   public int getZoomInSteps() {
      return this.zoomInSteps;
   }

   public SteppedZoom zoomInSteps(int zoomInSteps) {
      this.zoomInSteps = zoomInSteps;
      return this;
   }

   public int getZoomOutSteps() {
      return this.zoomOutSteps;
   }

   public SteppedZoom zoomOutSteps(int zoomOutSteps) {
      this.zoomOutSteps = zoomOutSteps;
      return this;
   }

   public double getZoomPerStep() {
      return this.zoomPerStep;
   }

   public SteppedZoom zoomPerStep(double zoomPerStep) {
      this.zoomPerStep = zoomPerStep;
      return this;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public SteppedZoom animation(Animation animation) {
      this.animation = animation;
      return this;
   }
}
