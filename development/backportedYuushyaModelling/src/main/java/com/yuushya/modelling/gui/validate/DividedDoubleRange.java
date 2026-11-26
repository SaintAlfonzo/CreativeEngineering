// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/DividedDoubleRange.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

import com.yuushya.modelling.gui.widget.SliderButton;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DividedDoubleRange extends DoubleRange {
   private final Double middle;

   public DividedDoubleRange(Double minInclusive, Double middle, Double maxInclusive) {
      super(minInclusive, maxInclusive);
      this.middle = middle;
   }

   @Override
   public Double fromSliderValue(double sliderValue) {
      return sliderValue <= 0.5
         ? Mth.map(sliderValue, 0.0, 0.5, this.minInclusive, this.middle)
         : Mth.map(sliderValue, 0.5, 1.0, this.middle, this.maxInclusive);
   }

   @Override
   public double toSliderValue(Double value) {
      return value <= this.middle
         ? Mth.map(value, this.minInclusive, this.middle, 0.0, 0.5)
         : Mth.map(value, this.middle, this.maxInclusive, 0.5, 1.0);
   }

   public static DividedDoubleRange.ButtonBuilder buttonBuilder(
      Component caption, Double minInclusive, Double middle, Double maxInclusive, Consumer<Double> onValueChanged
   ) {
      return new DividedDoubleRange.ButtonBuilder(caption, minInclusive, middle, maxInclusive, onValueChanged);
   }

   public static class ButtonBuilder extends SliderButton.Builder<Double> {
      public ButtonBuilder(Component caption, Double minInclusive, Double middle, Double maxInclusive, Consumer<Double> onValueChanged) {
         super(caption, new DividedDoubleRange(minInclusive, middle, maxInclusive), onValueChanged);
         this.text(LazyDoubleRange::captionToString);
         this.initial(minInclusive);
      }
   }
}
