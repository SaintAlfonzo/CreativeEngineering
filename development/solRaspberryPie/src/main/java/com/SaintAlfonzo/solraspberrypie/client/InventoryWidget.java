package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The diet bar on the survival inventory (11.1): active flat bonuses first (gold frame), then active filled slots.
 * Click (locked) opens the effect screen; unlocked, it drags and snaps to the inventory's edges on both axes at once.
 * Hidden while the recipe book is open (the Curios panel is its own screen, so the bar is not drawn there).
 */
public final class InventoryWidget {
    private static final int CELL = 20;
    private static final int PAD = 2;
    private static final int SNAP = 6;

    private record Cell(ResourceLocation effect, int level, boolean flat, int slot) {}

    private static boolean dragging;
    private static boolean moved;
    private static double grabX, grabY;
    private static int dragX, dragY;
    private static Rect2i lastRect;

    public static boolean shown(InventoryScreen inv) {
        return RpConfig.CLIENT.widgetEnabled.get() && ClientState.ready() && !inv.getRecipeBookComponent().isVisible();
    }

    private static List<Cell> cells() {
        List<Cell> out = new ArrayList<>();
        S2CState s = ClientState.state;
        for (Map.Entry<ResourceLocation, Integer> e : s.flat.entrySet()) out.add(new Cell(e.getKey(), e.getValue(), true, -1));
        for (int i = 0; i < s.activeSlots && i < s.slots.size(); i++) {
            SlotChoice c = s.slots.get(i);
            if (c != null && !s.flat.containsKey(c.effect())) out.add(new Cell(c.effect(), c.level(), false, i));
        }
        return out;
    }

    /** The bar's rectangle in screen coordinates (vertical beside the inventory, horizontal above or below it). */
    public static Rect2i rect(InventoryScreen inv) {
        int n = Math.max(1, cells().size());
        int relX = dragging ? dragX : RpConfig.CLIENT.widgetX.get();
        int relY = dragging ? dragY : RpConfig.CLIENT.widgetY.get();
        boolean vertical = vertical(inv);
        int w = vertical ? CELL + 2 * PAD : n * CELL + 2 * PAD;
        int h = vertical ? n * CELL + 2 * PAD : CELL + 2 * PAD;
        return new Rect2i(inv.getGuiLeft() + relX, inv.getGuiTop() + relY, w, h);
    }

    private static boolean vertical(InventoryScreen inv) {
        int relX = dragging ? dragX : RpConfig.CLIENT.widgetX.get();
        return relX <= -(CELL + 2 * PAD) || relX >= inv.getXSize();
    }

    public static Rect2i lastRect() {
        return lastRect;
    }

