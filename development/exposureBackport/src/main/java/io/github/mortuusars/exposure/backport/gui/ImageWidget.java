package io.github.mortuusars.exposure.backport.gui;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

/** Backport of 1.20.2+ {@code ImageWidget}: a non-interactive sprite or texture. */
public abstract class ImageWidget extends AbstractWidget {
    ImageWidget(int x, int y, int width, int height) {
        super(x, y, width, height, CommonComponents.EMPTY);
    }

    public static ImageWidget texture(int width, int height, ResourceLocation texture, int textureWidth, int textureHeight) {
        return new ImageWidget(0, 0, width, height) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(texture, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0.0F, 0.0F, this.getWidth(), this.getHeight(), textureWidth, textureHeight);
            }
        };
    }

    public static ImageWidget sprite(int width, int height, ResourceLocation sprite) {
        return new ImageWidget(0, 0, width, height) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager handler) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }
}
