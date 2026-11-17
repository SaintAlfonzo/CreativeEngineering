package com.SaintAlfonzo.cdevcapture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Widget actions (TX-05, client_capture.md 2.2), resolved from the open screen, not from screen pixels. Clicks go
 * through the screen's own {@code mouseClicked} / {@code mouseReleased} at the widget's or slot's centre, so focus,
 * shift-click and creative-tab handling are the screen's own; the virtual mouse is parked there too (hover state).
 *
 * <pre>
 * {"click_slot": N, "button": 0, "shift": false}        slot N of the open container (index as in hover / slot_item)
 * {"click_widget": REF, "button": 0}                     REF: "label" (message text, exact then substring, case-
 *                                                        insensitive), N (index in READ widgets), or
 *                                                        {"class": "EditBox", "n": 0} (n-th widget of that class)
 * {"type_into": REF | null, "text": "...", "clear": true, "enter": false}
 *                                                        clicks REF (null = keep the focused one), clears an EditBox,
 *                                                        types each character through the screen's charTyped
 * {"scroll": delta, "at": REF} or {"scroll": d, "x": gx, "y": gy}   mouseScrolled there (default screen centre)
 * {"click": {"x": gx, "y": gy} | {"px": wx, "py": wy}, "button": 0}  raw click, GUI or window pixels
 * {"press": "escape" | "e" | "enter" | "key.inventory" ..., "shift": false}
 *                                                        a key: to the open screen (keyPressed / keyReleased), or with
 *                                                        no screen through the key mapping (escape = pause menu)
 * </pre>
 */
final class Widgets {

    private Widgets() {}

    static Steps.Step parse(JsonObject o) {
        if (o.has("click_slot")) return new Act("click_slot", o);
        if (o.has("click_widget")) return new Act("click_widget", o);
        if (o.has("type_into")) return new Act("type_into", o);
        if (o.has("scroll")) return new Act("scroll", o);
        if (o.has("click")) return new Act("click", o);
        if (o.has("press")) return new Act("press", o);
        return null;
    }

    /** A widget of the open screen with its bounds in GUI coordinates (null bounds when unknown). */
    record Entry(GuiEventListener l, int[] rect) {
        String describe() {
            String msg = l instanceof AbstractWidget w ? w.getMessage().getString() : "";
            String r = rect == null ? "? ? ? ?" : rect[0] + " " + rect[1] + " " + rect[2] + " " + rect[3];
            String extra = l instanceof EditBox e ? " value " + Reads.quote(e.getValue()) : "";
            if (l instanceof AbstractWidget w && !w.active) extra += " inactive";
            return l.getClass().getSimpleName() + " " + Reads.quote(msg) + " " + r + extra;
        }
    }

    /** The screen's widgets, nested containers flattened depth-first, each listener once. */
    static List<Entry> list(Screen s) {
        if (s == null) return List.of();
        List<Entry> out = new ArrayList<>();
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        walk(s, out, seen);
        return out;
    }

    private static void walk(ContainerEventHandler c, List<Entry> out, Set<Object> seen) {
        for (GuiEventListener l : c.children()) {
            if (!seen.add(l)) continue;
            int[] rect = l instanceof AbstractWidget w ? new int[]{w.x, w.y, w.getWidth(), w.getHeight()} : null;
            out.add(new Entry(l, rect));
            if (l instanceof ContainerEventHandler inner && !(l instanceof Screen)) walk(inner, out, seen);
        }
    }

