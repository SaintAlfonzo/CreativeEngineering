package net.satisfy.vinery.core.compat.jei.port;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Port-only JEI pages (grape stomping, wine aging): plain slots, an animated arrow and the large result slot, all from
 * JEI's {@code gui_vanilla.png}. Item positions are what {@code addSlot} takes; the draw helpers frame around them.
 * Based on patcherMod's 1.3.10 Vinery pages (feature_writeup/vinery_jei.md), which the port replaces.
 */
abstract class PortCategory<T> implements IRecipeCategory<T> {
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

    PortCategory(IGuiHelper gui, RecipeType<T> type, Component title, ItemStack icon, int width, int height) {
        this.gui = gui;
        this.type = type;
        this.title = title;
        this.background = gui.createBlankDrawable(width, height);
        this.icon = gui.createDrawableItemStack(icon);
        this.slot = gui.getSlotDrawable();
        this.bigSlot = gui.createDrawable(GUI, 56, 128, 26, 26);
        this.arrowEmpty = gui.createDrawable(GUI, 24, 132, ARROW_W, ARROW_H);
    }

    IDrawableAnimated animatedArrow(int ticks) {
        return gui.drawableBuilder(GUI, 82, 128, ARROW_W, ARROW_H).buildAnimated(ticks, IDrawableAnimated.StartDirection.LEFT, false);
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

    @Override
    public @NotNull RecipeType<T> getRecipeType() {
        return type;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }
}
