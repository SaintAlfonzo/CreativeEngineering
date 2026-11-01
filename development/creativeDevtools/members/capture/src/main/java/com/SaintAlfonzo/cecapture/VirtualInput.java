package com.SaintAlfonzo.cecapture;

/**
 * Faked input read by the mixins: modifier keys for {@code Screen.hasShiftDown()} and friends, and a mouse
 * position for {@code MouseHandler.xpos()/ypos()} so slot tooltips render without moving the real cursor.
 * Mouse coordinates are window pixels, like the real MouseHandler values.
 */
public final class VirtualInput {

    public static volatile boolean shift;
    public static volatile boolean ctrl;
    public static volatile boolean alt;

    public static volatile boolean mouseActive;
    public static volatile double mouseX;
    public static volatile double mouseY;

    private VirtualInput() {}

    public static void releaseAll() {
        shift = ctrl = alt = false;
        mouseActive = false;
    }
}
