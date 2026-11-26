// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/widget/AbstractColorScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.widget;

import java.awt.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class AbstractColorScreen extends Screen {
   protected int color = Color.WHITE.getRGB();

   protected AbstractColorScreen(Component title) {
      super(title);
   }

   public abstract Font getColorFont();

   public abstract EditBox getColorEditBox();

   public abstract void updateColorData(int var1);

   public abstract void setColorFocused(AbstractWidget var1);

   public abstract void setColorDragging(boolean var1);
}
