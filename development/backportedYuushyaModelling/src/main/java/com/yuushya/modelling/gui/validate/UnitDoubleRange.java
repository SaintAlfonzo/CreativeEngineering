// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/validate/UnitDoubleRange.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.validate;

import com.yuushya.modelling.gui.widget.SliderButton;
import java.util.function.Consumer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public record UnitDoubleRange() implements ValidateRange<Double> {
   public double toSliderValue(Double value) {
      return value;
   }

   public Double fromSliderValue(double sliderValue) {
      return sliderValue;
   }

   public Double minInclusive() {
      return 0.0;
   }

   public Double maxInclusive() {
      return 1.0;
   }

   public void setStep(Double step) {
   }

   public Double getStep() {
      return 0.001;
   }

   public static Component percentValueLabel(Component text, double value) {
      return Component.translatable("options.percent_value", new Object[]{text, (int)(value * 100.0)});
   }

   public static Component genericValueLabel(Component text, Component value) {
      return Component.translatable("options.generic_value", new Object[]{text, value});
   }

   public static Component captionToString(Component caption, Double value) {
      return value == 0.0 ? genericValueLabel(caption, CommonComponents.OPTION_OFF) : percentValueLabel(caption, value);
   }

   public static UnitDoubleRange.ButtonBuilder buttonBuilder(Component caption, Consumer<Double> onValueChanged) {
      return new UnitDoubleRange.ButtonBuilder(caption, onValueChanged);
   }

   public static class ButtonBuilder extends SliderButton.Builder<Double> {
      public ButtonBuilder(Component caption, Consumer<Double> onValueChanged) {
         super(caption, new UnitDoubleRange(), onValueChanged);
         this.text(UnitDoubleRange::captionToString);
         this.initial(Double.valueOf(0.0));
      }
   }
}