    public static void render(InventoryScreen inv, PoseStack pose, int mouseX, int mouseY) {
        if (!shown(inv)) {
            lastRect = null;
            return;
        }
        Rect2i r = rect(inv);
        lastRect = r;
        boolean locked = RpConfig.CLIENT.widgetLocked.get();
        int border = locked ? 0xFF555555 : 0xFFAAAA55;
        GuiComponent.fill(pose, r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() + r.getHeight(), border);
        GuiComponent.fill(pose, r.getX() + 1, r.getY() + 1, r.getX() + r.getWidth() - 1, r.getY() + r.getHeight() - 1, 0xFF1E1E1E);
        boolean vertical = vertical(inv);
        List<Cell> cells = cells();
        boolean paused = ClientState.state.paused;
        Cell hovered = null;
        for (int i = 0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            int x = r.getX() + PAD + (vertical ? 0 : i * CELL);
            int y = r.getY() + PAD + (vertical ? i * CELL : 0);
            if (c.flat) GuiComponent.fill(pose, x, y, x + CELL, y + CELL, 0xFF8A6D1E);
            if (paused) RenderSystem.setShaderColor(0.35F, 0.35F, 0.35F, 1F);
            Icons.effect(pose, c.effect, x + 1, y + 1);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) hovered = c;
        }
        if (cells.isEmpty()) {
            Minecraft.getInstance().font.draw(pose, "+", r.getX() + PAD + 7, r.getY() + PAD + 6, 0xFFAAAAAA);
        }
        if (dragging) return;
        if (hovered != null) {
            inv.renderComponentTooltip(pose, tooltip(hovered), mouseX, mouseY);
        } else if (r.contains(mouseX, mouseY)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("solraspberrypie.widget.title"));
            tip.add(Component.translatable(locked ? "solraspberrypie.widget.locked" : "solraspberrypie.widget.unlocked").withStyle(ChatFormatting.GRAY));
            inv.renderComponentTooltip(pose, tip, mouseX, mouseY);
        }
    }

    private static List<Component> tooltip(Cell c) {
        List<Component> tip = new ArrayList<>();
        tip.add(DietEngine.effectName(c.effect, c.level));
        if (c.flat) {
            double at = 0;
            for (Rung r : ClientState.ladder.rungs) {
                if (r.kind == Rung.Kind.FLAT && c.effect.equals(r.id) && r.level == c.level) {
                    at = r.threshold;
                    break;
                }
            }
            tip.add(Component.translatable("solraspberrypie.widget.flat", Icons.num(at)).withStyle(ChatFormatting.GOLD));
        } else {
            double at = c.slot < ClientState.ladder.slots.size() ? ClientState.ladder.slots.get(c.slot).threshold : 0;
            tip.add(Component.translatable("solraspberrypie.widget.slot", c.slot + 1, Icons.num(at)).withStyle(ChatFormatting.AQUA));
        }
        if (ClientState.state.paused) tip.add(Component.translatable("solraspberrypie.paused").withStyle(ChatFormatting.RED));
        return tip;
    }

    public static boolean mousePressed(InventoryScreen inv, double mx, double my, int button) {
        if (!shown(inv) || button != 0) return false;
        Rect2i r = rect(inv);
        if (!r.contains((int) mx, (int) my)) return false;
        if (RpConfig.CLIENT.widgetLocked.get()) {
            EffectScreen.open();
            return true;
        }
        dragging = true;
        moved = false;
        grabX = mx - r.getX();
        grabY = my - r.getY();
        dragX = RpConfig.CLIENT.widgetX.get();
        dragY = RpConfig.CLIENT.widgetY.get();
        return true;
    }

    public static boolean mouseDragged(InventoryScreen inv, double mx, double my) {
        if (!dragging) return false;
        moved = true;
        int gl = inv.getGuiLeft(), gt = inv.getGuiTop(), iw = inv.getXSize(), ih = inv.getYSize();
        int ax = (int) Math.round(mx - grabX);
        int ay = (int) Math.round(my - grabY);
        Rect2i cur = rect(inv);
        int w = cur.getWidth(), h = cur.getHeight();
        ax = snap(ax, new int[]{gl - w, gl + iw, gl, gl + iw - w});
        ay = snap(ay, new int[]{gt - h, gt + ih, gt, gt + ih - h});
        dragX = ax - gl;
        dragY = ay - gt;
        return true;
    }

    private static int snap(int v, int[] targets) {
        int best = v, dist = SNAP + 1;
        for (int t : targets) {
            int d = Math.abs(v - t);
            if (d <= SNAP && d < dist) {
                best = t;
                dist = d;
            }
        }
        return best;
    }

    public static boolean mouseReleased() {
        if (!dragging) return false;
        dragging = false;
        if (moved) {
            RpConfig.CLIENT.widgetX.set(dragX);
            RpConfig.CLIENT.widgetY.set(dragY);
            RpConfig.CLIENT_SPEC.save();
        }
        return true;
    }

    public static void toggleLock() {
        RpConfig.CLIENT.widgetLocked.set(!RpConfig.CLIENT.widgetLocked.get());
        RpConfig.CLIENT_SPEC.save();
    }

    private InventoryWidget() {}
}
