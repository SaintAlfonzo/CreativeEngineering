package io.github.mortuusars.exposure.backport.gui;

/**
 * Backport of 1.19.3+ {@code DefaultTooltipPositioner}: the vanilla placement next to the cursor, which is what
 * 1.19.2's tooltip rendering always does. {@link GuiGraphics#renderTooltip(net.minecraft.client.gui.Font, java.util.List,
 * DefaultTooltipPositioner, int, int)} takes it to keep upstream's calls.
 */
public final class DefaultTooltipPositioner {
    public static final DefaultTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

    private DefaultTooltipPositioner() {
    }
}
