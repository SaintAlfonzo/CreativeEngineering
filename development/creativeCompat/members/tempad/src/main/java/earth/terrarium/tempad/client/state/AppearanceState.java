// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/state/AppearanceState.kt. Translated to Java for Forge 1.19.2.
package earth.terrarium.tempad.client.state;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.List;

/** Shows / enables every widget added to it together (a widget is added again on every screen init). */
public class AppearanceState extends MutableState<List<AbstractWidget>> {
    private boolean visible = true;
    private boolean active = true;

    public AppearanceState() {
        super(new ArrayList<>());
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        for (AbstractWidget w : value) w.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        for (AbstractWidget w : value) w.active = active;
    }

    public <W extends AbstractWidget> W add(W widget) {
        value.add(widget);
        widget.visible = visible;
        widget.active = active;
        return widget;
    }
}
