package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import com.SaintAlfonzo.solraspberrypie.mixin.client.FoodBookScreenAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.client.gui.BenefitsPage;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.FoodListPage;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Raspberry Pie's pages in Apple Pie's Diet Log (6.3, 12.4): a progress bar over a window of the ladder (the last
 * milestone reached, a short stretch before it and the next few milestones) with an icon per rung (reached in colour,
 * unreached greyed, hover for the value), then the milestone list. Inserted after the diversity page; Apple Pie's own
 * benefit pages are removed. With the experimental double page view the food pages split into foods in the window and
 * foods out of it, and the first page out of the window is drawn on a second book page to the right.
 */
public final class DietLogPages {
    static final int HEALTH = 0xFFD03030, ATTRIBUTE = 0xFF4060C0, SLOT = 0xFF20A0A0, FLAT = 0xFFC09020, UNLOCK = 0xFF9040C0, GREY = 0xFFA0A0A0;
    private static final int ROWS_PER_PAGE = 11;
    private static final int ICON = 9;
    /** Apple Pie's book: page size and the gap between the two books of the double page view. */
    public static final int BOOK_W = 186, BOOK_H = 192, GAP = 4;
    public static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("solapplepie", "textures/gui/food_book.png");

    /** True when the double page view is on and the screen is wide enough for two books. */
    public static boolean doublePage(int screenWidth) {
        return RpConfig.CLIENT_SPEC.isLoaded() && RpConfig.CLIENT.doublePage.get() && screenWidth >= 2 * BOOK_W + GAP + 8;
    }

