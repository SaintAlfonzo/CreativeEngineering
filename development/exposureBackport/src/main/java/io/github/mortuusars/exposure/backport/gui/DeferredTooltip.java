package io.github.mortuusars.exposure.backport.gui;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * 1.19.4+ {@code Screen.setTooltipForNextRenderPass}: a hovered widget queues its tooltip, the screen draws it after
 * everything else. Drawn by {@link BackportScreen} / {@link BackportContainerScreen} at the end of render.
 */
public final class DeferredTooltip {
    @Nullable
    private static Tooltip pending;

    private DeferredTooltip() {
    }

    public static void set(Tooltip tooltip) {
        pending = tooltip;
    }

    public static void renderAndClear(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Tooltip tooltip = pending;
        pending = null;
        if (tooltip != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 400);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip.toCharSequence(Minecraft.getInstance()), mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
    }

    public static void clear() {
        pending = null;
    }
}
