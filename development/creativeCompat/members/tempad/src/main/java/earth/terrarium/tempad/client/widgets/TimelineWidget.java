// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/widgets/TimelineWidget.kt.
// Translated to Java for Forge 1.19.2: Olympus BaseParentWidget -> the client/ui KitWidget (the items take no input,
// as upstream, so they are drawn but not registered as child listeners); 1.19.2 has only the vertical scroll delta
// (upstream: scroll - scrollX * 10 + scrollY * 10).
package earth.terrarium.tempad.client.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.KitWidget;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimelineWidget extends KitWidget {
    protected final List<Item> items = new ArrayList<>();

    protected double scroll = 0.0;
    protected int lastWidth = 0;

    @Nullable
    public Item current;

    public TimelineWidget(int width, int height) {
        super(width, height);
    }

    public void clear() {
        items.clear();
    }

    public void set(List<Item> items) {
        this.items.clear();
        this.items.addAll(items);
        updateScrollBar();
    }

    public void add(Item item) {
        items.add(item);
        item.parent = this;
        item.height = height;
        updateScrollBar();
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        Gfx.enableScissor(x + 1, y, x + width, y + height);

        int x = this.x + width / 2 - (int) scroll - 10;
        this.lastWidth = 0;

        for (Item item : items) {
            item.x = x;
            item.y = y;
            item.current = x > (this.x + width / 2) - item.width && x <= (this.x + width / 2);
            if (item.current) {
                current = item;
            }
            item.render(pose, this.x, this.y + height / 2, partialTicks);
            x += item.width;
            this.lastWidth += item.width;
        }

        Gfx.disableScissor();
    }

    /** Upstream's parent widget forwards clicks to the items, which ignore them. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        this.scroll = Mth.clamp(this.scroll + delta * 10, 0.0, lastWidth - 20.0);
        return true;
    }

    private void updateLastWidth() {
        this.lastWidth = 0;
        int x = this.x - (int) scroll;
        for (Item item : items) {
            item.x = x;
            item.y = y;
            this.lastWidth += item.width;
            x += item.width;
        }
    }

    protected void updateScrollBar() {
        updateLastWidth();
        this.scroll = Mth.clamp(this.scroll, 0.0, Math.max(0.0, this.lastWidth));
    }

    public void scrollToBottom() {
        updateLastWidth();
        this.scroll = Math.max(0.0, this.lastWidth - 20.0);
    }

    public abstract static class Item {
        protected int x;
        protected int y;
        @Nullable
        protected TimelineWidget parent;
        protected int height;
        protected int width = 20;
        public boolean current = false;

        public abstract void render(PoseStack pose, int mouseX, int mouseY, float partialTick);
    }
}
