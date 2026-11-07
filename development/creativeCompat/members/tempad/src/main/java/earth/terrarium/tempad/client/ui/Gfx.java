// Port glue (ours, GPLv3), TP-D4/TP-D5: the few GuiGraphics calls the Tempad screens use, on 1.19.2's PoseStack +
// GuiComponent, plus a nesting scissor stack (1.21 GuiGraphics keeps one; 1.19.2's GuiComponent.enableScissor does not).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Gfx {
    /** Olympus MinecraftColors (ChatFormatting colours, alpha 0 as there). */
    public static final int BLACK = 0x000000;
    public static final int WHITE = 0xFFFFFF;
    public static final int GRAY = 0xAAAAAA;
    public static final int DARK_GRAY = 0x555555;
    public static final int RED = 0xFF5555;
    public static final int[] MC_COLORS;

    static {
        ChatFormatting[] order = {ChatFormatting.BLACK, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_AQUA,
                ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE, ChatFormatting.GOLD, ChatFormatting.GRAY, ChatFormatting.DARK_GRAY,
                ChatFormatting.BLUE, ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE,
                ChatFormatting.YELLOW, ChatFormatting.WHITE};
        MC_COLORS = new int[order.length];
        for (int i = 0; i < order.length; i++) MC_COLORS[i] = order[i].getColor();
    }

    private record Rect(int x1, int y1, int x2, int y2) {
    }

    private static final Deque<Rect> SCISSORS = new ArrayDeque<>();

    private Gfx() {
    }

    /** Olympus UIHelper.getEnsureAlpha: a colour with alpha 0 is drawn opaque. */
    public static int ensureAlpha(int color) {
        return (color >>> 24) == 0 ? color | 0xFF000000 : color;
    }

    public static void fill(PoseStack pose, int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    /** GuiGraphics.renderOutline. */
    public static void outline(PoseStack pose, int x, int y, int width, int height, int color) {
        fill(pose, x, y, x + width, y + 1, color);
        fill(pose, x, y + height - 1, x + width, y + height, color);
        fill(pose, x, y + 1, x + 1, y + height - 1, color);
        fill(pose, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /** GuiGraphics.drawString(font, text, x, y, color, shadow); returns the end x like 1.21. */
    public static int drawString(PoseStack pose, Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public static int drawString(PoseStack pose, Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public static int drawString(PoseStack pose, Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public static void enableScissor(int x1, int y1, int x2, int y2) {
        Rect r = new Rect(x1, y1, x2, y2);
        Rect top = SCISSORS.peek();
        if (top != null) {
            r = new Rect(Math.max(r.x1, top.x1), Math.max(r.y1, top.y1), Math.min(r.x2, top.x2), Math.min(r.y2, top.y2));
            if (r.x2 < r.x1) r = new Rect(r.x1, r.y1, r.x1, r.y2);
            if (r.y2 < r.y1) r = new Rect(r.x1, r.y1, r.x2, r.y1);
        }
        SCISSORS.push(r);
        GuiComponent.enableScissor(r.x1, r.y1, r.x2, r.y2);
    }

    public static void disableScissor() {
        SCISSORS.poll();
        Rect top = SCISSORS.peek();
        if (top == null) {
            GuiComponent.disableScissor();
        } else {
            GuiComponent.enableScissor(top.x1, top.y1, top.x2, top.y2);
        }
    }

    /** GuiGraphics.containsPointInScissor. */
    public static boolean inScissor(int x, int y) {
        Rect top = SCISSORS.peek();
        return top == null || (x >= top.x1 && x < top.x2 && y >= top.y1 && y < top.y2);
    }
}
