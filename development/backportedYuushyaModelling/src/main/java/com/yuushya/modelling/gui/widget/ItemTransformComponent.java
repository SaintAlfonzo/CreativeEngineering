// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ItemTransformComponent.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import com.yuushya.modelling.gui.compat.ButtonUtils;
import com.yuushya.modelling.blockentity.itemblock.ItemBlockEntity;
import com.yuushya.modelling.blockentity.transformData.ItemTransformType;
import com.yuushya.modelling.gui.validate.LazyDoubleRange;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class ItemTransformComponent {
   public ItemTransformType type;
   public double standardStep;
   public double fine_tuneStep = 0.001;
   public SliderButton<Double> sliderButton;
   public Button minusButton;
   public Button addButton;
   public EditBox editBox;
   public Button cancelButton;
   public Button finishButton;

   public ItemTransformComponent(ItemTransformType type) {
      this.type = type;
   }

   public double setStandardStep(double step) {
      this.standardStep = step;
      return step;
   }

   public void setSliderInitial(ItemBlockEntity blockEntity, int slot) {
      double step = this.sliderButton.getStep();
      this.sliderButton.setStep(this.fine_tuneStep);
      this.sliderButton.setInitialValidatedValue(this.type.extract(blockEntity, slot));
      this.sliderButton.setStep(step);
      if (this.editBox != null) {
         this.editBox.setValue(String.valueOf(this.sliderButton.getValidatedValue()));
      }
   }

   public void setSliderStep() {
      this.sliderButton.setStep(this.standardStep);
   }

   public void setSliderFineTune() {
      this.sliderButton.setStep(this.fine_tuneStep);
   }

   public void initWidget(Font font) {
      this.minusButton = ButtonUtils.builder(Component.literal("-"), btn -> {
         double currentValue = this.sliderButton.getValidatedValue();
         double newValue = currentValue - this.sliderButton.getStep();
         this.sliderButton.setValidatedValue(newValue);
      }).bounds(this.sliderButton.x - 10, this.sliderButton.y, 10, 20).build();
      this.addButton = ButtonUtils.builder(Component.literal("+"), btn -> {
         double currentValue = this.sliderButton.getValidatedValue();
         double newValue = currentValue + this.sliderButton.getStep();
         this.sliderButton.setValidatedValue(newValue);
      }).bounds(this.sliderButton.x + this.sliderButton.getWidth(), this.sliderButton.y, 10, 20).build();
      this.editBox = new EditBox(font, this.sliderButton.x, this.sliderButton.y, this.sliderButton.getWidth(), 20, Component.empty());
      this.editBox.setMaxLength(15);
      this.editBox.visible = false;
      this.cancelButton = ButtonUtils.builder(Component.literal("\u00d7").withStyle(ChatFormatting.RED), btn -> {
         this.editBox.setValue(String.valueOf(this.sliderButton.getValidatedValue()));
         this.triggerVisible(true);
      }).bounds(this.sliderButton.x - 10, this.sliderButton.y, 10, 20).build();
      this.cancelButton.visible = false;
      this.finishButton = ButtonUtils.builder(Component.literal("\u2713").withStyle(ChatFormatting.GREEN), btn -> {
         this.saveEditBoxValue();
         this.triggerVisible(true);
      }).bounds(this.sliderButton.x + this.sliderButton.getWidth(), this.sliderButton.y, 10, 20).build();
      this.finishButton.visible = false;
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

   public void setEditBoxInitial() {
      this.editBox.setValue(String.valueOf(this.sliderButton.getValidatedValue()));
   }

   public void triggerVisible(boolean sliderVisible) {
      this.sliderButton.visible = sliderVisible;
      this.minusButton.visible = sliderVisible;
      this.addButton.visible = sliderVisible;
      this.editBox.setVisible(!sliderVisible);
      this.cancelButton.visible = !sliderVisible;
      this.finishButton.visible = !sliderVisible;
      if (!sliderVisible) {
         this.editBox.setValue(String.valueOf(this.sliderButton.getValidatedValue()));
      }
   }

   public void setInvisible() {
      this.sliderButton.visible = false;
      this.minusButton.visible = false;
      this.addButton.visible = false;
      this.editBox.setVisible(false);
      this.cancelButton.visible = false;
      this.finishButton.visible = false;
   }
}
