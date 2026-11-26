// SPDX-License-Identifier: GPL-3.0-only
// Port helper (from Yuushya Modelling 2.2.0's 1.19.2 gui/ButtonUtils, MIT): 1.19.3's Button.builder on 1.19.2's
// Button constructor, so the 2.4.1 screens keep their builder chains. See PORT_NOTES.md.
package com.yuushya.modelling.gui.compat;

import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.Button.OnTooltip;
import net.minecraft.network.chat.Component;

public class ButtonUtils {
   public static ButtonUtils.Builder builder(Component message, OnPress onPress) {
      return new ButtonUtils.Builder(message, onPress);
   }

   public static class Builder {
      private final Component message;
      private final OnPress onPress;
      @Nullable
      private OnTooltip tooltip;
      private int x;
      private int y;
      private int width = 150;
      private int height = 20;

      public Builder(Component message, OnPress onPress) {
         this.message = message;
         this.onPress = onPress;
      }

      public ButtonUtils.Builder pos(int x, int y) {
         this.x = x;
         this.y = y;
         return this;
      }

      public ButtonUtils.Builder width(int width) {
         this.width = width;
         return this;
      }

      public ButtonUtils.Builder size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public ButtonUtils.Builder bounds(int x, int y, int width, int height) {
         return this.pos(x, y).size(width, height);
      }

      public ButtonUtils.Builder tooltip(@Nullable OnTooltip tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public Button build() {
         return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.tooltip == null ? Button.NO_TOOLTIP : this.tooltip);
      }
   }
}
