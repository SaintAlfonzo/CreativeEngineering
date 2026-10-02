package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Backport of the 1.20.4+ {@code Checkbox} (builder, value change callback), drawn with 1.19.2's checkbox sheet. */
public class Checkbox extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int BOX_SIZE = 20;
    private boolean selected;
    private final OnValueChange onValueChange;
    private final Font font;

    Checkbox(int x, int y, Component message, Font font, boolean selected, OnValueChange onValueChange) {
        super(x, y, BOX_SIZE + 4 + font.width(message), BOX_SIZE, message);
        this.font = font;
        this.selected = selected;
        this.onValueChange = onValueChange;
    }

    public static Builder builder(Component message, Font font) {
        return new Builder(message, font);
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, this.getX(), this.getY(), this.isHoveredOrFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, BOX_SIZE, this.height, 64, 64);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.drawString(this.font, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, TEXT_COLOR | (int)(this.alpha * 255.0F) << 24);
    }

    public static class Builder {
        private final Component message;
        private final Font font;
        private int x = 0;
        private int y = 0;
        private OnValueChange onValueChange = OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component message, Font font) {
            this.message = message;
            this.font = font;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder onValueChange(OnValueChange onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public Builder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Checkbox build() {
            Checkbox checkbox = new Checkbox(this.x, this.y, this.message, this.font, this.selected, this.onValueChange);
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

    public interface OnValueChange {
        OnValueChange NOP = (checkbox, value) -> {
        };

        void onValueChange(Checkbox checkbox, boolean value);
    }
}
