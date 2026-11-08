// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.components.compound.LayoutWidget
// holding a ClearableGridLayout of one column (the only use in the Tempad screens): background, content margin,
// vertical scrolling with overscroll and a draggable scrollbar, content clipped to the view.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScrollList extends KitWidget {
    public interface ScrollbarRenderer {
        void render(PoseStack pose, ScrollList list, Ctx ctx, float partialTick);
    }

    /** TriState: TRUE always, FALSE never, null when the content is taller than the widget. */
    @Nullable
    private Boolean scrollableY = Boolean.FALSE;
    private final List<AbstractWidget> rows = new ArrayList<>();
    private int scrollWidth = 6;
    private int scrollMargin = 2;
    private int contentMargin;
    private int overscrollY;
    private int yScroll;
    private boolean fillWidth;
    private boolean draggingScrollbar;
    @Nullable
    private ResourceLocation background;
    @Nullable
    private ScrollbarRenderer scrollbarRenderer;
    @Nullable
    private AbstractWidget clicked;

    public ScrollList() {
        super(0, 0);
    }

    public ScrollList withContentFillWidth() {
        this.fillWidth = true;
        return this;
    }

    /** withScrollableY(TriState): null = UNDEFINED (only when needed). */
    public ScrollList withScrollableY(@Nullable Boolean scrollable) {
        this.scrollableY = scrollable;
        return this;
    }

    public ScrollList withScrollbarYRenderer(ScrollbarRenderer renderer) {
        this.scrollbarRenderer = renderer;
        return this;
    }

    public ScrollList withTexture(@Nullable ResourceLocation background) {
        this.background = background;
        return this;
    }

    public ScrollList withOverscrollY(int overscroll) {
        this.overscrollY = overscroll;
        return this;
    }

    public ScrollList withContentMargin(int margin) {
        this.contentMargin = margin;
        return this;
    }

    public void clear() {
        rows.clear();
        clicked = null;
    }

    public <W extends AbstractWidget> W add(W widget) {
        rows.add(widget);
        return widget;
    }

    public int layoutHeight() {
        int h = 0;
        for (AbstractWidget row : rows) h += row.getHeight();
        return h;
    }

    public int getContentHeight() {
        return layoutHeight() + overscrollY * 2;
    }

    public int getYScroll() {
        return yScroll;
    }

    public int getOverscrollY() {
        return overscrollY;
    }

    public boolean isYScrollbarVisible() {
        return Boolean.TRUE.equals(scrollableY) || (scrollableY == null && layoutHeight() > height);
    }

    public int getViewWidth() {
        return width - contentMargin * 2 - (isYScrollbarVisible() ? scrollWidth + scrollMargin * 2 : 0);
    }

    public int getViewHeight() {
        return height - contentMargin * 2;
    }

    public void withScrollY(int y) {
        this.yScroll = Mth.clamp(y, -overscrollY, Math.max(0, layoutHeight() + overscrollY - getViewHeight()));
    }

    private void arrange() {
        int y = this.y - yScroll + contentMargin;
        for (AbstractWidget row : rows) {
            if (fillWidth) row.setWidth(getViewWidth());
            row.x = this.x + contentMargin;
            row.y = y;
            y += row.getHeight();
        }
    }

    private boolean isOverContent(double mx, double my) {
        return mx >= x + contentMargin && mx <= x + contentMargin + getViewWidth() && my >= y + contentMargin && my <= y + contentMargin + getViewHeight();
    }

    private boolean isOverScrollbarY(double mx, double my) {
        return isYScrollbarVisible() && mx >= x + getViewWidth() + scrollMargin && mx <= x + width - scrollMargin
                && my >= y + scrollMargin && my <= y + getViewHeight() - scrollMargin;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        arrange();
        if (background != null) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            Sprites.blit(pose, background, x, y, getViewWidth() + contentMargin * 2, getViewHeight() + contentMargin * 2);
        }
        Gfx.enableScissor(x + contentMargin, y + contentMargin, x + getViewWidth() + contentMargin, y + getViewHeight() + contentMargin);
        for (AbstractWidget row : rows) row.render(pose, mouseX, mouseY, partialTick);
        Gfx.disableScissor();
        if (isYScrollbarVisible() && scrollbarRenderer != null) {
            Ctx ctx = new Ctx(this, mouseX, mouseY, x + getViewWidth() + scrollMargin + contentMargin * 2, y + scrollMargin, scrollWidth, getViewHeight() - scrollMargin * 2);
            scrollbarRenderer.render(pose, this, ctx, partialTick);
        }
    }

    @Nullable
    @Override
    public Component tooltipAt(double mouseX, double mouseY) {
        if (!visible || !isOverContent(mouseX, mouseY)) return null;
        for (AbstractWidget row : rows) {
            if (row instanceof TooltipSource source) {
                Component tooltip = source.tooltipAt(mouseX, mouseY);
                if (tooltip != null) return tooltip;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;
        if (isOverScrollbarY(mouseX, mouseY)) {
            draggingScrollbar = true;
            return true;
        }
        if (!isOverContent(mouseX, mouseY)) return false;
        arrange();
        for (AbstractWidget row : List.copyOf(rows)) {
            if (row.mouseClicked(mouseX, mouseY, button)) {
                clicked = row;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        AbstractWidget w = clicked;
        clicked = null;
        return w != null && w.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            int actualHeight = getViewHeight();
            double barHeight = (double) actualHeight / (layoutHeight() + overscrollY * 2) * actualHeight;
            double barDrag = dragY / (actualHeight - barHeight);
            withScrollY(yScroll + (int) (barDrag * (layoutHeight() + overscrollY * 2)));
            return true;
        }
        return clicked != null && clicked.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean isDraggingScrollbar() {
        return draggingScrollbar;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseOver(mouseX, mouseY) && isYScrollbarVisible()) {
            withScrollY(yScroll - (int) (delta * 10.0));
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
