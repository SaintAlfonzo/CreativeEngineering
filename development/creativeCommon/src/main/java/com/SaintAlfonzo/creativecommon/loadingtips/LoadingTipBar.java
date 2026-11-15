package com.SaintAlfonzo.creativecommon.loadingtips;

import com.SaintAlfonzo.creativecommon.CommonClientConfig;
import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The tip bar at the bottom of every loading screen between world / server selection and join
 * (feature_writeup/loading_tips.md section 1; inventory in plan_loading_tips_screens.md section 3). Screens that start
 * or continue a join show it at once; ProgressScreen and the other dirt screens are shared with leaving a world and
 * deleting one, so they show it only inside a join (a loading screen shown less than {@link #JOIN_GAP_MS} ago).
 * One join keeps one tip: tips roll only on the {@code [loading_tips] cycleSeconds} timer, through a shuffled deck that
 * carries over between loads, so no tip repeats until all have shown.
 */
@Mod.EventBusSubscriber(modid = CreativeCommon.MODID, value = Dist.CLIENT)
public final class LoadingTipBar {
    private static final Component LABEL = Component.translatable("creativecommon.loading_tips.label");
    private static final int LABEL_COLOR = 0x3AB35A; // Loading Screen Tips' green
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BAR_COLOR = 0x90000000;
    private static final int MAX_WIDTH = 360;
    private static final int PAD = 4;

    private static final Logger LOGGER = LogUtils.getLogger();
    /** Gap after which the next loading screen counts as a new join (new tip). */
    private static final long JOIN_GAP_MS = 5000;
    /** Dirt-screen titles that open a join: reading a world, creating one, validating its datapacks. */
    private static final Set<String> JOIN_DIRT_TITLES =
            Set.of("selectWorld.data_read", "createWorld.preparing", "dataPack.validation.working");
    /** Dirt-screen titles that never get the bar (leaving a world). */
    private static final Set<String> NO_BAR_DIRT_TITLES = Set.of("menu.savingLevel");

    private static final List<String> deck = new ArrayList<>();
    private static int next;
    private static Class<?> lastScreen;
    private static String current;
    private static long shownAt;
    private static long lastLoadingAt;

    private LoadingTipBar() {}

    /** The tip list changed (resource reload): start a fresh deck. */
    static synchronized void reset() {
        deck.clear();
        next = 0;
        current = null;
    }

    private static synchronized String nextTip() {
        List<String> tips = LoadingTips.tips();
        if (tips.isEmpty()) return null;
        if (next >= deck.size()) {
            String last = current;
            deck.clear();
            deck.addAll(tips);
            Collections.shuffle(deck);
            // Never show the same tip twice in a row across a reshuffle.
            if (deck.size() > 1 && deck.get(0).equals(last)) Collections.swap(deck, 0, deck.size() - 1);
            next = 0;
        }
        return deck.get(next++);
    }

    private static String titleKey(Screen s) {
        return s.getTitle().getContents() instanceof TranslatableContents t ? t.getKey() : "";
    }

    /** The allowlist. A screen that starts a join shows the bar at any time. */
    private static boolean startsJoin(Screen s) {
        return s instanceof ConnectScreen || s instanceof LevelLoadingScreen || s instanceof ReceivingLevelScreen
                || s instanceof GenericDirtMessageScreen && JOIN_DIRT_TITLES.contains(titleKey(s));
    }

    /** Screens that also show outside a join, so they get the bar only inside one. */
    private static boolean continuesJoin(Screen s) {
        return s instanceof ProgressScreen
                || s instanceof GenericDirtMessageScreen && !NO_BAR_DIRT_TITLES.contains(titleKey(s));
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen s = event.getScreen();
        long now = System.currentTimeMillis();
        boolean inJoin = now - lastLoadingAt < JOIN_GAP_MS;
        if (!(startsJoin(s) || inJoin && continuesJoin(s)) || !CommonClientConfig.LOADING_TIPS.enabled.get()) return;
        long cycle = CommonClientConfig.LOADING_TIPS.cycleSeconds.get() * 1000L;
        if (!inJoin || current == null || now - shownAt >= cycle) {
            current = nextTip();
            shownAt = now;
        }
        lastLoadingAt = now;
        if (s.getClass() != lastScreen) {
            lastScreen = s.getClass();
            LOGGER.info("Loading tips: bar on {} ({})", lastScreen.getSimpleName(), titleKey(s));
        }
        if (current == null) return;

        Font font = Minecraft.getInstance().font;
        Component text = I18n.exists(current) ? Component.translatable(current) : Component.literal(current);
        int width = Math.min(MAX_WIDTH, s.width - 40);
        int labelWidth = font.width(LABEL) + 4;
        List<FormattedCharSequence> lines = font.split(text, width - labelWidth);
        int barHeight = lines.size() * font.lineHeight + PAD * 2;
        int left = (s.width - width) / 2 - PAD;
        int top = s.height - barHeight - 12;
        // Never cover a button (the connect screen's Cancel): sit below the lowest widget, or skip the frame.
        int widgetBottom = 0;
        for (GuiEventListener c : s.children()) {
            if (c instanceof AbstractWidget w && w.visible) widgetBottom = Math.max(widgetBottom, w.y + w.getHeight());
        }
        if (top < widgetBottom + 4) {
            top = widgetBottom + 4;
            if (top + barHeight > s.height) return;
        }

        PoseStack pose = event.getPoseStack();
        GuiComponent.fill(pose, left, top, left + width + PAD * 2, top + barHeight, BAR_COLOR);
        int y = top + PAD;
        font.drawShadow(pose, LABEL, left + PAD, y, LABEL_COLOR);
        for (FormattedCharSequence line : lines) {
            font.drawShadow(pose, line, left + PAD + labelWidth, y, TEXT_COLOR);
            y += font.lineHeight;
        }
    }
}
