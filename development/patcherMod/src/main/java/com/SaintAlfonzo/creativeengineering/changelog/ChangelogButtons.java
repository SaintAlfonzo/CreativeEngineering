package com.SaintAlfonzo.creativeengineering.changelog;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

/**
 * The book button on the title screen and the pause menu, and the Quark-style update bubble beside it
 * (feature_writeup/changelog.md 3.3, CEConfig {@code [changelog]}).
 *
 * <p>Spot: right of Singleplayer on the title screen (vanilla leaves it empty and Quark's Q button sits by
 * Mods / Realms / Options), else right of Multiplayer, else left of Singleplayer; right of Back to Game on the
 * pause menu, else left of it. A spot overlapping another widget or off screen is skipped. Buttons are found
 * by their translation key, so moved vanilla buttons still work.
 *
 * <p>Bubble: while the newest version is above the read marker, "New in X!" bobs one pixel every half second
 * (Quark's motion) beside the button, drawn after the screen. It points at the button from whichever side has room.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class ChangelogButtons {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TOOLTIP = Component.translatable("creativeengineering.changelog.button");

    // Bubble pixel art after Quark's chat bubble: # outline, W highlight, F fill, S shade, . clear. The body is
    // 14 rows; the tail piece hangs 3 rows lower towards the button.
    private static final String[] TAIL_LEFT = {
            "..####", ".#WWWW", "#WFFFF", "#WFFFF", "#WFFFF", "#WFFFF", "#WFFFF", "#WFFFF", "#WFFFF", "#WFFFF",
            "#WFFFF", "#WFFFF", "#WFFSS", "#WFS##", "#WS#..", "#S#...", "##...."};
    private static final String[] END_RIGHT = {
            "###..", "WWW#.", "FFFS#", "FFFS#", "FFFS#", "FFFS#", "FFFS#", "FFFS#", "FFFS#", "FFFS#",
            "FFFS#", "FFFS#", "SSS#.", "###.."};
    private static final String[] END_LEFT = {
            "..###", ".#WWW", "#WFFF", "#WFFF", "#WFFF", "#WFFF", "#WFFF", "#WFFF", "#WFFF", "#WFFF",
            "#WFFF", "#WFFF", ".#SSS", "..###"};
    private static final String[] TAIL_RIGHT = {
            "####..", "WWWW#.", "FFFFS#", "FFFFS#", "FFFFS#", "FFFFS#", "FFFFS#", "FFFFS#", "FFFFS#", "FFFFS#",
            "FFFFS#", "FFFFS#", "SSFFS#", "##SFS#", "..#SS#", "...#S#", "....##"};
    private static final String MIDDLE = "#WFFFFFFFFFFS#";

    @Nullable private static BookButton button;
    @Nullable private static Screen owner;
    @Nullable private static Component bubble;
    @Nullable private static String lastSpot;

    private ChangelogButtons() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        button = null;
        owner = null;
        bubble = null;
        Screen screen = event.getScreen();
        CEConfig.Changelog cfg = CEConfig.CHANGELOG;
        boolean title = screen instanceof TitleScreen;
        if (!cfg.enabled.get() || !(title && cfg.titleButton.get() || screen instanceof PauseScreen && cfg.pauseButton.get())) return;

        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (ChangelogStore.versions(manager).isEmpty() && !ChangelogStore.hasDevList(manager)) return;

        int[] spot = spot(screen, event.getListenersList(), title);
        if (spot == null) return;
        button = new BookButton(spot[0], spot[1], b -> Minecraft.getInstance().setScreen(new ChangelogScreen(screen)),
                (b, pose, mouseX, mouseY) -> screen.renderTooltip(pose, TOOLTIP, mouseX, mouseY));
        event.addListener(button);
        owner = screen;
        if (cfg.updatePopup.get() && ChangelogStore.unread(manager)) {
            bubble = Component.translatable("creativeengineering.changelog.popup", ChangelogStore.newest(manager));
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (button == null || bubble == null || owner != event.getScreen()) return;
        Screen screen = event.getScreen();
        Font font = Minecraft.getInstance().font;
        int w = font.width(bubble);
        int bob = Util.getMillis() / 50 % 20 > 10 ? 1 : 0;
        int y = button.y - 10 + bob;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(0, 0, 300);
        int right = button.x + button.getWidth();
        if (right + w + 11 <= screen.width || button.x - w - 11 < 0) {
            draw(pose, TAIL_LEFT, right, y);
            middle(pose, right + 6, y, w);
            draw(pose, END_RIGHT, right + 6 + w, y);
            font.draw(pose, bubble, right + 5, y + 3, 0x303030);
        } else {
            int x = button.x - w - 11;
            draw(pose, END_LEFT, x, y);
            middle(pose, x + 5, y, w);
            draw(pose, TAIL_RIGHT, x + 5 + w, y);
            font.draw(pose, bubble, x + 5, y + 3, 0x303030);
        }
        pose.popPose();
    }

    @Nullable
    private static int[] spot(Screen screen, List<GuiEventListener> widgets, boolean title) {
        AbstractWidget first = find(widgets, title ? "menu.singleplayer" : "menu.returnToGame");
        AbstractWidget second = title ? find(widgets, "menu.multiplayer") : null;
        int[][] candidates = {
                first == null ? null : new int[]{first.x + first.getWidth() + 4, first.y},
                second == null ? null : new int[]{second.x + second.getWidth() + 4, second.y},
                first == null ? null : new int[]{first.x - 24, first.y}};
        String[] names = {"right of " + name(first), "right of " + name(second), "left of " + name(first)};
        for (int i = 0; i < candidates.length; i++) {
            int[] c = candidates[i];
            if (c == null || c[0] < 0 || c[1] < 0 || c[0] + 20 > screen.width || c[1] + 20 > screen.height) continue;
            if (widgets.stream().anyMatch(l -> l instanceof AbstractWidget wd && wd.visible
                    && c[0] < wd.x + wd.getWidth() && wd.x < c[0] + 20 && c[1] < wd.y + wd.getHeight() && wd.y < c[1] + 20)) continue;
            log(screen, names[i]);
            return c;
        }
        log(screen, "nowhere (every spot is taken)");
        return null;
    }

    @Nullable
    private static AbstractWidget find(List<GuiEventListener> widgets, String key) {
        for (GuiEventListener l : widgets) {
            if (l instanceof AbstractWidget w && w.getMessage().getContents() instanceof TranslatableContents t && t.getKey().equals(key)) {
                return w;
            }
        }
        return null;
    }

    private static String name(@Nullable AbstractWidget w) {
        return w == null ? "-" : w.getMessage().getString();
    }

    private static void log(Screen screen, String where) {
        String line = screen.getClass().getSimpleName() + ": " + where;
        if (!line.equals(lastSpot)) LOGGER.info("Changelog book button on {}", line);
        lastSpot = line;
    }

    private static void draw(PoseStack pose, String[] rows, int x, int y) {
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int c = 0; c < row.length(); ) {
                char ch = row.charAt(c);
                int start = c;
                while (c < row.length() && row.charAt(c) == ch) c++;
                if (ch != '.') GuiComponent.fill(pose, x + start, y + r, x + c, y + r + 1, colour(ch));
            }
        }
    }

    private static void middle(PoseStack pose, int x, int y, int width) {
        for (int r = 0; r < MIDDLE.length(); r++) {
            GuiComponent.fill(pose, x, y + r, x + width, y + r + 1, colour(MIDDLE.charAt(r)));
        }
    }

    private static int colour(char ch) {
        return switch (ch) {
            case 'W' -> 0xFFFFFFFF;
            case 'F' -> 0xFFF2F2F2;
            case 'S' -> 0xFFCECECE;
            default -> 0xFF000000;
        };
    }

    /** A plain 20x20 button showing a written book; the "What's New" text is its tooltip and narration. */
    private static final class BookButton extends Button {
        @Nullable private static ItemStack book;

        BookButton(int x, int y, OnPress press, OnTooltip tooltip) {
            super(x, y, 20, 20, Component.empty(), press, tooltip);
        }

        @Override
        public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
            super.renderButton(pose, mouseX, mouseY, partialTick);
            if (book == null) book = new ItemStack(Items.WRITTEN_BOOK);
            Minecraft.getInstance().getItemRenderer().renderGuiItem(book, x + 2, y + 2);
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return wrapDefaultNarrationMessage(TOOLTIP);
        }
    }
}
