package io.github.mortuusars.exposure.backport.gui;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Backport of 1.19.4+ {@code MultiLineTextWidget}: wrapped, optionally centered, non-interactive text. */
public class MultiLineTextWidget extends AbstractWidget {
    private final Font font;
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private boolean centered = false;
    private int color = 0xFFFFFF;

    public MultiLineTextWidget(Component message, Font font) {
        this(0, 0, message, font);
    }

    public MultiLineTextWidget(int x, int y, Component message, Font font) {
        super(x, y, 0, 0, message);
        this.font = font;
        this.active = false;
        this.updateSize();
    }

    public MultiLineTextWidget setColor(int color) {
        this.color = color;
        return this;
    }

    public MultiLineTextWidget setMaxWidth(int maxWidth) {
        this.maxWidth = OptionalInt.of(maxWidth);
        this.updateSize();
        return this;
    }

    public MultiLineTextWidget setMaxRows(int maxRows) {
        this.maxRows = OptionalInt.of(maxRows);
        this.updateSize();
        return this;
    }

    public MultiLineTextWidget setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    public MultiLineTextWidget alignLeft() {
        return this.setCentered(false);
    }

    public MultiLineTextWidget alignCenter() {
        return this.setCentered(true);
    }

    private List<FormattedCharSequence> lines() {
        List<FormattedCharSequence> lines = this.font.split(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE));
        return this.maxRows.isPresent() && lines.size() > this.maxRows.getAsInt() ? lines.subList(0, this.maxRows.getAsInt()) : lines;
    }

    private void updateSize() {
        List<FormattedCharSequence> lines = this.lines();
        int w = 0;
        for (FormattedCharSequence line : lines) {
            w = Math.max(w, this.font.width(line));
        }
        this.width = w;
        this.height = lines.size() * 9;
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        if (this.font != null) {
            this.updateSize();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int y = this.getY();
        for (FormattedCharSequence line : this.lines()) {
            int x = this.centered ? this.getX() + (this.width - this.font.width(line)) / 2 : this.getX();
            guiGraphics.drawString(this.font, line, x, y, this.color);
            y += 9;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
