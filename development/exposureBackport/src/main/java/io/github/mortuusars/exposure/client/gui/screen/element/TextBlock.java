package io.github.mortuusars.exposure.client.gui.screen.element;

import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import io.github.mortuusars.exposure.backport.gui.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextBlock extends AbstractWidget {
   public int fontColor = -16777216;
   public boolean drawShadow = false;
   public HorizontalAlignment alignment = HorizontalAlignment.LEFT;
   private final Font font;
   private final Function<Style, Boolean> componentClickedHandler;
   private List<FormattedCharSequence> renderedLines;
   private List<FormattedCharSequence> tooltipLines;

   public TextBlock(Font font, int x, int y, int width, int height, Component message, Function<Style, Boolean> componentClickedHandler) {
      super(x, y, width, height, message);
      this.font = font;
      this.componentClickedHandler = componentClickedHandler;
      this.makeLines();
   }

   public void setMessage(Component message) {
      super.setMessage(message);
      this.makeLines();
   }

   protected void makeLines() {
      Component text = this.getMessage();
      List<FormattedCharSequence> lines = this.font.split(text, this.getWidth());
      int availableLines = Math.min(lines.size(), this.height / 9);
      List<FormattedCharSequence> visibleLines = new ArrayList<>();

      for (int i = 0; i < availableLines; i++) {
         FormattedCharSequence line = lines.get(i);
         if (i == availableLines - 1 && availableLines < lines.size()) {
            line = FormattedCharSequence.composite(line, Component.literal("...").withStyle(text.getStyle()).getVisualOrderText());
         }

         visibleLines.add(line);
      }

      List<FormattedCharSequence> hiddenLines = Collections.emptyList();
      if (availableLines < lines.size()) {
         hiddenLines = new ArrayList<>(lines.stream().skip((long)availableLines).toList());
         hiddenLines.set(0, FormattedCharSequence.composite(FormattedCharSequence.forward("...", text.getStyle()), hiddenLines.get(0)));
      }

      this.renderedLines = visibleLines;
      this.tooltipLines = hiddenLines;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      Style style = this.getClickedComponentStyleAt(mouseX, mouseY);
      return button == 0 && style != null && this.componentClickedHandler.apply(style);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
   }

   @NotNull
   protected MutableComponent createNarrationMessage() {
      return this.getMessage().copy();
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      for (int i = 0; i < this.renderedLines.size(); i++) {
         FormattedCharSequence line = this.renderedLines.get(i);
         int x = this.getX() + this.alignment.align(this.getWidth(), this.font.width(line));
         guiGraphics.drawString(this.font, line, x, this.getY() + 9 * i, this.fontColor, this.drawShadow);
      }

      if (this.isHovered()) {
         Style style = this.getClickedComponentStyleAt((double)mouseX, (double)mouseY);
         if (style != null) {
            guiGraphics.renderComponentHoverEffect(this.font, style, mouseX, mouseY);
         }
      }

      if (!this.tooltipLines.isEmpty() && this.isMouseOver((double)mouseX, (double)mouseY)) {
         guiGraphics.renderTooltip(this.font, this.tooltipLines, DefaultTooltipPositioner.INSTANCE, mouseX, mouseY);
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
      if (this.renderedLines.isEmpty()) {
         return null;
      } else {
         int x = Mth.floor(mouseX - (double)this.getX());
         int y = Mth.floor(mouseY - (double)this.getY());
         if (x >= 0 && y >= 0 && x <= this.getWidth() && y <= this.getHeight()) {
            int hoveredLine = y / 9;
            if (hoveredLine >= this.renderedLines.size()) {
               return null;
            } else {
               FormattedCharSequence line = this.renderedLines.get(hoveredLine);
               int lineStart = this.alignment.align(this.getWidth(), this.font.width(line));
               return x < lineStart ? null : this.font.getSplitter().componentStyleAtWidth(line, x - lineStart);
            }
         } else {
            return null;
         }
      }
   }
}