    static Entry resolve(Screen s, JsonElement ref) {
        List<Entry> ws = list(s);
        if (ws.isEmpty()) throw new Steps.StepFailed("no widgets (screen " + (s == null ? "none" : s.getClass().getSimpleName()) + ")");
        if (ref.isJsonPrimitive() && ref.getAsJsonPrimitive().isNumber()) {
            int n = ref.getAsInt();
            if (n < 0 || n >= ws.size()) throw new Steps.StepFailed("no widget " + n + " (" + ws.size() + " widgets)");
            return ws.get(n);
        }
        if (ref.isJsonObject()) {
            JsonObject o = ref.getAsJsonObject();
            String cls = o.get("class").getAsString();
            int n = o.has("n") ? o.get("n").getAsInt() : 0;
            for (Entry e : ws) {
                Class<?> c = e.l().getClass();
                if ((c.getSimpleName().equals(cls) || c.getName().equals(cls) || isA(c, cls)) && n-- == 0) return e;
            }
            throw new Steps.StepFailed("no widget of class " + cls);
        }
        String want = ref.getAsString().trim().toLowerCase(Locale.ROOT);
        for (Entry e : ws) if (label(e).equals(want)) return e;
        for (Entry e : ws) if (!want.isEmpty() && label(e).contains(want)) return e;
        throw new Steps.StepFailed("no widget labelled \"" + ref.getAsString() + "\"");
    }

