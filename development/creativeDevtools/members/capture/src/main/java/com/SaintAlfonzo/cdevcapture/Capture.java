package com.SaintAlfonzo.cdevcapture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.Screenshot;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Framebuffer shots and the rectangles the cropper needs. Every rect is in GUI coordinates; the sidecar
 * carries the GUI scale so crop.py converts them to pixels.
 *
 * <p>Tooltip bounds come from {@code RenderTooltipEvent.Pre} at the lowest priority (after any mod moved the
 * tooltip), computed the way {@code Screen.renderTooltipInternal} places it, without the border; the
 * capture's {@code pad} covers Legendary Tooltips' frame. Only tooltips drawn in the captured frame count.
 */
final class Capture {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** What the current capture hides; empty outside a capture. */
    final Set<String> hide = new HashSet<>();
    private final List<int[]> frameTooltips = new ArrayList<>();

    /** Render tick START: the tooltip list then only holds what the coming frame draws. */
    void beginFrame() {
        frameTooltips.clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onTooltip(RenderTooltipEvent.Pre e) {
        List<ClientTooltipComponent> comps = e.getComponents();
        if (comps.isEmpty()) return;
        int w = 0, h = comps.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent c : comps) {
            w = Math.max(w, c.getWidth(e.getFont()));
            h += c.getHeight();
        }
        int x = e.getX() + 12, y = e.getY() - 12;
        if (x + w > e.getScreenWidth()) x -= 28 + w;
        if (y + h + 6 > e.getScreenHeight()) y = e.getScreenHeight() - h - 6;
        frameTooltips.add(new int[]{x, y, w, h});
    }

    @SubscribeEvent
    public void onHand(RenderHandEvent e) {
        if (hide.contains("hand")) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onOverlay(RenderGuiOverlayEvent.Pre e) {
        if (hide.contains("crosshair") && e.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) e.setCanceled(true);
        if (hide.contains("chat") && e.getOverlay().id().equals(VanillaGuiOverlay.CHAT_PANEL.id())) e.setCanceled(true);
    }

    /** Applied before the settle frames of a capture. Chat and toasts are cleared whatever {@code hide} says. */
    void applyHide(Minecraft mc, Set<String> what) {
        hide.clear();
        hide.addAll(what);
        mc.gui.getChat().clearMessages(false);
        mc.getToasts().clear();
        mc.options.renderDebug = what.contains("debug") ? false : mc.options.renderDebug;
        mc.options.hideGui = what.contains("hud");
    }

    void clearHide(Minecraft mc) {
        hide.clear();
        mc.options.hideGui = false;
    }

    /**
     * Writes {@code <rawDir>/<file>.png} and {@code <file>.json}. Must run at the end of a frame (render tick
     * END), when the main render target holds the finished frame, GUI included.
     */
    JsonObject shoot(Minecraft mc, Path rawDir, String file, JsonObject extra) throws IOException {
        Files.createDirectories(rawDir);
        try (NativeImage img = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            img.writeToFile(rawDir.resolve(file + ".png"));
        }
        JsonObject side = sidecar(mc);
        extra.entrySet().forEach(en -> side.add(en.getKey(), en.getValue()));
        side.addProperty("file", file + ".png");
        Files.writeString(rawDir.resolve(file + ".json"), GSON.toJson(side), StandardCharsets.UTF_8);
        return side;
    }

    private JsonObject sidecar(Minecraft mc) {
        Window win = mc.getWindow();
        JsonObject o = new JsonObject();
        o.addProperty("gui_scale", win.getGuiScale());
        o.add("frame", arr(mc.getMainRenderTarget().width, mc.getMainRenderTarget().height));
        o.add("gui_size", arr(win.getGuiScaledWidth(), win.getGuiScaledHeight()));
        o.add("crosshair", arr(win.getGuiScaledWidth() / 2, win.getGuiScaledHeight() / 2));
        Screen screen = mc.screen;
        o.addProperty("screen", screen == null ? null : screen.getClass().getName());
        int[] gui = guiRect(screen);
        if (gui != null) o.add("gui", arr(gui));
        if (!frameTooltips.isEmpty()) {
            o.add("tooltip", arr(frameTooltips.get(frameTooltips.size() - 1)));
            JsonArray all = new JsonArray();
            frameTooltips.forEach(t -> all.add(arr(t)));
            o.add("tooltips", all);
        }
        if (screen instanceof AbstractContainerScreen<?> acs) {
            JsonArray slots = new JsonArray();
            List<Slot> list = acs.getMenu().slots;
            for (int i = 0; i < list.size(); i++) {
                Slot s = list.get(i);
                slots.add(arr(i, acs.getGuiLeft() + s.x, acs.getGuiTop() + s.y));
            }
            o.add("slots", slots);
        }
        if (mc.player != null) {
            o.add("player", arrD(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
            o.add("look", arrD(mc.player.getYRot(), mc.player.getXRot()));
        }
        return o;
    }

    /** The open screen's main area: container bounds, JEI's recipe screen (with its side tabs), else null. */
    static int[] guiRect(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> acs) {
            return new int[]{acs.getGuiLeft(), acs.getGuiTop(), acs.getXSize(), acs.getYSize()};
        }
        if (screen != null && screen.getClass().getName().equals("mezz.jei.gui.recipes.RecipesGui")) {
            try {
                Object area = screen.getClass().getMethod("getArea").invoke(screen);
                int extra = (int) screen.getClass().getMethod("getLeftSideExtraWidth").invoke(screen);
                Method gx = area.getClass().getMethod("getX"), gy = area.getClass().getMethod("getY");
                Method gw = area.getClass().getMethod("getWidth"), gh = area.getClass().getMethod("getHeight");
                return new int[]{(int) gx.invoke(area) - extra, (int) gy.invoke(area),
                        (int) gw.invoke(area) + extra, (int) gh.invoke(area)};
            } catch (ReflectiveOperationException | ClassCastException e) {
                CaptureDriver.LOG.warn("[cdevcapture] JEI recipe screen bounds unavailable: {}", e.toString());
            }
        }
        return null;
    }

    private static JsonArray arr(int... v) {
        JsonArray a = new JsonArray();
        for (int i : v) a.add(i);
        return a;
    }

    private static JsonArray arrD(double... v) {
        JsonArray a = new JsonArray();
        for (double d : v) a.add(Math.round(d * 100) / 100.0);
        return a;
    }
}
