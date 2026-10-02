package io.github.mortuusars.exposure.client.gui.component;

import com.google.common.base.Preconditions;
import io.github.mortuusars.exposure.client.gui.Tooltips;
import io.github.mortuusars.exposure.client.gui.Widgets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CycleButton<T> extends Button {
   protected final List<T> values;
   protected final T startingValue;
   protected final Map<T, WidgetSprites> spritesMap;
   protected CycleButton.OnCycle<T> onCycle;
   protected boolean loop = true;
   @Nullable
   protected Tooltip defaultTooltip;
   protected Map<T, Tooltip> tooltips = Collections.emptyMap();
   @Nullable
   protected SoundEvent clickSound;
   protected int currentIndex;

   public CycleButton(
      int x, int y, int width, int height, List<T> values, @NotNull T startingValue, Map<T, WidgetSprites> spritesMap, CycleButton.OnCycle<T> onCycle
   ) {
      super(x, y, width, height, CommonComponents.EMPTY, b -> {
      }, DEFAULT_NARRATION);
      Preconditions.checkArgument(!values.isEmpty(), "Cannot create a CycleButton with 0 possible values.");
      this.values = values;
      this.startingValue = startingValue;
      Preconditions.checkArgument(!spritesMap.isEmpty(), "Cannot create a CycleButton with 0 sprites.");
      this.spritesMap = spritesMap;
      this.onCycle = onCycle;
      this.setCurrentIndex(Math.max(values.indexOf(startingValue), 0));
   }

   public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue, Map<T, WidgetSprites> spritesMap) {
      this(x, y, width, height, values, startingValue, spritesMap, (b, v) -> {
      });
   }

   public CycleButton(
      int x, int y, int width, int height, List<T> values, @NotNull T startingValue, Function<T, WidgetSprites> spritesFunc, CycleButton.OnCycle<T> onCycle
   ) {
      this(x, y, width, height, values, startingValue, Widgets.createMap(values, spritesFunc), onCycle);
   }

   public CycleButton(int x, int y, int width, int height, List<T> values, @NotNull T startingValue, Function<T, WidgetSprites> spritesFunc) {
      this(x, y, width, height, values, startingValue, Widgets.createMap(values, spritesFunc), (b, v) -> {
      });
   }

   public CycleButton<T> setLooping(boolean loop) {
      this.loop = loop;
      return this;
   }

   public CycleButton<T> setDefaultTooltip(Tooltip tooltip) {
      this.defaultTooltip = tooltip;
      this.updateVisuals();
      return this;
   }

   public CycleButton<T> setTooltips(Map<T, Tooltip> tooltips) {
      this.tooltips = tooltips;
      this.updateVisuals();
      return this;
   }

   public CycleButton<T> setTooltips(Function<T, Component> tooltipFunc) {
      this.tooltips = Tooltips.createMap(this.values, tooltipFunc);
      this.updateVisuals();
      return this;
   }

   public CycleButton<T> setClickSound(SoundEvent soundEvent) {
      this.clickSound = soundEvent;
      return this;
   }

   public CycleButton<T> onCycle(CycleButton.OnCycle<T> onCycle) {
      this.onCycle = onCycle;
      return this;
   }

   public CycleButton<T> onCycle(Consumer<T> onCycle) {
      this.onCycle = (button, newValue) -> onCycle.accept(newValue);
      return this;
   }

   public T getCurrentValue() {
      return this.values.get(Mth.clamp(this.currentIndex, 0, this.values.size() - 1));
   }

   public void setCurrentValue(T value) {
      this.setCurrentIndex(Mth.clamp(this.values.indexOf(value), 0, this.values.size() - 1));
   }

   public void setCurrentIndex(int index) {
      this.currentIndex = index;
      this.updateVisuals();
   }

   public void cycle(boolean reverse) {
      int value = this.currentIndex;
      value += reverse ? -1 : 1;
      if (value < 0) {
         value = this.loop ? this.values.size() - 1 : 0;
      } else if (value >= this.values.size()) {
         value = this.loop ? 0 : this.values.size() - 1;
      }

      if (this.currentIndex != value) {
         this.currentIndex = value;
         this.onCycle();
      }
   }

   protected void onCycle() {
      this.updateVisuals();
      this.onCycle.onCycle(this, this.getCurrentValue());
   }

   private void updateVisuals() {
      Tooltip tooltip = this.tooltips.getOrDefault(this.getCurrentValue(), this.defaultTooltip);
      if (tooltip != null) {
         this.setTooltip(tooltip);
      }
   }

   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      WidgetSprites sprites = this.spritesMap.get(this.getCurrentValue());
      ResourceLocation spriteLocation = sprites != null ? sprites.get(this.isActive(), this.isHoveredOrFocused()) : TextureManager.INTENTIONAL_MISSING_TEXTURE;
      guiGraphics.blitSprite(spriteLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight());
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if ((button == 0 || button == 1) && this.clicked(mouseX, mouseY)) {
         this.cycle(button == 1);
         this.playDownSound(Minecraft.getInstance().getSoundManager());
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      this.cycle(scrollY < 0.0);
      this.playDownSound(Minecraft.getInstance().getSoundManager());
      return true;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      boolean pressed = super.keyPressed(pKeyCode, pScanCode, pModifiers);
      if (pressed) {
         this.cycle(Screen.hasShiftDown());
      }

      return pressed;
   }

   public void playDownSound(SoundManager handler) {
      if (this.clickSound != null) {
         handler.play(SimpleSoundInstance.forUI(this.clickSound, 1.0F));
      }
   }

   public interface OnCycle<T> {
      void onCycle(CycleButton<T> var1, T var2);
   }
}
