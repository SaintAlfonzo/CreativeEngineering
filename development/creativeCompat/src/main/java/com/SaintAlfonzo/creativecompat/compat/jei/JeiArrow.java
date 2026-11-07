package com.SaintAlfonzo.creativecompat.compat.jei;

import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * The arrow every pack JEI page draws (JEI's own recipe arrow from {@code gui_vanilla.png}): Feast Plating, the world
 * tabs, Tipped Arrows, Potion Buckets, Seasoning. One place, so a later per-seasoning split keeps it (TODO
 * jei/jei-coverage #24).
 */
public final class JeiArrow {
    public static final int WIDTH = 24;
    public static final int HEIGHT = 17;
    private static final ResourceLocation GUI = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private JeiArrow() {}

    public static IDrawableStatic create(IGuiHelper helper) {
        return helper.createDrawable(GUI, 82, 128, WIDTH, HEIGHT);
    }
}
