// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/ColorSliderButton.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class ColorSliderButton extends AbstractSliderButton {
   public ColorSliderButton(int i, int j, int k, int l, Component component, double d) {
      super(i, j, k, l, component, d);
   }

   protected void updateMessage() {
   }

   protected void applyValue() {
   }
}
