package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Backport of the 1.20.2+ sprite {@code ImageButton}. */
public class ImageButton extends Button {
    protected final WidgetSprites sprites;

    public ImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress) {
        this(x, y, width, height, sprites, onPress, CommonComponents.EMPTY);
    }

    public ImageButton(int x, int y, int width, int height, WidgetSprites sprites, Button.OnPress onPress, Component message) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.sprites = sprites;
    }

    public ImageButton(int width, int height, WidgetSprites sprites, Button.OnPress onPress, Component message) {
        this(0, 0, width, height, sprites, onPress, message);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(this.sprites.get(this.isActive(), this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
    }
}
