package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.net.C2SAction;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CPreview;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The effect screen (11.2): flat bonuses (read only), then the slots in order. Active slots: fill (free, lands in the
 * lowest empty active slot), level change (free), change effect and drop (paid, through the confirm panel). Greyed
 * slots show their stored choice and are read only. While paused, every action is disabled and PAUSED shows.
 */
public class EffectScreen extends Screen {
    private static final int W = 276;
    private static final int ROW = 24;

    private enum Mode { SLOTS, PICK_FILL, PICK_CHANGE, CONFIRM }

    private Mode mode = Mode.SLOTS;
    private int pickSlot = -1;
    private S2CPreview preview;
    private int left, top, height0;
    private final List<Runnable> hoverTips = new ArrayList<>();

    public EffectScreen() {
        super(Component.translatable("solraspberrypie.screen.title"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new EffectScreen());
    }

    public void refresh() {
        if (mode == Mode.CONFIRM) return; // the panel waits for the server's reply or the player's choice
        rebuild();
    }

    public void showPreview(S2CPreview p) {
        preview = p;
        mode = Mode.CONFIRM;
        rebuild();
    }

    @Override
    protected void init() {
        rebuild();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void rebuild() {
        clearWidgets();
        if (!ClientState.ready()) return;
        Ladder ladder = ClientState.ladder;
        S2CState s = ClientState.state;
        height0 = Math.max(210, 70 + Math.max(1, ladder.slotCount()) * ROW + 40);
        left = (width - W) / 2;
        top = Math.max(8, (height - height0) / 2);
        switch (mode) {
            case SLOTS -> buildSlots(ladder, s);
            case PICK_FILL, PICK_CHANGE -> buildPicker(ladder, s);
            case CONFIRM -> buildConfirm(s);
        }
        addRenderableWidget(new Button(left + W - 64, top + height0 - 26, 56, 20, Component.translatable("gui.done"), b -> onClose()));
    }

    private int slotsTop() {
        return top + 66;
    }

    private void buildSlots(Ladder ladder, S2CState s) {
        boolean locked = s.paused;
        for (int i = 0; i < ladder.slotCount(); i++) {
            int y = slotsTop() + i * ROW;
            boolean active = i < s.activeSlots;
            SlotChoice c = i < s.slots.size() ? s.slots.get(i) : null;
            if (!active || locked) continue;
            final int slot = i;
            int bx = left + W - 120;
            if (c == null) {
                addRenderableWidget(new Button(bx + 54, y + 2, 58, 18, Component.translatable("solraspberrypie.screen.choose"), b -> {
                    mode = Mode.PICK_FILL;
                    pickSlot = slot;
                    rebuild();
                }));
                continue;
            }
            int max = Math.max(ladder.unlockedLevel(c.effect(), s.maxDiversity), c.level());
            Button minus = new Button(bx - 22, y + 2, 18, 18, Component.literal("-"), b -> send(C2SAction.Op.LEVEL, slot, c.effect(), c.level() - 1));
            minus.active = c.level() > 1;
            Button plus = new Button(bx - 2, y + 2, 18, 18, Component.literal("+"), b -> send(C2SAction.Op.LEVEL, slot, c.effect(), c.level() + 1));
            plus.active = c.level() < max && c.level() < ladder.unlockedLevel(c.effect(), s.maxDiversity);
            addRenderableWidget(minus);
            addRenderableWidget(plus);
            addRenderableWidget(new Button(bx + 20, y + 2, 48, 18, Component.translatable("solraspberrypie.screen.change"), b -> {
                mode = Mode.PICK_CHANGE;
                pickSlot = slot;
                rebuild();
            }));
            addRenderableWidget(new Button(bx + 70, y + 2, 42, 18, Component.translatable("solraspberrypie.screen.drop"),
                    b -> send(C2SAction.Op.PREVIEW_DROP, slot, null, 0)));
        }
    }

    private List<ResourceLocation> pickable(Ladder ladder, S2CState s) {
        List<ResourceLocation> out = new ArrayList<>();
        for (ResourceLocation id : ladder.pool.keySet()) {
            if (ladder.unlockedLevel(id, s.maxDiversity) < 1 || s.flat.containsKey(id)) continue;
            boolean used = false;
            for (SlotChoice c : s.slots) if (c != null && c.effect().equals(id)) used = true;
            if (!used && ForgeRegistries.MOB_EFFECTS.containsKey(id)) out.add(id);
        }
        return out;
    }

    private int gridX(int i) {
        return left + 12 + (i % 11) * 23;
    }

    private int gridY(int i) {
        return slotsTop() + 14 + (i / 11) * 23;
    }

    private void buildPicker(Ladder ladder, S2CState s) {
        List<ResourceLocation> ids = pickable(ladder, s);
        for (int i = 0; i < ids.size(); i++) {
            ResourceLocation id = ids.get(i);
            int lvl = ladder.unlockedLevel(id, s.maxDiversity);
            Button b = new Button(gridX(i), gridY(i), 22, 22, Component.empty(), btn -> {
                if (mode == Mode.PICK_FILL) {
                    send(C2SAction.Op.FILL, pickSlot, id, lvl);
                    mode = Mode.SLOTS;
                } else {
                    send(C2SAction.Op.PREVIEW_CHANGE, pickSlot, id, lvl);
                }
            });
            addRenderableWidget(b);
        }
        addRenderableWidget(new Button(left + 8, top + height0 - 26, 60, 20, Component.translatable("gui.cancel"), b -> {
            mode = Mode.SLOTS;
            rebuild();
        }));
    }

    private void buildConfirm(S2CState s) {
        Button ok = new Button(left + 8, top + height0 - 26, 70, 20, Component.translatable("solraspberrypie.screen.confirm"), b -> {
            C2SAction.Op op = preview.op == C2SAction.Op.PREVIEW_CHANGE ? C2SAction.Op.CONFIRM_CHANGE : C2SAction.Op.CONFIRM_DROP;
            RpNetwork.toServer(new C2SAction(op, preview.slot, preview.effect, preview.level, preview.stamp));
            preview = null;
            mode = Mode.SLOTS;
            rebuild();
        });
        ok.active = !s.paused;
        addRenderableWidget(ok);
        addRenderableWidget(new Button(left + 82, top + height0 - 26, 60, 20, Component.translatable("gui.cancel"), b -> {
            preview = null;
            mode = Mode.SLOTS;
            rebuild();
        }));
    }

    private void send(C2SAction.Op op, int slot, ResourceLocation effect, int level) {
        RpNetwork.toServer(new C2SAction(op, slot, effect, level, ClientState.state.stamp));
    }

    // ---- drawing ---------------------------------------------------------------------------------------------------

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
        renderBackground(pose);
        hoverTips.clear();
        if (!ClientState.ready()) {
            drawCenteredString(pose, font, Component.translatable("solraspberrypie.screen.waiting"), width / 2, height / 2, 0xFFFFFF);
            super.render(pose, mouseX, mouseY, partial);
            return;
        }
        Ladder ladder = ClientState.ladder;
        S2CState s = ClientState.state;
        fill(pose, left - 1, top - 1, left + W + 1, top + height0 + 1, 0xFF6B6B6B);
        fill(pose, left, top, left + W, top + height0, 0xFF202020);
        drawString(pose, font, title, left + 8, top + 8, 0xFFFFFF);
        Component div = Component.translatable("solraspberrypie.screen.diversity", Icons.num(round(s.diversity)), Icons.num(round(s.maxDiversity)));
        drawString(pose, font, div, left + 8, top + 20, 0xAAAAAA);
        if (s.foodsEaten < s.minFoods) {
            drawString(pose, font, Component.translatable(s.uniqueGate ? "solraspberrypie.screen.gate_unique" : "solraspberrypie.screen.gate", s.minFoods - s.foodsEaten), left + 8, top + 31, 0xFFAA55);
        }
        // Flat bonuses (read only).
        drawString(pose, font, Component.translatable("solraspberrypie.screen.flat"), left + 8, top + 44, 0xFFD27F);
        int fx = left + 90;
        if (s.flat.isEmpty()) drawString(pose, font, Component.translatable("solraspberrypie.screen.none"), fx, top + 44, 0x777777);
        for (Map.Entry<ResourceLocation, Integer> e : s.flat.entrySet()) {
            fill(pose, fx - 1, top + 39, fx + 19, top + 59, 0xFF8A6D1E);
            Icons.effect(pose, e.getKey(), fx, top + 40);
            if (in(mouseX, mouseY, fx, top + 40, 18, 18)) {
                Component name = DietEngine.effectName(e.getKey(), e.getValue());
                hoverTips.add(() -> renderComponentTooltip(pose, List.of(name, Component.translatable("solraspberrypie.screen.flat_tip").withStyle(ChatFormatting.GOLD)), mouseX, mouseY));
            }
            fx += 22;
        }
        switch (mode) {
            case SLOTS -> drawSlots(pose, ladder, s, mouseX, mouseY);
            case PICK_FILL, PICK_CHANGE -> drawPicker(pose, ladder, s, mouseX, mouseY);
            case CONFIRM -> drawConfirm(pose, mouseX, mouseY);
        }
        super.render(pose, mouseX, mouseY, partial);
        for (Runnable r : hoverTips) r.run();
    }

    private void drawSlots(PoseStack pose, Ladder ladder, S2CState s, int mouseX, int mouseY) {
        if (s.paused) {
            drawString(pose, font, Component.translatable("solraspberrypie.paused").withStyle(ChatFormatting.BOLD), left + W - 60, top + 8, 0xFF5555);
        }
        if (ladder.slotCount() == 0) {
            drawString(pose, font, Component.translatable("solraspberrypie.screen.no_slots"), left + 8, slotsTop() + 4, 0x777777);
        }
        for (int i = 0; i < ladder.slotCount(); i++) {
            int y = slotsTop() + i * ROW;
            boolean active = i < s.activeSlots;
            SlotChoice c = i < s.slots.size() ? s.slots.get(i) : null;
            fill(pose, left + 6, y, left + W - 6, y + ROW - 2, active ? 0xFF2E3A2E : 0xFF2A2A2A);
            fill(pose, left + 9, y + 2, left + 29, y + 22, 0xFF111111);
            if (c != null) {
                if (!active || s.paused) RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, 1F);
                Icons.effect(pose, c.effect(), left + 10, y + 3);
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            }
            Component name = c != null ? DietEngine.effectName(c.effect(), c.level()) : Component.translatable("solraspberrypie.screen.empty");
            boolean covered = c != null && active && s.flat.containsKey(c.effect());
            int color = !active ? 0x777777 : covered ? 0xFFFF55 : 0xFFFFFF;
            drawString(pose, font, name, left + 34, y + 3, color);
            Rung r = ladder.slots.get(i);
            Component sub = active ? (covered ? Component.translatable("solraspberrypie.screen.covered")
                    : Component.translatable("solraspberrypie.widget.slot", i + 1, Icons.num(r.threshold)))
                    : Component.translatable("solraspberrypie.screen.greyed", i + 1, Icons.num(r.threshold));
            drawString(pose, font, sub, left + 34, y + 13, active ? 0x88AA88 : 0x666666);
        }
    }

