// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/TransformComponent.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.yuushya.modelling.gui.compat.ButtonUtils;
import com.yuushya.modelling.blockentity.showblock.ShowBlockEntity;
import com.yuushya.modelling.blockentity.transformData.TransformType;
import com.yuushya.modelling.gui.validate.LazyDoubleRange;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TransformComponent {
   public TransformType type;
   public double standardStep;
   public double fine_tuneStep = 0.001;
   public SliderButton<Double> sliderButton;
   public Button minusButton;
   public Button addButton;
   public EditBox editBox;
   public Button cancelButton;
   public Button finishButton;

   public TransformComponent(TransformType type) {
      this.type = type;
   }

   public double setStandardStep(double step) {
      this.standardStep = step;
      return step;
   }

   public void setSliderInitial(ShowBlockEntity blockEntity, int slot) {
      double step = this.sliderButton.getStep();
      this.sliderButton.setStep(this.fine_tuneStep);
      this.sliderButton.setInitialValidatedValue(this.type.extract(blockEntity, slot));
      this.sliderButton.setStep(step);
   }

   public void setSliderStep() {
      this.sliderButton.setStep(this.standardStep);
   }

   public void setSliderFineTune() {
      this.sliderButton.setStep(this.fine_tuneStep);
   }

   public void step(boolean increase) {
      if (increase) {
         this.sliderButton.setValidatedValue(this.sliderButton.getValidatedValue() + this.sliderButton.getStep());
      } else {
         this.sliderButton.setValidatedValue(this.sliderButton.getValidatedValue() - this.sliderButton.getStep());
      }
   }

   public void setEditBoxInitial() {
      this.editBox.setValue(String.valueOf(this.sliderButton.getValidatedValue()));
   }

   public void saveEditBoxValue() {
      double number;
      try {
         number = Double.parseDouble(this.editBox.getValue());
      } catch (NumberFormatException var11) {
         number = this.sliderButton.getValidatedValue();
      }

      if (this.sliderButton.getValidateRange() instanceof LazyDoubleRange doubleValidateRange) {
         double min = doubleValidateRange.minInclusive();
         double max = doubleValidateRange.maxInclusive();
         if (number < min) {
            double finalNumber = number;
            doubleValidateRange.setMinInclusiveSupplier(() -> finalNumber);
         }

         if (number > max) {
            double finalNumber = number;
            doubleValidateRange.setMaxInclusiveSupplier(() -> finalNumber);
         }
      }

      this.sliderButton.setValidatedValue(number);
      this.setEditBoxInitial();
   }

   public void triggerVisible(boolean sliderVisible) {
      this.sliderButton.visible = sliderVisible;
      this.addButton.visible = sliderVisible;
      this.minusButton.visible = sliderVisible;
      if (!sliderVisible) {
         this.setEditBoxInitial();
      }

      this.editBox.setVisible(!sliderVisible);
      this.finishButton.visible = !sliderVisible;
      this.cancelButton.visible = !sliderVisible;
   }

   Component editBoxComponent() {
      MutableComponent component = switch (this.type) {
         case POS_X, ROT_X -> Component.translatable("block.yuushya.showblock.x", new Object[]{""}).withStyle(ChatFormatting.DARK_RED);
         case POS_Y, ROT_Y -> Component.translatable("block.yuushya.showblock.y", new Object[]{""}).withStyle(ChatFormatting.GREEN);
         case POS_Z, ROT_Z -> Component.translatable("block.yuushya.showblock.z", new Object[]{""}).withStyle(ChatFormatting.BLUE);
         case SCALE_X -> Component.translatable("gui.yuushya.showBlockScreen.scale_text", new Object[]{""});
         default -> Component.empty();
      };
      return Component.empty().append(this.sliderButton.getCaption()).append(component);
   }

   public void initWidget(Font font) {
      this.minusButton = ButtonUtils.builder(Component.literal("-"), btn -> this.step(false))
         .bounds(this.sliderButton.x - 10, this.sliderButton.y, 10, 20)
         .build();
      this.addButton = ButtonUtils.builder(Component.literal("+"), btn -> this.step(true))
         .bounds(this.sliderButton.x + this.sliderButton.getWidth(), this.sliderButton.y, 10, 20)
         .build();
      this.editBox = new EditBox(font, this.sliderButton.x, this.sliderButton.y, this.sliderButton.getWidth(), 20, this.editBoxComponent());
      this.editBox.setMaxLength(15);
      this.setEditBoxInitial();
      this.cancelButton = ButtonUtils.builder(Component.literal("\u00d7"), btn -> this.setEditBoxInitial())
         .bounds(this.sliderButton.x - 10, this.sliderButton.y, 10, 20)
         .build();
      this.finishButton = ButtonUtils.builder(Component.literal("\u221a"), btn -> this.saveEditBoxValue())
         .bounds(this.sliderButton.x + this.sliderButton.getWidth(), this.sliderButton.y, 10, 20)
         .build();
      this.triggerVisible(true);
   }
}
