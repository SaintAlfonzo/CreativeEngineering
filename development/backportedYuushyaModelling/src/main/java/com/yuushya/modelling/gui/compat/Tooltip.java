// SPDX-License-Identifier: GPL-3.0-only
// Port helper (after Yuushya Modelling 2.2.0's 1.19.2 gui/TooltipUtils, MIT): 1.19.3's Tooltip.create(Component) as a
// 1.19.2 Button.OnTooltip drawn by the current screen, plus a draw call for widgets that are not Buttons.
package com.yuushya.modelling.gui.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Button.OnTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class Tooltip {
   private Tooltip() {
   }

   public static OnTooltip create(Component component) {
      return new OnTooltip() {
         @Override
         public void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
            Tooltip.render(component, poseStack, mouseX, mouseY);
         }

         @Override
         public void narrateTooltip(Consumer<Component> contents) {
            contents.accept(component);
         }
      };
   }

   /** 1.20 CycleButton.withTooltip(value -> Tooltip.create(text)): 1.19.2 takes the wrapped lines instead. */
   public static List<FormattedCharSequence> lines(@Nullable Component component) {
      return component == null ? List.of() : Minecraft.getInstance().font.split(component, 170);
   }

   /**
    * 1.19.2 only draws TooltipAccessor tooltips (CycleButton, our SliderButton) in options screens; 1.20 widgets draw
    * their own. Screens call this at the end of render() to draw the hovered widget's tooltip.
    */
   public static void renderHovered(Screen screen, List<? extends GuiEventListener> children, PoseStack poseStack, int mouseX, int mouseY) {
      for (GuiEventListener child : children) {
         if (child instanceof AbstractWidget widget && child instanceof TooltipAccessor accessor && widget.visible && widget.isHoveredOrFocused()
            && widget.isMouseOver((double)mouseX, (double)mouseY)) {
            List<FormattedCharSequence> lines = accessor.getTooltip();
            if (!lines.isEmpty()) {
               screen.renderTooltip(poseStack, lines, mouseX, mouseY);
            }
            return;
         }
      }
   }

   /** Draws a tooltip with the open screen's font and tooltip style; no-op without a screen or text. */
   public static void render(@Nullable Component component, PoseStack poseStack, int mouseX, int mouseY) {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null && component != null) {
         screen.renderTooltip(poseStack, Minecraft.getInstance().font.split(component, Math.max(screen.width / 2 - 43, 170)), mouseX, mouseY);
      }
   }
}
