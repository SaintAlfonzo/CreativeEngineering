package com.SaintAlfonzo.creativecommon.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Client only. Fix for vanilla MC-122477: the key that opens chat (T, or / for a command) is sometimes typed into the
 * new chat box, giving "//" or "t". Chat opens on the next client tick after the key press; GLFW usually delivers the
 * key's character in the same event poll (screen still null, ignored), but on Linux/X11 it can arrive a poll later,
 * after the chat screen exists. That made it intermittent with identical timing.
 *
 * <p>{@code Minecraft.openChatScreen} arms the guard with the opening key. The first character event after that is
 * dropped only if no other key press or repeat came in between, the opening key is still held, it arrives within
 * {@link #WINDOW_MS}, and it is the character that key produces. Anything else disarms the guard and types normally.
 * Rebound chat/command keys are covered, since the bound key is read at open time.
 */
public final class ChatOpenKeyGuard {
    private static final long WINDOW_MS = 250L;

    private static InputConstants.Key armedKey;
    private static long armedAt;

    private ChatOpenKeyGuard() {}

    /** Called at the head of {@code Minecraft.openChatScreen(String)}. */
    public static void arm(String prefix) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping mapping = "/".equals(prefix) ? mc.options.keyCommand : mc.options.keyChat;
        InputConstants.Key key = mapping.getKey();
        if (key.getType() != InputConstants.Type.KEYSYM || key.getValue() == InputConstants.UNKNOWN.getValue()) {
            armedKey = null;
            return;
        }
        armedKey = key;
        armedAt = Util.getMillis();
    }

    /** Any key press or repeat after the open means later characters are real typing. */
    public static void onKeyPress(int action) {
        if (action != GLFW.GLFW_RELEASE) {
            armedKey = null;
        }
    }

    /** True when this character event is the stale copy of the chat-opening key press and should be dropped. */
    public static boolean shouldDrop(int codePoint) {
        InputConstants.Key key = armedKey;
        if (key == null) {
            return false;
        }
        armedKey = null;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen) || Util.getMillis() - armedAt > WINDOW_MS) {
            return false;
        }
        int keyCode = key.getValue();
        if (!InputConstants.isKeyDown(mc.getWindow().getWindow(), keyCode)) {
            return false;
        }
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name == null || name.codePointCount(0, name.length()) != 1) {
            return false;
        }
        int expected = name.codePointAt(0);
        return Character.toLowerCase(expected) == Character.toLowerCase(codePoint);
    }
}
