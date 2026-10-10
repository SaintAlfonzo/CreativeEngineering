package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The 1.20+ {@code AbstractWidget} surface over 1.19.2's: {@code renderWidget(GuiGraphics, ...)} instead of
 * {@code renderButton(PoseStack, ...)}, {@code getX()/setX()}, {@code setTooltip}, {@code updateWidgetNarration} and
 * the 4-argument {@code mouseScrolled}. Upstream widgets extend this under the vanilla name (the port tool maps the
 * import), so their code stays as written.
 * <p>
 * Tooltips are queued in {@link DeferredTooltip} and drawn after the screen's other renderables (what 1.19.4+'s
 * {@code setTooltipForNextRenderPass} does); {@link BackportScreen}/{@link BackportContainerScreen} draw the queue.
 */
public abstract class AbstractWidget extends net.minecraft.client.gui.components.AbstractWidget {
    @Nullable
    private Tooltip tooltip;

    public AbstractWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    // ---- 1.20 rendering ----

    protected abstract void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        render(new GuiGraphics(poseStack), mouseX, mouseY, partialTick);
    }

    /** 1.20 {@code AbstractWidget.render(GuiGraphics, ...)}: hover state, renderWidget, tooltip. */
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        this.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if (this.tooltip != null && this.isHovered()) {
            DeferredTooltip.set(this.tooltip);
        }
    }

    @Override
    public final void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderWidget(new GuiGraphics(poseStack), mouseX, mouseY, partialTick);
    }

    /** 1.20 helper: message centered in the widget, scrolled when it doesn't fit (here: clipped). */
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
        this.renderScrollingString(guiGraphics, font, 2, color);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
        int minX = this.getX() + width;
        int maxX = this.getX() + this.getWidth() - width;
        renderScrollingString(guiGraphics, font, this.getMessage(), minX, this.getY(), maxX, this.getY() + this.getHeight(), color);
    }

    protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color) {
        int textWidth = font.width(text);
        int y = (minY + maxY - 9) / 2 + 1;
        int available = maxX - minX;
        if (textWidth > available) {
            guiGraphics.enableScissor(minX, minY, maxX, maxY);
            guiGraphics.drawString(font, text, minX, y, color);
            guiGraphics.disableScissor();
        } else {
            guiGraphics.drawCenteredString(font, text, (minX + maxX) / 2, y, color);
        }
    }

    // ---- 1.19.3+ position accessors ----

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getRight() {
        return this.getX() + this.getWidth();
    }

    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isActive() {
        return this.active && this.visible;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    /**
     * 1.20 {@code ContainerEventHandler.setFocused}: moving a screen's focus also sets the widgets' own focused
     * flags. 1.19.2's only stores the field, so {@link TextBox} (which gates input on {@link #isFocused()}) never
     * received keys: the album note and album title boxes could not be typed in. Only text boxes are flagged, since
     * buttons and photo slots draw highlighted while focused and would stay lit after a click. Called by the backport
     * screens before storing the field.
     */
    public static void moveFocus(@Nullable GuiEventListener from, @Nullable GuiEventListener to) {
        if (from != to && from instanceof TextBox box) {
            box.setFocused(false);
        }
        if (to instanceof TextBox box) {
            box.setFocused(true);
        }
    }

    // ---- tooltip ----

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Nullable
    public Tooltip getTooltip() {
        return this.tooltip;
    }

    // ---- narration (1.20 name) ----

    protected abstract void updateWidgetNarration(NarrationElementOutput narrationElementOutput);

    @Override
    public final void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.updateWidgetNarration(narrationElementOutput);
    }

    // ---- input (1.20.2+ scroll signature) ----

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return this.mouseScrolled(mouseX, mouseY, 0.0, delta);
    }

    protected Minecraft minecraft() {
        return Minecraft.getInstance();
    }
}
