// Port glue (ours, GPLv3): 1.21's StringWidget (one line, shadowed, aligned in its box) and Olympus 1.0.18's
// MultilineTextWidget (MIT, Terrarium; wrapped, unshadowed, left aligned) for Forge 1.19.2, which has neither.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class Label extends KitWidget {
    private final Font font;
    private final Component text;
    private int color = 0xFFFFFF;
    private float alignX = 0.5f;
    private boolean shadow = true;
    private List<FormattedCharSequence> lines;

    /** StringWidget(text, font): as wide as the text, 9 high. */
    public Label(Component text, Font font) {
        this(0, 0, font.width(text.getVisualOrderText()), 9, text, font);
    }

    /** StringWidget(x, y, width, height, text, font): centred until {@link #alignLeft()}. */
    public Label(int x, int y, int width, int height, Component text, Font font) {
        super(width, height);
        this.x = x;
        this.y = y;
        this.text = text;
        this.font = font;
        this.active = false;
    }

    /** Olympus MultilineTextWidget(maxWidth, text, font): wrapped at maxWidth, no shadow. */
    public static Label multiline(int maxWidth, Component text) {
        Font font = Minecraft.getInstance().font;
        Label label = new Label(0, 0, maxWidth, 9, text, font);
        label.lines = font.split(text, maxWidth);
        int width = 0;
        for (FormattedCharSequence line : label.lines) width = Math.max(width, font.width(line));
        label.setSize(width, label.lines.size() * 9);
        label.shadow = false;
        return label;
    }

    public Label setColor(int color) {
        this.color = color;
        return this;
    }

    public Label alignLeft() {
        this.alignX = 0f;
        return this;
    }

    public Label alignRight() {
        this.alignX = 1f;
        return this;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (lines != null) {
            int y = this.y;
            for (FormattedCharSequence line : lines) {
                Gfx.drawString(pose, font, line, x + Math.round((width - font.width(line)) * alignX), y, color, shadow);
                y += 9;
            }
            return;
        }
        int textWidth = font.width(text.getVisualOrderText());
        int textX = x + Math.round(alignX * (width - textWidth));
        int textY = y + (height - 9 + 1) / 2;
        Gfx.drawString(pose, font, text, textX, textY, color, shadow);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
