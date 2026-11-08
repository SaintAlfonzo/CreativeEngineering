// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), Widgets.textarea / MultilineTextWidget as the Tempad guide uses it: text
// wrapped to the widget width, optional scale (wrapped at width / scale, drawn scaled), block and line alignment,
// colour, no shadow, and a click callback that receives the Style under the cursor (clickActionCallback). A width
// change rewraps (the guide's width callbacks size every paragraph to its panel).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TextArea extends KitWidget {
    private final Font font = Minecraft.getInstance().font;
    private final Component text;
    private int color = Gfx.WHITE;
    private float scale = 1f;
    private float lineAlign;
    private List<FormattedCharSequence> lines = List.of();
    private int wrappedAt = -1;
    @Nullable
    private Predicate<Style> clickCallback;

    /** Widgets.textarea(text, maxWidth). */
    public TextArea(Component text, int maxWidth) {
        super(maxWidth, 9);
        this.text = text;
        this.active = false;
        rewrap();
    }

    public TextArea setColor(int color) {
        this.color = color;
        return this;
    }

    public TextArea scale(float scale) {
        this.scale = scale;
        rewrap();
        return this;
    }

    public TextArea alignLeft() {
        this.lineAlign = 0f;
        return this;
    }

    /** alignCenter + textAlignCenter: every line centred in the widget. */
    public TextArea alignCenter() {
        this.lineAlign = 0.5f;
        return this;
    }

    public TextArea clickActionCallback(Predicate<Style> callback) {
        this.clickCallback = callback;
        this.active = true;
        return this;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        rewrap();
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        rewrap();
    }

    private void rewrap() {
        int wrapWidth = Math.max(1, (int) (width / scale));
        if (wrapWidth == wrappedAt) return;
        wrappedAt = wrapWidth;
        lines = font.split(text, wrapWidth);
        this.height = Mth.ceil(lines.size() * 9 * scale);
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1f);
        int lineWidth = (int) (width / scale);
        int lineY = 0;
        for (FormattedCharSequence line : lines) {
            Gfx.drawString(pose, font, line, Math.round((lineWidth - font.width(line)) * lineAlign), lineY, color, false);
            lineY += 9;
        }
        pose.popPose();
    }

    @Nullable
    private Style styleAt(double mouseX, double mouseY) {
        double localX = (mouseX - x) / scale;
        int lineIndex = (int) ((mouseY - y) / scale / 9);
        if (lineIndex < 0 || lineIndex >= lines.size() || localX < 0) return null;
        FormattedCharSequence line = lines.get(lineIndex);
        int lineX = Math.round(((int) (width / scale) - font.width(line)) * lineAlign);
        return font.getSplitter().componentStyleAtWidth(line, (int) (localX - lineX));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickCallback == null || button != 0 || !visible || !isMouseOver(mouseX, mouseY)) return false;
        Style style = styleAt(mouseX, mouseY);
        return style != null && clickCallback.test(style);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