    /**
     * Rebuilds the page list after Apple Pie's initPages. Returns the page to draw on the right-hand book in the double
     * page view, or null.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static UIElement insert(FoodBookScreen screen, int screenWidth) {
        FoodBookScreenAccessor acc = (FoodBookScreenAccessor) (Object) screen;
        List pages = acc.solrp$pages();
        if (pages.isEmpty()) return null;
        Rectangle frame = ((UIElement) pages.get(0)).frame;
        pages.removeIf(p -> p instanceof BenefitsPage);

        UIElement companion = null;
        if (doublePage(screenWidth)) companion = splitFoodPages(pages, frame, acc.solrp$foodData());

        if (!ClientState.ready()) return companion;
        List<UIElement> ours = new ArrayList<>();
        ours.add(new ProgressPage(new Rectangle(frame)));
        List<Rung> rungs = ClientState.ladder.rungs;
        for (int i = 0; i < rungs.size(); i += ROWS_PER_PAGE) {
            ours.add(new ListPage(new Rectangle(frame), rungs.subList(i, Math.min(rungs.size(), i + ROWS_PER_PAGE))));
        }
        pages.addAll(Math.min(1, pages.size()), ours);
        return companion;
    }

    // ---- double page view (#31) ------------------------------------------------------------------------------------

    private static Method foodPages;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<UIElement> foodListPages(Rectangle frame, String header, List<ItemStack> stacks, Map<FoodInstance, Integer> map) {
        try {
            if (foodPages == null) {
                foodPages = FoodListPage.class.getDeclaredMethod("pages", Rectangle.class, String.class, List.class, Map.class);
                foodPages.setAccessible(true);
            }
            return (List<UIElement>) foodPages.invoke(null, frame, header, stacks, map);
        } catch (ReflectiveOperationException e) {
            return List.of();
        }
    }

    /** Replaces Apple Pie's single food queue with in-window pages, then out-of-window pages 2+; returns out-of-window page 1. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static UIElement splitFoodPages(List pages, Rectangle frame, java.util.Set<Map.Entry<FoodInstance, Integer>> data) {
        if (data == null) return null;
        int at = -1;
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i) instanceof FoodListPage) {
                at = i;
                break;
            }
        }
        if (at < 0) return null;
        pages.removeIf(p -> p instanceof FoodListPage);

        List<Map.Entry<FoodInstance, Integer>> sorted = new ArrayList<>(data);
        sorted.sort(Map.Entry.comparingByValue());
        Map<FoodInstance, Integer> map = new HashMap<>();
        List<ItemStack> in = new ArrayList<>(), out = new ArrayList<>();
        int end = SOLSweetPotatoConfig.endDecay();
        for (Map.Entry<FoodInstance, Integer> e : sorted) {
            map.put(e.getKey(), e.getValue());
            if (e.getKey().getItem() == null) continue;
            (e.getValue() < end ? in : out).add(new ItemStack(e.getKey().getItem()));
        }
        List<UIElement> inPages = foodListPages(frame,
                Component.translatable("solraspberrypie.dietlog.window_in", in.size()).getString(), in, map);
        Rectangle right = new Rectangle(frame);
        right.translate(BOOK_W + GAP, 0);
        List<UIElement> outRight = foodListPages(right,
                Component.translatable("solraspberrypie.dietlog.window_out", out.size()).getString(), out, map);
        List<UIElement> outPages = foodListPages(frame,
                Component.translatable("solraspberrypie.dietlog.window_out", out.size()).getString(), out, map);
        List added = new ArrayList<>(inPages);
        if (outPages.size() > 1) added.addAll(outPages.subList(1, outPages.size()));
        pages.addAll(Math.min(at, pages.size()), added);
        return outRight.isEmpty() ? null : outRight.get(0);
    }

    /** The right-hand book of the double page view, with its page. */
    public static void renderCompanion(PoseStack pose, UIElement page, Rectangle left, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, BOOK_TEXTURE);
        GuiComponent.blit(pose, left.x + BOOK_W + GAP, left.y, 0, 0, BOOK_W, BOOK_H, 256, 256);
        if (page != null) UIElement.render(pose, page, mouseX, mouseY);
    }

    // ---- rungs -----------------------------------------------------------------------------------------------------

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

    private static ItemStack attributeItem(ResourceLocation id) {
        String path = id == null ? "" : id.getPath();
        if (path.endsWith("armor_toughness")) return new ItemStack(Items.NETHERITE_CHESTPLATE);
        if (path.endsWith("knockback_resistance")) return new ItemStack(Items.SHIELD);
        if (path.endsWith("movement_speed")) return new ItemStack(Items.FEATHER);
        if (path.endsWith("attack_damage")) return new ItemStack(Items.IRON_SWORD);
        if (path.endsWith("attack_speed")) return new ItemStack(Items.GOLDEN_SWORD);
        if (path.endsWith("luck")) return new ItemStack(Items.RABBIT_FOOT);
        return ItemStack.EMPTY;
    }

    /** The rung's icon at {@code (x, y)}, {@link #ICON} pixels: kind-coloured frame, greyed when not reached. */
    static void drawIcon(PoseStack pose, Rung r, int x, int y, boolean got) {
        switch (r.kind) {
            case HEALTH -> Icons.heart(pose, x, y);
            case ATTRIBUTE -> {
                ItemStack stack = attributeItem(r.id);
                if (stack.isEmpty()) Icons.armor(pose, x, y);
                else Icons.item(stack, x, y, ICON);
            }
            case SLOT -> {
                SlotChoice c = ClientState.state.slots.size() > r.slotIndex && r.slotIndex >= 0 ? ClientState.state.slots.get(r.slotIndex) : null;
                GuiComponent.fill(pose, x, y, x + ICON, y + ICON, 0x40000000);
                if (c != null) Icons.effect(pose, c.effect(), x, y, ICON);
            }
            case FLAT, UNLOCK -> Icons.effect(pose, r.id, x, y, ICON);
        }
        Icons.frame(pose, x, y, ICON, got ? color(r) : GREY);
        if (!got) Icons.dim(pose, x, y, ICON);
    }

    /** A legend sample for a kind (a heart, a toughness chestplate, a slot box, Speed, Haste). */
    private static Rung sample(Rung.Kind kind) {
        return switch (kind) {
            case HEALTH -> new Rung(kind, 0, null, 2, 0);
            case ATTRIBUTE -> new Rung(kind, 0, new ResourceLocation("minecraft", "generic.armor_toughness"), 1, 0);
            case SLOT -> new Rung(kind, 0, null, 0, 0);
            case FLAT -> new Rung(kind, 0, new ResourceLocation("minecraft", "speed"), 0, 1);
            case UNLOCK -> new Rung(kind, 0, new ResourceLocation("minecraft", "haste"), 0, 1);
        };
    }

    /** A hover target over an icon: Apple Pie's UIElement shows its tooltip string. Draws nothing itself. */
    private static final class Hover extends UIElement {
        Hover(Rectangle frame, String tooltip) {
            super(frame);
            this.tooltip = tooltip;
        }

        @Override
        protected void render(PoseStack pose) {}
    }

    /** The bar's window over the ladder: values from {@code lo} to {@code hi}. */
    record Window(double lo, double hi, boolean before, boolean after) {
        static Window of(Ladder l, double diversity, int ahead) {
            TreeSet<Double> t = new TreeSet<>();
            for (Rung r : l.rungs) t.add(r.threshold);
            List<Double> ts = new ArrayList<>(t);
            if (ts.isEmpty()) return new Window(0, Math.max(1, diversity), false, false);
            int last = -1;
            for (int i = 0; i < ts.size(); i++) if (ts.get(i) <= diversity) last = i;
            double lo;
            if (last < 0) {
                lo = 0;
            } else {
                double prev = last > 0 ? ts.get(last - 1) : 0;
                lo = Math.max(0, ts.get(last) - (ts.get(last) - prev) * 0.35);
            }
            int far = Math.min(ts.size() - 1, last + ahead);
            double hi = ts.get(far);
            if (hi <= lo || far == last) hi = Math.max(hi, diversity) + Math.max(1, (hi - lo) * 0.15);
            return new Window(lo, hi, lo > 0, far < ts.size() - 1);
        }

        int x(double v, int x0, int x1) {
            return x0 + (int) Math.round((x1 - x0) * Math.max(0, Math.min(1, (v - lo) / (hi - lo))));
        }

        boolean contains(double v) {
            return v >= lo - 1e-9 && v <= hi + 1e-9;
        }
    }

    private static final class ProgressPage extends UIElement {
        private static final int BAR_DY = 66, LEGEND_DY = 86;
        private static final Rung.Kind[] KINDS = {Rung.Kind.HEALTH, Rung.Kind.ATTRIBUTE, Rung.Kind.SLOT, Rung.Kind.FLAT, Rung.Kind.UNLOCK};
        private static final String[] NAMES = {"health", "attribute", "slot", "flat", "unlock"};
        private final Window window;
        private final List<Rung> shown = new ArrayList<>();
        private final List<int[]> spots = new ArrayList<>();

        ProgressPage(Rectangle frame) {
            super(frame);
            Ladder l = ClientState.ladder;
            var s = ClientState.state;
            window = Window.of(l, s.diversity, RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.progressAhead.get() : 3);
            int x0 = frame.x + 10, x1 = frame.x + frame.width - 10;
            int barY = frame.y + BAR_DY;
            Map<Integer, Integer> stack = new HashMap<>();
            for (Rung r : l.rungs) {
                if (!window.contains(r.threshold)) continue;
                int x = window.x(r.threshold, x0, x1);
                int n = stack.merge(x, 1, Integer::sum) - 1;
                int ix = x - ICON / 2, iy = barY - 3 - ICON - n * (ICON + 2);
                shown.add(r);
                spots.add(new int[]{ix, iy});
                children.add(new Hover(new Rectangle(ix, iy, ICON, ICON), Icons.num(r.threshold) + ": " + describe(r).getString()));
            }
            int ly = frame.y + LEGEND_DY;
            for (int i = 0; i < NAMES.length; i++) {
                children.add(new Hover(new Rectangle(frame.x + 8, ly + i * 11, ICON, ICON),
                        Component.translatable("solraspberrypie.dietlog.legend." + NAMES[i]).getString()));
            }
        }

        @Override
        protected void render(PoseStack pose) {
            var s = ClientState.state;
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.ladder"), frame.x + 6, frame.y + 4, 0x000000);
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.now", Icons.num(Math.round(s.diversity * 10) / 10.0)), frame.x + 6, frame.y + 16, 0x333333);
            fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.max", Icons.num(Math.round(s.maxDiversity * 10) / 10.0)), frame.x + 6, frame.y + 26, 0x333333);

            int x0 = frame.x + 10, x1 = frame.x + frame.width - 10, barY = frame.y + BAR_DY;
            GuiComponent.fill(pose, x0, barY, x1, barY + 4, 0xFF8A8A8A);
            int filled = window.x(s.diversity, x0, x1);
            GuiComponent.fill(pose, x0, barY, filled, barY + 4, 0xFF3FA34D);
            if (window.contains(s.maxDiversity)) {
                int maxX = window.x(s.maxDiversity, x0, x1);
                GuiComponent.fill(pose, maxX, barY - 1, maxX + 1, barY + 5, 0xFF7A3FA3);
            } else if (s.maxDiversity > window.hi()) {
                fontRenderer.draw(pose, ">", x1 + 2, barY - 2, 0x7A3FA3);
            }
            if (window.before()) fontRenderer.draw(pose, "<", x0 - 7, barY - 2, 0x777777);
            fontRenderer.draw(pose, Icons.num(Math.round(window.lo() * 10) / 10.0), x0, barY + 7, 0x777777);
            String hi = Icons.num(Math.round(window.hi() * 10) / 10.0) + (window.after() ? " ..." : "");
            fontRenderer.draw(pose, hi, x1 - fontRenderer.width(hi), barY + 7, 0x777777);

            for (int i = 0; i < shown.size(); i++) {
                Rung r = shown.get(i);
                drawIcon(pose, r, spots.get(i)[0], spots.get(i)[1], reached(r));
            }

            int ly = frame.y + LEGEND_DY;
            for (int i = 0; i < NAMES.length; i++) {
                drawIcon(pose, sample(KINDS[i]), frame.x + 8, ly + i * 11, true);
                fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.legend." + NAMES[i]), frame.x + 22, ly + i * 11 + 1, 0x333333);
            }
            if (s.foodsEaten < s.minFoods) {
                fontRenderer.draw(pose, Component.translatable(s.uniqueGate ? "solraspberrypie.screen.gate_unique" : "solraspberrypie.screen.gate",
                        s.minFoods - s.foodsEaten), frame.x + 6, ly + 57, 0x8A4A00);
            }
            super.render(pose);
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
                drawIcon(pose, r, frame.x + 7, y, got);
                fontRenderer.draw(pose, Icons.num(r.threshold), frame.x + 20, y + 1, got ? 0x1E6B2A : 0x777777);
                String text = fontRenderer.plainSubstrByWidth(describe(r).getString(), frame.width - 46);
                fontRenderer.draw(pose, text, frame.x + 40, y + 1, got ? 0x000000 : 0x777777);
                y += 12;
            }
            super.render(pose);
        }
    }

    private DietLogPages() {}
}
