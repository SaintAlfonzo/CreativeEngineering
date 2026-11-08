// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/screen/tempad/AbstractTempadScreen.kt.
// Translated to Java for Forge 1.19.2. Also the host of the widget kit's popup layers and widget tooltips (TP-D4), and
// it draws the darkened world behind itself, which 1.21's Screen.render does on its own.
package earth.terrarium.tempad.client.screen.tempad;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.app.AppRegistry;
import earth.terrarium.tempad.api.app.TempadApp;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.state.MutableState;
import earth.terrarium.tempad.client.ui.AppList;
import earth.terrarium.tempad.client.ui.Button;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.client.ui.Popup;
import earth.terrarium.tempad.client.ui.Renderers;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.client.ui.TextBox;
import earth.terrarium.tempad.client.ui.TooltipSource;
import earth.terrarium.tempad.common.menu.AbstractTempadMenu;
import earth.terrarium.tempad.common.network.c2s.RedirectAppPacket;
import earth.terrarium.tempad.common.network.c2s.UpdateTempadLockPacket;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModNetworking;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public abstract class AbstractTempadScreen<T extends AbstractTempadMenu<?>> extends AbstractContainerScreen<T> implements Popup.Host {
    public static final ResourceLocation background = Tempad.id("screen/tempad");

    @Nullable
    private final ResourceLocation appSprite;
    protected final Inventory inv;
    protected int localLeft;
    protected int localTop;
    private String timeText = "";
    protected final MutableState<Boolean> locked;

    private final Deque<Popup> popups = new ArrayDeque<>();
    @Nullable
    private Component frameTooltip;
    @Nullable
    private GuiEventListener draggingChild;

    protected AbstractTempadScreen(@Nullable ResourceLocation appSprite, T menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.appSprite = appSprite;
        this.inv = inv;
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.titleLabelX = 34;
        this.titleLabelY = 27;
        this.locked = MutableState.of(ModComponents.isLocked(menu.ctx.getStack()));
    }

    @Override
    protected void init() {
        super.init();
        // A resize rebuilds the screen; open popups were placed for the old size (Olympus closes its overlays too).
        for (Popup popup : List.copyOf(popups)) closePopup(popup);
        this.localTop = this.topPos + 20;
        this.localLeft = this.leftPos + 30;

        AppList appList = new AppList(localLeft - 16, localTop + 1, 16, 116, 15,
                entry -> ModNetworking.sendToServer(new RedirectAppPacket(entry.appId(), menu.ctxHolder, menu.appContent.isStationary)));
        for (Map.Entry<ResourceLocation, TempadApp<?>> app : AppRegistry.getAll(menu.ctxHolder.getCtx(minecraft.player), menu.appContent.isStationary).entrySet()) {
            appList.addEntry(new AppList.Entry(app.getValue(), app.getKey()));
        }
        addRenderableWidget(appList);

        Button close = new Button().withTexture(TempadUI.steelButton)
                .withRenderer(Renderers.icon(TempadUI.xIcon).withColor(Gfx.BLACK).withCentered(7, 7))
                .withCallback(this::onClose);
        close.withSize(11).withPosition(leftPos + 233, topPos + 13);
        addRenderableWidget(close);
        if (menu.appContent.isStationary) {
            Button lock = new Button().withTexture(TempadUI.steelButton)
                    .withRenderer(locked.withRenderer(isLocked -> Renderers.icon(isLocked ? TempadUI.lockIcon : TempadUI.unlockIcon).withColor(Gfx.BLACK).withCentered(7, 7)))
                    .withCallback(() -> {
                        locked.setValue(!locked.getValue());
                        ModNetworking.sendToServer(new UpdateTempadLockPacket(locked.getValue(), menu.ctxHolder));
                    });
            lock.withSize(11).withPosition(leftPos + 233, topPos + 25);
            addRenderableWidget(lock);
        }
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        Sprites.blit(pose, background, leftPos, topPos, imageWidth, imageHeight);
        if (appSprite != null) {
            RenderSystem.enableBlend();
            Sprites.blit(pose, appSprite, leftPos + 30, topPos + 20, 198, 118);
        }
        ChrononHandler chronons = ChrononHandler.of(menu.ctx.getStack());
        if (chronons != null) {
            int height = (int) (Math.max(0f, Math.min(1f, (float) chronons.getPower() / chronons.getMaxPower())) * 54);
            Sprites.blit(pose, TempadUI.powerVert, 6, 54, 0, 54 - height, localLeft + 207, localTop + 32 + 54 - height, 6, height);
            if (mouseX >= localLeft + 207 && mouseX <= localLeft + 211 && mouseY >= localTop + 32 && mouseY <= localTop + 86) {
                setTooltipForNextFrame(Component.literal(chronons.getPower() + "/" + chronons.getMaxPower()));
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        Gfx.drawString(pose, font, title, titleLabelX, titleLabelY, Tempad.ORANGE.getValue(), true);
        if (minecraft != null && minecraft.level != null) {
            long time = minecraft.level.getDayTime() % 24000;
            int minutes = (int) ((time % 1000) * 0.06);
            long hours = ((time / 1000) + 6) % 24;
            timeText = String.format("%02d:%02d", hours, minutes);
            Gfx.drawString(pose, font, timeText, 30 + 194 - font.width(timeText), titleLabelY, Tempad.ORANGE.getValue(), true);
        }
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        boolean blocked = !popups.isEmpty();
        int mx = blocked ? -1 : mouseX;
        int my = blocked ? -1 : mouseY;
        renderBackground(pose);
        super.render(pose, mx, my, partialTick);
        renderTooltip(pose, mx, my);
        if (!blocked) {
            for (GuiEventListener child : children()) {
                if (child instanceof TooltipSource source) {
                    Component tooltip = source.tooltipAt(mouseX, mouseY);
                    if (tooltip != null) frameTooltip = tooltip;
                }
            }
        }
        if (blocked) {
            int z = 400;
            for (java.util.Iterator<Popup> it = popups.descendingIterator(); it.hasNext(); ) {
                Popup popup = it.next();
                pose.pushPose();
                pose.translate(0, 0, z);
                // Only the top popup sees the mouse, as only the top Olympus overlay screen gets input.
                boolean top = popup == popups.peek();
                popup.render(pose, top ? mouseX : -1, top ? mouseY : -1, partialTick);
                pose.popPose();
                z += 100;
            }
        }
        if (frameTooltip != null) {
            pose.pushPose();
            pose.translate(0, 0, 400 + popups.size() * 100);
            renderTooltip(pose, frameTooltip, mouseX, mouseY);
            pose.popPose();
            frameTooltip = null;
        }
    }

    /** The open popup layers, top first (empty when none is open). */
    protected Deque<Popup> popups() {
        return popups;
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
    protected void containerTick() {
        super.containerTick();
        for (GuiEventListener child : children()) {
            if (child instanceof TextBox box) box.tick();
        }
        for (Popup popup : List.copyOf(popups)) popup.tick();
    }

    // --- input: the top popup takes everything; text boxes keep their keys (upstream keyPressed) ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Popup top = popups.peek();
        if (top != null) {
            top.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        for (GuiEventListener child : children()) {
            if (child instanceof TextBox box && !box.isMouseOver(mouseX, mouseY)) box.setFocus(false);
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
        return super.mouseClicked(mouseX, mouseY, button);
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
        if (getFocused() instanceof TextBox box && box.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                box.setFocus(false);
                setFocused(null);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_TAB) return super.keyPressed(keyCode, scanCode, modifiers);
            box.keyPressed(keyCode, scanCode, modifiers);
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
