package io.github.mortuusars.exposure.backport.gui;

/** Backport of 1.19.4+ {@code TooltipRenderUtil}: the vanilla tooltip box (1.19.2's Screen.renderTooltipInternal colors). */
public final class TooltipRenderUtil {
    public static final int MOUSE_OFFSET = 12;
    private static final int BACKGROUND_COLOR = 0xF0100010;
    private static final int BORDER_COLOR_TOP = 0x505000FF;
    private static final int BORDER_COLOR_BOTTOM = 0x5028007F;

    private TooltipRenderUtil() {
    }

    public static void renderTooltipBackground(GuiGraphics g, int x, int y, int width, int height, int z) {
        g.fillGradient(x - 3, y - 4, x + width + 3, y - 3, z, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fillGradient(x - 3, y + height + 3, x + width + 3, y + height + 4, z, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fillGradient(x - 3, y - 3, x + width + 3, y + height + 3, z, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fillGradient(x - 4, y - 3, x - 3, y + height + 3, z, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fillGradient(x + width + 3, y - 3, x + width + 4, y + height + 3, z, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fillGradient(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, z, BORDER_COLOR_TOP, BORDER_COLOR_BOTTOM);
        g.fillGradient(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, z, BORDER_COLOR_TOP, BORDER_COLOR_BOTTOM);
        g.fillGradient(x - 3, y - 3, x + width + 3, y - 3 + 1, z, BORDER_COLOR_TOP, BORDER_COLOR_TOP);
        g.fillGradient(x - 3, y + height + 2, x + width + 3, y + height + 3, z, BORDER_COLOR_BOTTOM, BORDER_COLOR_BOTTOM);
    }
}
