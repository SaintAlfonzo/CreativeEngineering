// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/DoubleRange.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

import com.yuushya.modelling.gui.widget.SliderButton;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DoubleRange implements ValidateRange<Double>, StepRange<Double> {
   protected final Double minInclusive;
   protected final Double maxInclusive;
   private double step = 0.001;

   public DoubleRange(Double minInclusive, Double maxInclusive) {
      this.minInclusive = minInclusive;
      this.maxInclusive = maxInclusive;
   }

   public Optional<Double> validateValue(Double value) {
      if (value < this.minInclusive()) {
         return Optional.of(this.minInclusive());
      } else {
         return value > this.maxInclusive() ? Optional.of(this.maxInclusive()) : Optional.of(near(value / this.step) * this.step);
      }
   }

   public double toSliderValue(Double value) {
      return Mth.map(value, this.minInclusive, this.maxInclusive, 0.0, 1.0);
   }

   public Double fromSliderValue(double sliderValue) {
      return Mth.map(sliderValue, 0.0, 1.0, this.minInclusive, this.maxInclusive);
   }

   public Double minInclusive() {
      return this.minInclusive;
   }

   public Double maxInclusive() {
      return this.maxInclusive;
   }

   public void setStep(Double step) {
      this.step = step;
   }

   public Double getStep() {
      return this.step;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         DoubleRange that = (DoubleRange)obj;
         return Objects.equals(this.minInclusive, that.minInclusive)
            && Objects.equals(this.maxInclusive, that.maxInclusive)
            && Objects.equals(this.step, that.step);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minInclusive, this.maxInclusive, this.step);
   }

   @Override
   public String toString() {
      return "DoubleRange[minInclusive=" + this.minInclusive + ", maxInclusive=" + this.maxInclusive + ", step=" + this.step + "]";
   }

   public static DoubleRange.ButtonBuilder buttonBuilder(Component caption, Double minInclusive, Double maxInclusive, Consumer<Double> onValueChanged) {
      return new DoubleRange.ButtonBuilder(caption, minInclusive, maxInclusive, onValueChanged);
   }

   public static Double near(Double value) {
      double ceil = Math.ceil(value);
      return ceil - value < 0.001 ? ceil : Math.floor(value);
   }

   public static class ButtonBuilder extends SliderButton.Builder<Double> {
      public ButtonBuilder(Component caption, Double minInclusive, Double maxInclusive, Consumer<Double> onValueChanged) {
         super(caption, new DoubleRange(minInclusive, maxInclusive), onValueChanged);
         this.text(LazyDoubleRange::captionToString);
         this.initial(minInclusive);
      }
   }
}
