// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/SliderButton.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.yuushya.modelling.gui.validate.ValidateRange;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.OptionInstance.CaptionBasedToString;
import net.minecraft.client.OptionInstance.TooltipSupplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class SliderButton<T extends Comparable<T>> extends AbstractSliderButton implements TooltipAccessor {
   private final CaptionBasedToString<T> captionBasedToString;
   private final T initialValue;
   private final Component caption;
   private ValidateRange<T> validateRange;
   private final TooltipSupplier<T> tooltipSupplier;
   private final Consumer<SliderButton<T>> onMouseOver;
   private final Consumer<T> onValueChanged;

   public SliderButton(
      Component caption,
      int x,
      int y,
      int width,
      int height,
      TooltipSupplier<T> tooltipSupplier,
      CaptionBasedToString<T> captionBasedToString,
      ValidateRange<T> validateRange,
      T initialValue,
      Consumer<SliderButton<T>> onMouseOver,
      Consumer<T> onValueChanged
   ) {
      super(x, y, width, height, CommonComponents.EMPTY, validateRange.toSliderValue(initialValue));
      this.caption = caption;
      this.initialValue = initialValue;
      this.validateRange = validateRange;
      this.tooltipSupplier = tooltipSupplier;
      this.captionBasedToString = captionBasedToString;
      this.onMouseOver = onMouseOver;
      this.onValueChanged = onValueChanged;
      this.updateMessage();
   }

   @Override
   public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      super.renderButton(poseStack, mouseX, mouseY, partialTick);
      if (this.isHovered) {
         this.onMouseOver.accept(this);
      }
   }

   protected void updateMessage() {
      T value = this.validateRange.fromSliderValue(this.value);
      T object = this.validateRange.validateValue(value).orElse(this.initialValue);
      this.setMessage(this.captionBasedToString.toString(this.caption, object));
   }

   // 1.19.2: the tooltip is read through TooltipAccessor (drawn by the screen, Tooltip.renderHovered) instead of
   // 1.20's setTooltip on every message update.
   @Override
   public List<FormattedCharSequence> getTooltip() {
      T value = this.validateRange.fromSliderValue(this.value);
      return this.tooltipSupplier.apply(this.validateRange.validateValue(value).orElse(this.initialValue));
   }

   protected void applyValue() {
      T value = this.validateRange.fromSliderValue(this.value);
      T object = this.validateRange.validateValue(value).orElseGet(() -> this.initialValue);
      this.onValueChanged.accept(object);
   }

   public static <R extends Comparable<R>> SliderButton.Builder<R> builder(Component caption, ValidateRange<R> validateRange, Consumer<R> onValueChanged) {
      return new SliderButton.Builder<>(caption, validateRange, onValueChanged);
   }

   public void setValidatedValue(T value) {
      this.setValue(this.validateRange.toSliderValue(value));
   }

   public void setInitialValidatedValue(T value) {
      this.value = this.validateRange.toSliderValue(value);
      this.updateMessage();
   }

   public T getValidatedValue() {
      return this.validateRange.fromSliderValue(this.value);
   }

   public void setValue(double value) {
      double d = this.value;
      this.value = Mth.clamp(value, 0.0, 1.0);
      if (d != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   public void setStep(T step) {
      this.validateRange.setStep(step);
   }

   public T getStep() {
      return this.validateRange.getStep();
   }

   public Component getCaption() {
      return this.caption;
   }

   public void setValidateRange(ValidateRange<T> validateRange) {
      this.validateRange = validateRange;
   }

   public ValidateRange<T> getValidateRange() {
      return this.validateRange;
   }

   public static class Builder<R extends Comparable<R>> {
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;
      private CaptionBasedToString<R> captionBasedToString = (captionx, number) -> captionx;
      private TooltipSupplier<R> tooltipSupplier = object -> List.of();
      private R initialValue;
      private final Component caption;
      private final ValidateRange<R> validateRange;
      private final Consumer<R> onValueChanged;
      private Consumer<SliderButton<R>> onMouseOver = btn -> {
      };
      private R step;

      public Builder(Component caption, ValidateRange<R> validateRange, Consumer<R> onValueChanged) {
         this.caption = caption;
         this.onValueChanged = onValueChanged;
         this.validateRange = validateRange;
         this.initialValue = validateRange.defaultValue();
      }

      public SliderButton.Builder<R> pos(int x, int y) {
         this.x = x;
         this.y = y;
         return this;
      }

      public SliderButton.Builder<R> width(int width) {
         this.width = width;
         return this;
      }

      public SliderButton.Builder<R> size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public SliderButton.Builder<R> bounds(int x, int y, int width, int height) {
         return this.pos(x, y).size(width, height);
      }

      public SliderButton.Builder<R> text(CaptionBasedToString<R> textCalculate) {
         this.captionBasedToString = textCalculate;
         return this;
      }

      public SliderButton.Builder<R> tooltip(TooltipSupplier<R> tooltipSupplier) {
         this.tooltipSupplier = tooltipSupplier;
         return this;
      }

      public SliderButton.Builder<R> onMouseOver(Consumer<SliderButton<R>> onMouseOver) {
         this.onMouseOver = onMouseOver;
         return this;
      }

      public SliderButton.Builder<R> initial(R number) {
         this.initialValue = number;
         return this;
      }

      public SliderButton.Builder<R> step(R step) {
         this.step = step;
         return this;
      }

      public SliderButton<R> build() {
         this.validateRange.setStep(this.step);
         return new SliderButton<>(
            this.caption,
            this.x,
            this.y,
            this.width,
            this.height,
            this.tooltipSupplier,
            this.captionBasedToString,
            this.validateRange,
            this.initialValue,
            this.onMouseOver,
            this.onValueChanged
         );
      }
   }
}