    private static boolean isA(Class<?> c, String simple) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) if (k.getSimpleName().equals(simple)) return true;
        return false;
    }

    private static String label(Entry e) {
        return e.l() instanceof AbstractWidget w ? w.getMessage().getString().trim().toLowerCase(Locale.ROOT) : "";
    }

    static double[] centre(Entry e) {
        if (e.rect() == null) throw new Steps.StepFailed("widget " + e.describe() + " has no bounds");
        return new double[]{e.rect()[0] + e.rect()[2] / 2.0, e.rect()[1] + e.rect()[3] / 2.0};
    }

    static void parkMouse(Minecraft mc, double gx, double gy) {
        Window w = mc.getWindow();
        VirtualInput.mouseX = gx * w.getScreenWidth() / w.getGuiScaledWidth();
        VirtualInput.mouseY = gy * w.getScreenHeight() / w.getGuiScaledHeight();
        VirtualInput.mouseActive = true;
    }

    static final class Act extends Steps.Step {
        final JsonObject o;
        double x, y;
        int button;
        boolean shift0;

        Act(String kind, JsonObject o) {
            super(kind);
            this.o = o;
            button = o.has("button") ? o.get("button").getAsInt() : 0;
        }

        private Screen screen(Minecraft mc) {
            if (mc.screen == null) throw new Steps.StepFailed(kind + ": no screen open");
            return mc.screen;
        }

        @Override
        boolean tick(CaptureDriver d) throws Exception {
            Minecraft mc = d.mc;
            switch (kind) {
                case "click_slot", "click_widget", "click" -> {
                    if (ticks == 0) {
                        Screen s = screen(mc);
                        target(mc, s);
                        shift0 = VirtualInput.shift;
                        VirtualInput.shift = shift0 || Steps.bool(o, "shift");
                        parkMouse(mc, x, y);
                        note = kind + " at " + (int) x + "," + (int) y + " -> " + s.mouseClicked(x, y, button);
                        return false;
                    }
                    if (ticks == 1) {
                        if (mc.screen != null) mc.screen.mouseReleased(x, y, button);
                        VirtualInput.shift = shift0;
                    }
                    return ticks >= 3;
                }
                case "type_into" -> {
                    if (ticks == 0) {
                        Screen s = screen(mc);
                        JsonElement ref = o.get("type_into");
                        if (ref != null && !ref.isJsonNull()) {
                            Entry e = resolve(s, ref);
                            double[] c = centre(e);
                            s.mouseClicked(c[0], c[1], 0);
                            s.mouseReleased(c[0], c[1], 0);
                            if (e.l() instanceof EditBox eb) {
                                eb.setFocus(true);
                                s.setFocused(eb);
                                if (!o.has("clear") || o.get("clear").getAsBoolean()) eb.setValue("");
                            }
                            note = "into " + e.describe();
                        }
                        return false;
                    }
                    if (ticks == 1) {
                        Screen s = screen(mc);
                        String text = o.has("text") ? o.get("text").getAsString() : "";
                        for (int i = 0; i < text.length(); ) {
                            int cp = text.codePointAt(i);
                            for (char ch : Character.toChars(cp)) s.charTyped(ch, 0);
                            i += Character.charCount(cp);
                        }
                        if (Steps.bool(o, "enter")) {
                            s.keyPressed(InputConstants.KEY_RETURN, 0, 0);
                            s.keyReleased(InputConstants.KEY_RETURN, 0, 0);
                        }
                        for (Entry e : list(mc.screen)) {
                            if (e.l() instanceof EditBox eb && eb.isFocused()) note += " -> value " + Reads.quote(eb.getValue());
                        }
                    }
                    return ticks >= 3;
                }
                case "scroll" -> {
                    if (ticks == 0) {
                        Screen s = screen(mc);
                        if (o.has("at")) {
                            double[] c = centre(resolve(s, o.get("at")));
                            x = c[0];
                            y = c[1];
                        } else {
                            x = o.has("x") ? o.get("x").getAsDouble() : mc.getWindow().getGuiScaledWidth() / 2.0;
                            y = o.has("y") ? o.get("y").getAsDouble() : mc.getWindow().getGuiScaledHeight() / 2.0;
                        }
                        parkMouse(mc, x, y);
                        double delta = o.get("scroll").getAsDouble();
                        note = "scroll " + delta + " at " + (int) x + "," + (int) y + " -> " + s.mouseScrolled(x, y, delta);
                        return false;
                    }
                    return ticks >= 3;
                }
                case "press" -> {
                    if (ticks == 0) {
                        press(mc, o.get("press").getAsString(), Steps.bool(o, "shift"));
                        return false;
                    }
                    return ticks >= 3;
                }
                default -> throw new IllegalArgumentException("unknown widget action " + kind);
            }
        }

        private void target(Minecraft mc, Screen s) {
            switch (kind) {
                case "click_slot" -> {
                    if (!(s instanceof AbstractContainerScreen<?> acs)) throw new Steps.StepFailed("click_slot: no container screen");
                    int n = o.get("click_slot").getAsInt();
                    List<Slot> slots = acs.getMenu().slots;
                    if (n < 0 || n >= slots.size()) throw new Steps.StepFailed("click_slot: no slot " + n + " (" + slots.size() + " slots)");
                    x = acs.getGuiLeft() + slots.get(n).x + 8;
                    y = acs.getGuiTop() + slots.get(n).y + 8;
                }
                case "click_widget" -> {
                    double[] c = centre(resolve(s, o.get("click_widget")));
                    x = c[0];
                    y = c[1];
                }
                default -> {
                    JsonObject at = o.getAsJsonObject("click");
                    if (at.has("px")) {
                        Window w = mc.getWindow();
                        x = at.get("px").getAsDouble() * w.getGuiScaledWidth() / w.getScreenWidth();
                        y = at.get("py").getAsDouble() * w.getGuiScaledHeight() / w.getScreenHeight();
                    } else {
                        x = at.get("x").getAsDouble();
                        y = at.get("y").getAsDouble();
                    }
                }
            }
        }

        private void press(Minecraft mc, String name, boolean shift) {
            String n = name.trim().toLowerCase(Locale.ROOT);
            KeyMapping mapping = null;
            for (KeyMapping km : mc.options.keyMappings) if (km.getName().equals(name)) mapping = km;
            InputConstants.Key key;
            try {
                key = mapping != null ? mapping.getKey() : InputConstants.getKey("key.keyboard." + n);
            } catch (IllegalArgumentException e) {
                key = InputConstants.UNKNOWN;
            }
            if (key == InputConstants.UNKNOWN) throw new Steps.StepFailed("press: unknown key " + name);
            int mods = shift ? 1 : 0; // GLFW_MOD_SHIFT
            boolean s0 = VirtualInput.shift;
            VirtualInput.shift = s0 || shift;
            try {
                if (mc.screen != null) {
                    note = "screen keyPressed " + key.getName() + " -> " + mc.screen.keyPressed(key.getValue(), 0, mods);
                    if (mc.screen != null) mc.screen.keyReleased(key.getValue(), 0, mods);
                } else if (key.getValue() == InputConstants.KEY_ESCAPE) {
                    mc.pauseGame(false);
                    note = "pause menu";
                } else {
                    KeyMapping.click(key);
                    note = "mapping click " + key.getName();
                }
            } finally {
                VirtualInput.shift = s0;
            }
        }
    }
}
