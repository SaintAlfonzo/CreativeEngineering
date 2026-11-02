package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A tooltip taller than the screen keeps its top (the name and first lines) on screen and runs off the bottom
 * (ui/tooltips #5, feature_writeup/tooltips.md 3).
 *
 * <p>Forge 43.3.9 {@code Screen.renderTooltipInternal} ({@code m_169383_}) places the box at the mouse, flips it left
 * when it overflows the right edge, then {@code if (y + height + 6 > screenHeight) y = screenHeight - height - 6}: a
 * box taller than the screen gets a negative {@code y} and loses its top. The clamped {@code y} is local slot 9 (int
 * locals in scope before {@code pushPose}: x 3, y 4, width 6, height 7, tooltipX 8, tooltipY 9; checked with javap on
 * the Forge client jar). It is raised to at least 4 just before {@code pushPose}, so the frame (drawn from {@code y - 4})
 * stays on screen. Legendary Tooltips and Iceberg draw their frames from this same position, so they follow.
 * A tooltip that fits only moves when the mouse is within 16 px of the top (vanilla let its frame poke off the top
 * there; now it starts at y 4). Read live from
 * {@code [tooltips] keepTopOnScreen}.
 */
@Mixin(Screen.class)
public class ScreenTooltipTopMixin {

    @ModifyVariable(method = "m_169383_", remap = false, require = 1, index = 9,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85836_()V", ordinal = 0, remap = false))
    private int creativeengineering$keepTopOnScreen(int y) {
        return CEConfig.TOOLTIPS.keepTopOnScreen.get() ? Math.max(y, 4) : y;
    }
}
