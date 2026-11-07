package com.SaintAlfonzo.creativecommon.loadingtips;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The tip bar at the bottom of the world loading screens (LevelLoadingScreen: singleplayer world start;
 * ReceivingLevelScreen: joining a server and changing dimension). Tips roll through a shuffled deck, one every
 * {@code [loading_tips] cycleSeconds}; the deck carries over between loads, so no tip repeats until all have shown.
 */
@Mod.EventBusSubscriber(modid = CreativeCommon.MODID, value = Dist.CLIENT)
public final class LoadingTipBar {
    private static final Component LABEL = Component.translatable("creativecommon.loading_tips.label");
    private static final int LABEL_COLOR = 0x3AB35A; // Loading Screen Tips' green
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BAR_COLOR = 0x90000000;
    private static final int MAX_WIDTH = 360;
    private static final int PAD = 4;

    private static final List<String> deck = new ArrayList<>();
    private static int next;
    private static Screen screen;
    private static String current;
    private static long shownAt;

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

    private static boolean isLoadingScreen(Screen s) {
        return s instanceof LevelLoadingScreen || s instanceof ReceivingLevelScreen;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen s = event.getScreen();
        if (!isLoadingScreen(s) || !CommonConfig.LOADING_TIPS.enabled.get()) return;
        long now = System.currentTimeMillis();
        long cycle = CommonConfig.LOADING_TIPS.cycleSeconds.get() * 1000L;
        if (s != screen || current == null || now - shownAt >= cycle) {
            screen = s;
            current = nextTip();
            shownAt = now;
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
