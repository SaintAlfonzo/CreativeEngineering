// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext.
package earth.terrarium.tempad.client.ui;

import net.minecraft.client.gui.components.AbstractWidget;

/** The area a {@link WidgetRenderer} draws in (the widget's bounds, narrowed by padding / centring). */
public final class Ctx {
    public final AbstractWidget widget;
    public final int mouseX;
    public final int mouseY;
    public int x;
    public int y;
    public int width;
    public int height;

    public Ctx(AbstractWidget widget, int mouseX, int mouseY) {
        this(widget, mouseX, mouseY, widget.x, widget.y, widget.getWidth(), widget.getHeight());
    }

    public Ctx(AbstractWidget widget, int mouseX, int mouseY, int x, int y, int width, int height) {
        this.widget = widget;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Ctx copy() {
        return new Ctx(widget, mouseX, mouseY, x, y, width, height);
    }

    public Ctx with(int x, int y, int width, int height) {
        return new Ctx(widget, mouseX, mouseY, x, y, width, height);
    }
}
