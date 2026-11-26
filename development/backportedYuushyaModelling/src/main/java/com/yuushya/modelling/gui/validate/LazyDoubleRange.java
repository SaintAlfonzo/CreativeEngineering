// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/LazyDoubleRange.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

import com.yuushya.modelling.gui.widget.SliderButton;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class LazyDoubleRange implements ValidateRange<Double>, StepRange<Double> {
   private Supplier<Double> minInclusiveSupplier;
   private Supplier<Double> maxInclusiveSupplier;
   private double step = 0.001;
   private Supplier<Double> stepSupplier;

   public LazyDoubleRange(Supplier<Double> minInclusiveSupplier, Supplier<Double> maxInclusiveSupplier, Supplier<Double> stepSupplier) {
      this(minInclusiveSupplier, maxInclusiveSupplier);
      this.stepSupplier = stepSupplier;
      this.step = 0.0;
   }

   public LazyDoubleRange(Supplier<Double> minInclusiveSupplier, Supplier<Double> maxInclusiveSupplier) {
      this.minInclusiveSupplier = minInclusiveSupplier;
      this.maxInclusiveSupplier = maxInclusiveSupplier;
      this.stepSupplier = () -> this.step;
   }

   public Double minInclusive() {
      return this.minInclusiveSupplier.get();
   }

   public Double maxInclusive() {
      return this.maxInclusiveSupplier.get();
   }

   public Optional<Double> validateValue(Double value) {
      if (value < this.minInclusive()) {
         return Optional.of(this.minInclusive());
      } else {
         return value > this.maxInclusive() ? Optional.of(this.maxInclusive()) : Optional.of(DoubleRange.near(value / this.getStep()) * this.getStep());
      }
   }

   public void setStep(Double step) {
      this.step = step;
   }

   public Double getStep() {
      return this.step == 0.0 ? this.stepSupplier.get() : this.step;
   }

   public double toSliderValue(Double value) {
      return Mth.map(value, this.minInclusive(), this.maxInclusive(), 0.0, 1.0);
   }

   public Double fromSliderValue(double sliderValue) {
      return Mth.map(sliderValue, 0.0, 1.0, this.minInclusive(), this.maxInclusive());
   }

   public static Component captionToString(Component caption, Double value) {
      return Component.empty().append(caption).append(Component.literal(":").append(Component.literal(String.format("%05.1f", value))));
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         LazyDoubleRange that = (LazyDoubleRange)obj;
         return Objects.equals(this.minInclusiveSupplier.get(), that.minInclusiveSupplier.get())
            && Objects.equals(this.maxInclusiveSupplier.get(), that.maxInclusiveSupplier.get())
            && Objects.equals(this.step, that.step);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minInclusiveSupplier.get(), this.maxInclusiveSupplier.get(), this.step);
   }

   @Override
   public String toString() {
      return "LazyDoubleRange[minInclusive="
         + this.minInclusiveSupplier.get()
         + ", maxInclusive="
         + this.maxInclusiveSupplier.get()
         + ", step="
         + this.step
         + "]";
   }

   public static LazyDoubleRange.ButtonBuilder buttonBuilder(
      Component caption, Supplier<Double> minInclusiveSupplier, Supplier<Double> maxInclusiveSupplier, Consumer<Double> onValueChanged
   ) {
      return new LazyDoubleRange.ButtonBuilder(caption, minInclusiveSupplier, maxInclusiveSupplier, onValueChanged);
   }

   public static LazyDoubleRange.ButtonBuilder buttonBuilder(
      Component caption,
      Supplier<Double> minInclusiveSupplier,
      Supplier<Double> maxInclusiveSupplier,
      Supplier<Double> stepSupplier,
      Consumer<Double> onValueChanged
   ) {
      return new LazyDoubleRange.ButtonBuilder(caption, minInclusiveSupplier, maxInclusiveSupplier, stepSupplier, onValueChanged);
   }

   public void setMinInclusiveSupplier(Supplier<Double> minInclusiveSupplier) {
      this.minInclusiveSupplier = minInclusiveSupplier;
   }

   public void setMaxInclusiveSupplier(Supplier<Double> maxInclusiveSupplier) {
      this.maxInclusiveSupplier = maxInclusiveSupplier;
   }

   public void setStepSupplier(Supplier<Double> stepSupplier) {
      this.stepSupplier = stepSupplier;
   }

   public static class ButtonBuilder extends SliderButton.Builder<Double> {
      public ButtonBuilder(Component caption, Supplier<Double> minInclusiveSupplier, Supplier<Double> maxInclusiveSupplier, Consumer<Double> onValueChanged) {
         super(caption, new LazyDoubleRange(minInclusiveSupplier, maxInclusiveSupplier), onValueChanged);
         this.text(LazyDoubleRange::captionToString);
         this.initial(minInclusiveSupplier.get());
      }

      public ButtonBuilder(
         Component caption,
         Supplier<Double> minInclusiveSupplier,
         Supplier<Double> maxInclusiveSupplier,
         Supplier<Double> stepSupplier,
         Consumer<Double> onValueChanged
      ) {
         super(caption, new LazyDoubleRange(minInclusiveSupplier, maxInclusiveSupplier, stepSupplier), onValueChanged);
         this.text(LazyDoubleRange::captionToString);
         this.initial(minInclusiveSupplier.get());
      }
   }
}