    private void drawPicker(PoseStack pose, Ladder ladder, S2CState s, int mouseX, int mouseY) {
        Component head = Component.translatable(mode == Mode.PICK_FILL ? "solraspberrypie.screen.pick_fill" : "solraspberrypie.screen.pick_change", pickSlot + 1);
        drawString(pose, font, head, left + 8, slotsTop(), 0xFFFFFF);
        List<ResourceLocation> ids = pickable(ladder, s);
        if (ids.isEmpty()) drawString(pose, font, Component.translatable("solraspberrypie.screen.none_unlocked"), left + 12, slotsTop() + 18, 0x777777);
        // Icons are drawn after the buttons (super.render), so queue them as overlays.
        for (int i = 0; i < ids.size(); i++) {
            ResourceLocation id = ids.get(i);
            int x = gridX(i), y = gridY(i);
            int lvl = ladder.unlockedLevel(id, s.maxDiversity);
            hoverTips.add(0, () -> Icons.effect(pose, id, x + 2, y + 2));
            if (in(mouseX, mouseY, x, y, 22, 22)) {
                List<Component> tip = new ArrayList<>();
                tip.add(DietEngine.effectName(id, lvl));
                if (mode == Mode.PICK_CHANGE) tip.add(Component.translatable("solraspberrypie.screen.change_cost").withStyle(ChatFormatting.RED));
                else tip.add(Component.translatable("solraspberrypie.screen.free").withStyle(ChatFormatting.GREEN));
                hoverTips.add(() -> renderComponentTooltip(pose, tip, mouseX, mouseY));
            }
        }
    }

