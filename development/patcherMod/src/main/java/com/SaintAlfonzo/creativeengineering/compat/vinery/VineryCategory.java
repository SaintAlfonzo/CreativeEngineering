package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared look for the Vinery pages, matching Vinery's own REI plugin: plain slots, REI's
 * empty-plus-filling arrow and the large 26x26 result slot. All sprites come from JEI's
 * {@code gui_vanilla.png} (the same ones JEI's furnace page uses).
 *
 * <p>Positions passed to the draw helpers are item positions (what {@code addSlot} takes);
 * the helpers offset the frame around them.
 */
@ParametersAreNonnullByDefault
abstract class VineryCategory<T> implements IRecipeCategory<T> {
    private static final ResourceLocation GUI = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
    static final int ARROW_W = 24;
    static final int ARROW_H = 17;
    static final int TEXT_COLOR = 0xFF404040;

    private final RecipeType<T> type;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic bigSlot;
    private final IDrawableStatic arrowEmpty;
    private final IGuiHelper gui;

    VineryCategory(IGuiHelper gui, RecipeType<T> type, Component title, ResourceLocation iconItem,
                   int width, int height) {
        this.gui = gui;
        this.type = type;
        this.title = title;
        this.background = gui.createBlankDrawable(width, height);
        this.icon = gui.createDrawableItemStack(VineryIds.stack(iconItem));
        this.slot = gui.getSlotDrawable();
        this.bigSlot = gui.createDrawable(GUI, 56, 128, 26, 26);
        this.arrowEmpty = gui.createDrawable(GUI, 24, 132, ARROW_W, ARROW_H);
    }

    /** REI's arrow fills over this many ticks; build once per category and draw with {@link #drawArrow}. */
    IDrawableAnimated animatedArrow(int ticks) {
        return gui.drawableBuilder(GUI, 82, 128, ARROW_W, ARROW_H)
                .buildAnimated(ticks, IDrawableAnimated.StartDirection.LEFT, false);
    }

    void drawSlot(PoseStack pose, int itemX, int itemY) {
        slot.draw(pose, itemX - 1, itemY - 1);
    }

    void drawBigSlot(PoseStack pose, int itemX, int itemY) {
        bigSlot.draw(pose, itemX - 5, itemY - 5);
    }

    void drawArrow(PoseStack pose, IDrawableAnimated filling, int x, int y) {
        arrowEmpty.draw(pose, x, y);
        filling.draw(pose, x, y);
    }

    static void drawCentered(PoseStack pose, Component text, int centerX, int y) {
        var font = Minecraft.getInstance().font;
        font.draw(pose, text, centerX - font.width(text) / 2f, y, TEXT_COLOR);
    }

    static boolean over(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    static Component ticksAsSeconds(int ticks) {
        float seconds = ticks / 20f;
        String shown = seconds == Math.floor(seconds) ? String.valueOf((int) seconds) : String.valueOf(seconds);
        return Component.translatable("creativeengineering.jei.vinery.time", shown);
    }

    /** Tooltip list helper: empty unless the mouse is over the given box. */
    static List<Component> tooltipIf(boolean hovered, Component... lines) {
        return hovered ? List.of(lines) : new ArrayList<>();
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public abstract void draw(T recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY);
}
