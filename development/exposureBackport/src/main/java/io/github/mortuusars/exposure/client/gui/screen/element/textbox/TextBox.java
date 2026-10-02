package io.github.mortuusars.exposure.client.gui.screen.element.textbox;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.util.Pos2i;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.font.TextFieldHelper.CursorStep;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.GuiRenderTypes;

public class TextBox extends AbstractWidget {
   public final Font font;
   public Supplier<String> textGetter;
   public Consumer<String> textSetter;
   public Predicate<String> textValidator = text -> text != null
         && this.getFont().wordWrapHeight(text, this.width) + (text.endsWith("\n") ? 9 : 0) <= this.height;
   public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
   public int fontColor = -16777216;
   public int fontUnfocusedColor = -16777216;
   public int selectionColor = -16776961;
   public int selectionUnfocusedColor = -2013265665;
   public final TextFieldHelper textFieldHelper;
   protected DisplayCache displayCache = new DisplayCache();
   protected int frameTick;
   protected long lastClickTime;
   protected int lastIndex = -1;

   public TextBox(@NotNull Font font, int x, int y, int width, int height, Supplier<String> textGetter, Consumer<String> textSetter) {
      super(x, y, width, height, Component.empty());
      this.font = font;
      this.textGetter = textGetter;
      this.textSetter = textSetter;
      this.textFieldHelper = new TextFieldHelper(
         this::getText,
         this::setText,
         TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
         TextFieldHelper.createClipboardSetter(Minecraft.getInstance()),
         this::validateText
      );
   }

   public void tick() {
      this.frameTick++;
   }

   public Font getFont() {
      return this.font;
   }

   @NotNull
   public String getText() {
      return this.textGetter.get();
   }

   public TextBox setText(@NotNull String text) {
      this.textSetter.accept(text);
      this.clearDisplayCache();
      return this;
   }

   protected boolean validateText(String text) {
      return this.textValidator.test(text);
   }

   public void setHeight(int height) {
      this.height = height;
      this.clearDisplayCache();
   }

   public int getCurrentFontColor() {
      return this.isFocused() ? this.fontColor : this.fontUnfocusedColor;
   }

   public TextBox setFontColor(int fontColor, int fontUnfocusedColor) {
      this.fontColor = fontColor;
      this.fontUnfocusedColor = fontUnfocusedColor;
      this.clearDisplayCache();
      return this;
   }

   public TextBox setFontColor(int fontColor) {
      this.fontColor = fontColor;
      this.fontUnfocusedColor = fontColor;
      this.clearDisplayCache();
      return this;
   }

   public TextBox setSelectionColor(int selectionColor, int selectionUnfocusedColor) {
      this.selectionColor = selectionColor;
      this.selectionUnfocusedColor = selectionUnfocusedColor;
      this.clearDisplayCache();
      return this;
   }

   public void setCursorToEnd() {
      this.textFieldHelper.setCursorToEnd();
      this.clearDisplayCache();
   }

   public void refresh() {
      this.clearDisplayCache();
   }

   protected DisplayCache getDisplayCache() {
      if (this.displayCache.needsRebuilding) {
         this.displayCache
            .rebuild(
               this.font,
               this.getText(),
               this.textFieldHelper.getCursorPos(),
               this.textFieldHelper.getSelectionPos(),
               this.getX(),
               this.getY(),
               this.getWidth(),
               this.getHeight(),
               this.horizontalAlignment
            );
      }

      return this.displayCache;
   }

   protected void clearDisplayCache() {
      this.displayCache.needsRebuilding = true;
   }

   protected Pos2i convertLocalToScreen(Pos2i pos) {
      return new Pos2i(this.getX() + pos.x, this.getY() + pos.y);
   }

   protected Pos2i convertScreenToLocal(Pos2i screenPos) {
      return new Pos2i(screenPos.x - this.getX(), screenPos.y - this.getY());
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      DisplayCache displayCache = this.getDisplayCache();

      for (DisplayCache.LineInfo lineInfo : displayCache.lines) {
         guiGraphics.drawString(this.font, lineInfo.asComponent, this.getX() + lineInfo.x, this.getY() + lineInfo.y, this.getCurrentFontColor(), false);
      }

      this.renderHighlight(guiGraphics, displayCache.selectionAreas);
      if (this.isFocused()) {
         this.renderCursor(guiGraphics, displayCache.cursorPos, displayCache.cursorAtEnd);
      }
   }

   protected void renderHighlight(GuiGraphics guiGraphics, Rect2i[] highlightAreas) {
      for (Rect2i selection : highlightAreas) {
         int x = this.getX() + selection.getX();
         int y = this.getY() + selection.getY();
         int x1 = x + selection.getWidth();
         int y1 = y + selection.getHeight();
         guiGraphics.fill(GuiRenderTypes.guiTextHighlight(), x, y - 1, x1, y1, this.isFocused() ? this.selectionColor : this.selectionUnfocusedColor);
      }
   }

