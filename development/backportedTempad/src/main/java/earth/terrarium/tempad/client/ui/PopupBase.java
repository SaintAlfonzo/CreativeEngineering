// Port glue (ours, GPLv3): a popup that holds widgets (Olympus Overlay screens, see Popup).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class PopupBase implements Popup {
    protected final List<AbstractWidget> widgets = new ArrayList<>();
    private AbstractWidget clicked;

    protected <W extends AbstractWidget> W add(W widget) {
        widgets.add(widget);
        return widget;
    }

    protected void renderWidgets(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        for (AbstractWidget widget : widgets) widget.render(pose, mouseX, mouseY, partialTick);
        Host host = Popup.host();
        if (host == null) return;
        for (AbstractWidget widget : widgets) {
            if (widget instanceof TooltipSource source) {
                Component tooltip = source.tooltipAt(mouseX, mouseY);
                if (tooltip != null) host.setTooltipForNextFrame(tooltip);
            }
        }
    }

    protected boolean clickWidgets(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : widgets) {
            if (widget instanceof EditBox box && !box.isMouseOver(mouseX, mouseY)) box.setFocus(false);
        }
        for (AbstractWidget widget : List.copyOf(widgets)) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                clicked = widget;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        AbstractWidget w = clicked;
        clicked = null;
        return w != null && w.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return clicked != null && clicked.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (AbstractWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY) && widget.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        for (AbstractWidget widget : widgets) {
            if (widget instanceof EditBox box && box.isFocused()) {
                if (key == 256) {
                    box.setFocus(false);
                    return true;
                }
                box.keyPressed(key, scan, modifiers);
                return true;
            }
        }
        if (key == 256) {
            close();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        for (AbstractWidget widget : widgets) {
            if (widget instanceof EditBox box && box.isFocused()) return box.charTyped(c, modifiers);
        }
        return false;
    }

    @Override
    public void tick() {
        for (AbstractWidget widget : widgets) {
            if (widget instanceof EditBox box) box.tick();
        }
    }
}
