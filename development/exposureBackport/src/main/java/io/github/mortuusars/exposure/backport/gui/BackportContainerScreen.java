package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

/**
 * A 1.19.2 {@link AbstractContainerScreen} with the 1.20.2+ contract, for upstream container screens:
 * {@code renderBg/renderLabels/renderTooltip(GuiGraphics, ...)}; {@code renderBackground(GuiGraphics, ...)} draws
 * the dark background and then {@code renderBg} (1.19.2 draws renderBg inside render); 4-argument scrolling; widget
 * tooltips drawn last.
 */
public abstract class BackportContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    public BackportContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public final void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.render(new GuiGraphics(poseStack), mouseX, mouseY, partialTick);
        DeferredTooltip.renderAndClear(new GuiGraphics(poseStack), mouseX, mouseY);
    }

    /** 1.20.2+: background (with renderBg), renderables, slots, labels, carried item. */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics.pose(), mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics.pose());
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    protected abstract void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY);

    /** 1.19.2 calls this from render; the background is drawn by {@link #renderBackground} instead. */
    @Override
    protected final void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    protected final void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        this.renderLabels(new GuiGraphics(poseStack), mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics.pose(), x, y);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.mouseScrolled(mouseX, mouseY, 0.0, delta);
    }

    public void setTooltipForNextRenderPass(Component tooltip) {
        DeferredTooltip.set(Tooltip.create(tooltip));
    }
}