   protected void renderCursor(GuiGraphics guiGraphics, Pos2i cursorPos, boolean isEndOfText) {
      if (this.frameTick / 6 % 2 == 0) {
         cursorPos = this.convertLocalToScreen(cursorPos);
         if (isEndOfText) {
            guiGraphics.drawString(this.font, "_", cursorPos.x, cursorPos.y, this.getCurrentFontColor(), false);
         } else {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
            RenderSystem.disableBlend();
            guiGraphics.fill(cursorPos.x, cursorPos.y - 1, cursorPos.x + 1, cursorPos.y + 9, this.getCurrentFontColor());
            guiGraphics.pose().popPose();
         }
      }
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
   }

   @NotNull
   public Component getMessage() {
      return Component.literal(this.getText());
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.isFocused()) {
         return false;
      } else {
         boolean handled = this.handleKeyPressed(keyCode, scanCode, modifiers);
         if (handled) {
            this.clearDisplayCache();
         }

         return handled;
      }
   }

   protected boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
      CursorStep cursorStep = Screen.hasControlDown() ? CursorStep.WORD : CursorStep.CHARACTER;
      if (keyCode == 265) {
         this.changeLine(-1);
         return true;
      } else if (keyCode == 264) {
         this.changeLine(1);
         return true;
      } else if (keyCode == 268) {
         this.keyHome();
         return true;
      } else if (keyCode == 269) {
         this.keyEnd();
         return true;
      } else if (keyCode == 259) {
         this.textFieldHelper.removeFromCursor(-1, cursorStep);
         return true;
      } else if (keyCode == 261) {
         this.textFieldHelper.removeFromCursor(1, cursorStep);
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         return this.textFieldHelper.keyPressed(keyCode);
      } else {
         this.textFieldHelper.insertText(CommonComponents.NEW_LINE.getString());
         return true;
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      if (!this.isFocused()) {
         return false;
      } else {
         boolean typed = this.textFieldHelper.charTyped(codePoint);
         if (typed) {
            this.clearDisplayCache();
         }

         return typed;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered && this.visible && this.isActive() && button == 0) {
         long currentTime = Util.getMillis();
         DisplayCache displayCache = this.getDisplayCache();
         int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
         if (index >= 0) {
            if (index != this.lastIndex || currentTime - this.lastClickTime >= 250L) {
               this.textFieldHelper.setCursorPos(index, Screen.hasShiftDown());
            } else if (!this.textFieldHelper.isSelecting()) {
               this.selectWord(index);
            } else {
               this.textFieldHelper.selectAll();
            }

            this.clearDisplayCache();
         }

         this.lastIndex = index;
         this.lastClickTime = currentTime;
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (button == 0) {
         DisplayCache displayCache = this.getDisplayCache();
         int index = displayCache.getIndexAtPosition(this.font, this.convertScreenToLocal(new Pos2i((int)mouseX, (int)mouseY)));
         this.textFieldHelper.setCursorPos(index, true);
         this.clearDisplayCache();
      }

      return true;
   }

   protected void selectWord(int index) {
      String string = this.getText();
      this.textFieldHelper.setSelectionRange(StringSplitter.getWordPosition(string, -1, index, false), StringSplitter.getWordPosition(string, 1, index, false));
   }

   protected void changeLine(int yChange) {
      int cursorPos = this.textFieldHelper.getCursorPos();
      int line = this.getDisplayCache().changeLine(cursorPos, yChange);
      this.textFieldHelper.setCursorPos(line, Screen.hasShiftDown());
   }

   protected void keyHome() {
      if (Screen.hasControlDown()) {
         this.textFieldHelper.setCursorToStart(Screen.hasShiftDown());
      } else {
         int cursorIndex = this.textFieldHelper.getCursorPos();
         int lineStartIndex = this.getDisplayCache().findLineStart(cursorIndex);
         this.textFieldHelper.setCursorPos(lineStartIndex, Screen.hasShiftDown());
      }
   }

   protected void keyEnd() {
      if (Screen.hasControlDown()) {
         this.textFieldHelper.setCursorToEnd(Screen.hasShiftDown());
      } else {
         DisplayCache displayCache = this.getDisplayCache();
         int cursorIndex = this.textFieldHelper.getCursorPos();
         int lineEndIndex = displayCache.findLineEnd(cursorIndex);
         this.textFieldHelper.setCursorPos(lineEndIndex, Screen.hasShiftDown());
      }
   }
}
