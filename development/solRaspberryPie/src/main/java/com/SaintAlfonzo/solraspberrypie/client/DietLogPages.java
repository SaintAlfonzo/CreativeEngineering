package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import com.SaintAlfonzo.solraspberrypie.mixin.client.FoodBookScreenAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Raspberry Pie's pages in Apple Pie's Diet Log (6.3): a progress line across diversity with a marker per rung
 * (reached coloured, unreached grey, hover for the value), then the milestone list. Inserted after the diversity page.
 */
public final class DietLogPages {
    static final int HEALTH = 0xFFD03030, ATTRIBUTE = 0xFF4060C0, SLOT = 0xFF20A0A0, FLAT = 0xFFC09020, UNLOCK = 0xFF9040C0, GREY = 0xFFA0A0A0;
    private static final int ROWS_PER_PAGE = 11;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void insert(FoodBookScreen screen) {
        List pages = ((FoodBookScreenAccessor) (Object) screen).solrp$pages();
        if (pages.isEmpty() || !ClientState.ready()) return;
        Rectangle frame = ((UIElement) pages.get(0)).frame;
        List<UIElement> ours = new ArrayList<>();
        ours.add(new ProgressPage(new Rectangle(frame)));
        List<Rung> rungs = ClientState.ladder.rungs;
        for (int i = 0; i < rungs.size(); i += ROWS_PER_PAGE) {
            ours.add(new ListPage(new Rectangle(frame), rungs.subList(i, Math.min(rungs.size(), i + ROWS_PER_PAGE))));
        }
        pages.addAll(1, ours);
    }

    static boolean reached(Rung r) {
        var s = ClientState.state;
        boolean gate = s.foodsEaten >= s.minFoods;
        return r.kind == Rung.Kind.UNLOCK ? r.threshold <= s.maxDiversity : gate && r.threshold <= s.diversity;
    }

    static int color(Rung r) {
        return switch (r.kind) {
            case HEALTH -> HEALTH;
            case ATTRIBUTE -> ATTRIBUTE;
            case SLOT -> SLOT;
            case FLAT -> FLAT;
            case UNLOCK -> UNLOCK;
        };
    }

    static Component describe(Rung r) {
        return switch (r.kind) {
            case HEALTH -> SlotActions.hearts(r.amount);
            case ATTRIBUTE -> SlotActions.attribute(r.id, r.amount);
            case SLOT -> Component.translatable("solraspberrypie.slot", r.slotIndex + 1);
            case FLAT -> Component.translatable("solraspberrypie.preview.flat", DietEngine.effectName(r.id, r.level));
            case UNLOCK -> Component.translatable("solraspberrypie.dietlog.unlock", DietEngine.effectName(r.id, r.level));
        };
    }

    /** A hover target: Apple Pie's UIElement shows its tooltip string. */
    private static final class Marker extends UIElement {
        final int color;

        Marker(Rectangle frame, int color, String tooltip) {
            super(frame);
            this.color = color;
            this.tooltip = tooltip;
        }

        @Override
        protected void render(PoseStack pose) {
            GuiComponent.fill(pose, frame.x, frame.y, frame.x + frame.width, frame.y + frame.height, color);
        }
    }

    private static final class ProgressPage extends UIElement {
        ProgressPage(Rectangle frame) {
            super(frame);
            Ladder l = ClientState.ladder;
            var s = ClientState.state;
            double scale = Math.max(1, Math.max(l.maxThreshold(), s.maxDiversity));
            int x0 = frame.x + 6, x1 = frame.x + frame.width - 6;
            int barY = frame.y + 58;
            // Stack markers that share a position.
            java.util.Map<Integer, Integer> stack = new java.util.HashMap<>();
            for (Rung r : l.rungs) {
                int x = x0 + (int) Math.round((x1 - x0) * Math.min(1, r.threshold / scale));
                int n = stack.merge(x, 1, Integer::sum) - 1;
                String tip = Icons.num(r.threshold) + ": " + describe(r).getString();
                children.add(new Marker(new Rectangle(x - 2, barY - 8 - n * 6, 5, 5), reached(r) ? color(r) : GREY, tip));
            }
            int ly = barY + 22;
            String[] names = {"health", "attribute", "slot", "flat", "unlock"};
            int[] colors = {HEALTH, ATTRIBUTE, SLOT, FLAT, UNLOCK};
            for (int i = 0; i < names.length; i++) {
                children.add(new Marker(new Rectangle(frame.x + 8, ly + i * 11 + 1, 6, 6), colors[i],
                        Component.translatable("solraspberrypie.dietlog.legend." + names[i]).getString()));
            }
        }

        @Override
        protected void render(PoseStack pose) {
            var s = ClientState.state;
            Ladder l = ClientState.ladder;
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.ladder"), frame.x + 6, frame.y + 4, 0x000000);
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.now", Icons.num(Math.round(s.diversity * 10) / 10.0)), frame.x + 6, frame.y + 18, 0x333333);
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.max", Icons.num(Math.round(s.maxDiversity * 10) / 10.0)), frame.x + 6, frame.y + 28, 0x333333);
            double scale = Math.max(1, Math.max(l.maxThreshold(), s.maxDiversity));
            int x0 = frame.x + 6, x1 = frame.x + frame.width - 6, barY = frame.y + 58;
            GuiComponent.fill(pose, x0, barY, x1, barY + 4, 0xFF8A8A8A);
            int filled = x0 + (int) Math.round((x1 - x0) * Math.min(1, s.diversity / scale));
            GuiComponent.fill(pose, x0, barY, filled, barY + 4, 0xFF3FA34D);
            int maxX = x0 + (int) Math.round((x1 - x0) * Math.min(1, s.maxDiversity / scale));
            GuiComponent.fill(pose, maxX, barY - 1, maxX + 1, barY + 5, 0xFF7A3FA3);
            int ly = barY + 22;
            String[] names = {"health", "attribute", "slot", "flat", "unlock"};
            for (int i = 0; i < names.length; i++) {
                fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.legend." + names[i]), frame.x + 18, ly + i * 11, 0x333333);
            }
            if (s.foodsEaten < s.minFoods) {
                fontRenderer.draw(pose, Component.translatable("solraspberrypie.screen.gate", s.minFoods - s.foodsEaten), frame.x + 6, ly + 60, 0x8A4A00);
            }
        }
    }

    private static final class ListPage extends UIElement {
        private final List<Rung> rungs;

        ListPage(Rectangle frame, List<Rung> rungs) {
            super(frame);
            this.rungs = rungs;
        }

        @Override
        protected void render(PoseStack pose) {
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.milestones"), frame.x + 6, frame.y + 4, 0x000000);
            int y = frame.y + 18;
            for (Rung r : rungs) {
                boolean got = reached(r);
                GuiComponent.fill(pose, frame.x + 6, y + 1, frame.x + 11, y + 6, got ? color(r) : GREY);
                fontRenderer.draw(pose, Icons.num(r.threshold), frame.x + 14, y, got ? 0x1E6B2A : 0x777777);
                String text = fontRenderer.plainSubstrByWidth(describe(r).getString(), frame.width - 40);
                fontRenderer.draw(pose, text, frame.x + 34, y, got ? 0x000000 : 0x777777);
                y += 11;
            }
        }
    }

    private DietLogPages() {}
}
