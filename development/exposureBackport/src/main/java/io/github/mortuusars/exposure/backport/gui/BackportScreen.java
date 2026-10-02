package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A 1.19.2 {@link Screen} with the 1.20.2+ rendering contract, for upstream screens ({@code extends Screen} becomes
 * {@code extends BackportScreen}): {@code render(GuiGraphics, ...)} draws the background and then the renderables,
 * {@code renderBackground(GuiGraphics, ...)} is overridable, scrolling has the 4-argument signature, and widget
 * tooltips ({@link DeferredTooltip}) are drawn last.
 */
public abstract class BackportScreen extends Screen {
    protected BackportScreen(Component title) {
        super(title);
    }

    @Override
    public final void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.render(new GuiGraphics(poseStack), mouseX, mouseY, partialTick);
        DeferredTooltip.renderAndClear(new GuiGraphics(poseStack), mouseX, mouseY);
    }

    /** 1.20.2+: background, then every renderable. */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics.pose(), mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics.pose());
    }

    /** 1.20.2+: the dark gradient drawn over the level. */
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    public void renderBlurredBackground(float partialTick) {
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.mouseScrolled(mouseX, mouseY, 0.0, delta);
    }

    /** 1.19.4+ {@code setTooltipForNextRenderPass}. */
    public void setTooltipForNextRenderPass(Component tooltip) {
        DeferredTooltip.set(Tooltip.create(tooltip));
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.setFocused(null);
        this.init();
    }
}