    private void drawConfirm(PoseStack pose, int mouseX, int mouseY) {
        if (preview == null) return;
        int y = slotsTop();
        Component head = preview.op == C2SAction.Op.PREVIEW_CHANGE
                ? Component.translatable("solraspberrypie.confirm.change", preview.slot + 1, DietEngine.effectName(preview.effect, preview.level))
                : Component.translatable("solraspberrypie.confirm.drop", preview.slot + 1);
        drawString(pose, font, head, left + 8, y, 0xFFFFFF);
        y += 12;
        if (preview.stale) {
            drawString(pose, font, Component.translatable("solraspberrypie.confirm.stale"), left + 8, y, 0xFFFF55);
            y += 12;
        }
        drawString(pose, font, Component.translatable("solraspberrypie.confirm.diversity", Icons.num(round(preview.before)), Icons.num(round(preview.after))), left + 8, y, 0xAAAAAA);
        y += 12;
        drawString(pose, font, Component.translatable("solraspberrypie.confirm.aged", preview.aged.size()), left + 8, y, 0xAAAAAA);
        y += 10;
        int x = left + 8;
        for (ResourceLocation id : preview.aged) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) continue;
            ItemStack stack = new ItemStack(item);
            itemRenderer.renderAndDecorateItem(stack, x, y);
            if (in(mouseX, mouseY, x, y, 16, 16)) hoverTips.add(() -> renderTooltip(pose, stack, mouseX, mouseY));
            x += 17;
            if (x > left + W - 24) {
                x = left + 8;
                y += 17;
            }
        }
        y += 20;
        if (preview.slotGreyedAfter) {
            drawString(pose, font, Component.translatable("solraspberrypie.confirm.greyed"), left + 8, y, 0xFFFF55);
            y += 12;
        }
        drawString(pose, font, Component.translatable(preview.lines.isEmpty() ? "solraspberrypie.confirm.nothing_lost" : "solraspberrypie.confirm.lost"), left + 8, y, 0xFFFFFF);
        y += 11;
        for (int i = 0; i < preview.lines.size() && y < top + height0 - 30; i++) {
            drawString(pose, font, preview.lines.get(i), left + 14, y, preview.yellow.get(i) ? 0xFFFF55 : 0xFF5555);
            y += 10;
        }
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
