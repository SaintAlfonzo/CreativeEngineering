package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * The 1.20 GUI render types used with {@code GuiGraphics.fill/fillGradient(RenderType, ...)}. 1.19.2 draws GUI fills
 * immediately, so each type is just the render state to set around the fill.
 */
public enum GuiRenderTypes {
    /** {@code RenderType.gui()}: plain translucent fill. */
    GUI,
    /** {@code RenderType.guiOverlay()}: fill drawn over items (slot highlight), depth test off. */
    GUI_OVERLAY,
    /** {@code RenderType.guiTextHighlight()}: inverts what's under it (text selection), like 1.19.2's EditBox. */
    GUI_TEXT_HIGHLIGHT,
    /** {@code RenderType.guiGhostRecipeOverlay()} */
    GUI_GHOST_RECIPE_OVERLAY;

    public static GuiRenderTypes gui() {
        return GUI;
    }

    public static GuiRenderTypes guiOverlay() {
        return GUI_OVERLAY;
    }

    public static GuiRenderTypes guiTextHighlight() {
        return GUI_TEXT_HIGHLIGHT;
    }

    public static GuiRenderTypes guiGhostRecipeOverlay() {
        return GUI_GHOST_RECIPE_OVERLAY;
    }

    void setup() {
        switch (this) {
            case GUI_OVERLAY, GUI_GHOST_RECIPE_OVERLAY -> RenderSystem.disableDepthTest();
            case GUI_TEXT_HIGHLIGHT -> {
                RenderSystem.disableTexture();
                RenderSystem.enableColorLogicOp();
                RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
            }
            default -> {
            }
        }
    }

    void clear() {
        switch (this) {
            case GUI_OVERLAY, GUI_GHOST_RECIPE_OVERLAY -> RenderSystem.enableDepthTest();
            case GUI_TEXT_HIGHLIGHT -> {
                RenderSystem.disableColorLogicOp();
                RenderSystem.enableTexture();
            }
            default -> {
            }
        }
    }
}
