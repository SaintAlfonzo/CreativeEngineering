package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import io.github.mortuusars.exposure.client.util.Minecrft;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import io.github.mortuusars.exposure.backport.gui.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.math.Matrix4f;

public class Slider extends AbstractWidget {
   public static final ResourceLocation SLIDER_SPRITE = new ResourceLocation("widget/slider");
   public static final ResourceLocation HIGHLIGHTED_SPRITE = new ResourceLocation("widget/slider_highlighted");
   public static final ResourceLocation SLIDER_HANDLE_SPRITE = new ResourceLocation("widget/slider_handle");
   public static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/slider_handle_highlighted");
   protected static final int TEXT_MARGIN = 2;
   protected static final int HANDLE_WIDTH = 8;
   protected static final int HANDLE_HALF_WIDTH = 4;
   protected double position;
   protected boolean canChangeValue;
   protected final double defaultValue;
   protected final double min;
   protected final double max;
   protected final DecimalFormat displayFormat;
   protected final String name;
   protected final Consumer<Double> onChanged;
   @Nullable
   protected Pair<Integer, Integer> horizontalGradient;

   public Slider(
      int x, int y, int width, int height, double defaultValue, double min, double max, int displayedPrecision, String name, Consumer<Double> onChanged
   ) {
      this(x, y, width, height, CommonComponents.EMPTY, defaultValue, min, max, displayedPrecision, name, onChanged);
   }

   public Slider(
      int x,
      int y,
      int width,
      int height,
      Component message,
      double defaultValue,
      double min,
      double max,
      int displayedPrecision,
      String name,
      Consumer<Double> onChanged
   ) {
      super(x, y, width, height, message);
      this.position = (defaultValue - min) / (max - min);
      this.defaultValue = Mth.clamp(defaultValue, min, max);
      this.min = min;
      this.max = max;
      String zeros = "#".repeat(displayedPrecision);
      this.displayFormat = new DecimalFormat("#" + (zeros.isEmpty() ? "" : "." + zeros));
      this.name = name;
      this.onChanged = onChanged;
      this.updateMessage();
   }

   public Slider setHorizontalGradient(int leftColor, int rightColor) {
      this.horizontalGradient = Pair.of(leftColor, rightColor);
      return this;
   }

   public void removeHorizontalGradient() {
      this.horizontalGradient = null;
   }

   protected ResourceLocation getSprite() {
      return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
   }

   protected ResourceLocation getHandleSprite() {
      return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
   }

   public double getPosition() {
      return this.position;
   }

   public void setPosition(double position) {
      double d = this.position;
      this.position = Mth.clamp(position, 0.0, 1.0);
      if (d != this.position) {
         this.applyValue();
      }

      this.updateMessage();
   }

   public Double getValue() {
      return Mth.clampedLerp(this.min, this.max, this.position);
   }

   public void setValue(double value) {
      double positionFromValue = (value - this.min) / (this.max - this.min);
      this.setPosition(positionFromValue);
   }

   public void resetToDefault() {
      this.setValue(this.defaultValue);
   }

   protected void updateMessage() {
      this.setMessage(Component.literal(this.name + ": " + this.displayFormat.format(this.getValue())));
   }

   protected void applyValue() {
      this.onChanged.accept(this.getValue());
   }

   public void onClick(double mouseX, double mouseY) {
      this.setPositionFromMouse(mouseX);
   }

   public void setFocused(boolean focused) {
      super.setFocused(focused);
      if (!focused) {
         this.canChangeValue = false;
      } else {
         // 1.19.2 doesn't track the last input type (1.19.4+): treat focus as mouse/tab focus
         this.canChangeValue = true;
      }
   }

   protected void setPositionFromMouse(double mouseX) {
      this.setPosition((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
   }

   public void onRelease(double mouseX, double mouseY) {
      super.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 1 && this.active && this.visible && this.clicked(mouseX, mouseY)) {
         this.resetToDefault();
         this.playDownSound(Minecrft.get().getSoundManager());
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
      this.setPositionFromMouse(mouseX);
      super.onDrag(mouseX, mouseY, dragX, dragY);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (CommonInputs.selected(keyCode)) {
         this.canChangeValue = !this.canChangeValue;
         return true;
      } else {
         if (this.canChangeValue) {
            boolean bl = keyCode == 263;
            if (bl || keyCode == 262) {
               float f = bl ? -1.0F : 1.0F;
               this.setPosition(this.position + (double)(f / (float)(this.width - 8)));
               return true;
            }
         }

         return false;
      }
   }

   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      guiGraphics.blitSprite(this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
      if (this.active && this.horizontalGradient != null) {
         this.fillHorizontalGradient(
            guiGraphics,
            this.getX() + 1,
            this.getY() + 1,
            this.getX() + this.getWidth() - 1,
            this.getY() + this.getHeight() - 1,
            (Integer)this.horizontalGradient.getFirst(),
            (Integer)this.horizontalGradient.getSecond()
         );
      }

      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 50.0F);
      guiGraphics.blitSprite(this.getHandleSprite(), this.getX() + (int)(this.position * (double)(this.width - 8)), this.getY(), 8, this.getHeight());
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int textColor = (this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24;
      this.renderScrollingString(guiGraphics, minecraft.font, 2, textColor);
      guiGraphics.pose().popPose();
   }

   private void fillHorizontalGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
      // 1.20 builds these vertices in RenderType.gui(); 1.19.2 draws GUI fills immediately
      guiGraphics.fillHorizontalGradient(x1, y1, x2, y2, colorFrom, colorTo);
   }

   @NotNull
   protected MutableComponent createNarrationMessage() {
      return Component.translatable("gui.narrate.slider", new Object[]{this.getMessage()});
   }

   public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
         } else {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
         }
      }
   }
}
