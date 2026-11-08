// Port glue (ours, GPLv3), TP-D4: stands in for Resourceful Lib's BaseCursorScreen (a plain Screen) for the widget
// kit. Hosts popup layers (colour picker, dropdown lists) and widget tooltips, ticks text boxes, and routes input the
// same way AbstractTempadScreen does for the Tempad screens. No cursor shapes (PORT_NOTES 6.13).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public abstract class KitScreen extends Screen implements Popup.Host {
    private final Deque<Popup> popups = new ArrayDeque<>();
    @Nullable
    private Component frameTooltip;
    @Nullable
    private GuiEventListener draggingChild;

    protected KitScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        // A resize rebuilds the screen; open popups were placed for the old size (Olympus closes its overlays too).
        for (Popup popup : List.copyOf(popups)) closePopup(popup);
        super.init();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        boolean blocked = !popups.isEmpty();
        int mx = blocked ? -1 : mouseX;
        int my = blocked ? -1 : mouseY;
        renderBackground(pose);
        super.render(pose, mx, my, partialTick);
        if (!blocked) {
            for (GuiEventListener child : children()) {
                if (child instanceof TooltipSource source) {
                    Component tooltip = source.tooltipAt(mouseX, mouseY);
                    if (tooltip != null) frameTooltip = tooltip;
                }
            }
        }
        int z = 400;
        for (Iterator<Popup> it = popups.descendingIterator(); it.hasNext(); ) {
            Popup popup = it.next();
            pose.pushPose();
            pose.translate(0, 0, z);
            boolean top = popup == popups.peek();
            popup.render(pose, top ? mouseX : -1, top ? mouseY : -1, partialTick);
            pose.popPose();
            z += 100;
        }
        if (frameTooltip != null) {
            pose.pushPose();
            pose.translate(0, 0, 400 + popups.size() * 100);
            renderTooltip(pose, frameTooltip, mouseX, mouseY);
            pose.popPose();
            frameTooltip = null;
        }
    }

    // --- Popup.Host ---

    @Override
    public void openPopup(Popup popup) {
        setFocused(null);
        popups.push(popup);
    }

    @Override
    public void closePopup(Popup popup) {
        if (popups.remove(popup)) popup.onClosed();
    }

    @Override
    public void setTooltipForNextFrame(Component tooltip) {
        frameTooltip = tooltip;
    }

    @Override
    public void tick() {
        super.tick();
        for (GuiEventListener child : children()) {
            if (child instanceof EditBox box) box.tick();
        }
        for (Popup popup : List.copyOf(popups)) popup.tick();
    }

    // --- input: the top popup takes everything ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Popup top = popups.peek();
        if (top != null) {
            top.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (GuiEventListener child : children()) {
            if (child instanceof EditBox box && !box.isMouseOver(mouseX, mouseY)) box.setFocus(false);
        }
        draggingChild = null;
        for (GuiEventListener child : new ArrayList<>(children())) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocused(child);
                if (button == 0) {
                    setDragging(true);
                    draggingChild = child;
                }
                return true;
            }
        }
        setFocused(null);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Popup top = popups.peek();
        if (top != null) {
            top.mouseReleased(mouseX, mouseY, button);
            return true;
        }
        if (draggingChild != null) {
            GuiEventListener child = draggingChild;
            draggingChild = null;
            setDragging(false);
            child.mouseReleased(mouseX, mouseY, button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        Popup top = popups.peek();
        if (top != null) {
            top.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        if (draggingChild != null) {
            draggingChild.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Popup top = popups.peek();
        if (top != null) {
            top.mouseScrolled(mouseX, mouseY, delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Popup top = popups.peek();
        if (top != null) {
            if (!top.keyPressed(keyCode, scanCode, modifiers) && keyCode == GLFW.GLFW_KEY_ESCAPE) closePopup(top);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        Popup top = popups.peek();
        if (top != null) return top.charTyped(c, modifiers);
        return super.charTyped(c, modifiers);
    }

    @Override
    public void removed() {
        for (Popup popup : List.copyOf(popups)) closePopup(popup);
        super.removed();
    }
}
