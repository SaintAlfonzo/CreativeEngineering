package net.satisfy.vinery.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.foundation.client.gui.recipebook.StationRecipeBookScreen;
import net.satisfy.vinery.core.menu.ApplePressMenu;
import net.satisfy.vinery.core.Vinery;

public class ApplePressGui extends StationRecipeBookScreen<ApplePressMenu> {
    public static final ResourceLocation TEXTURE = Vinery.identifier("textures/gui/apple_press_gui.png");

    public static final int MASHING_BAR_X = 40;
    public static final int MASHING_BAR_Y = 17;
    public static final int MASHING_BAR_WIDTH = 24;
    public static final int MASHING_BAR_HEIGHT = 38;
    public static final int MASHING_BAR_U = 176;
    public static final int MASHING_BAR_V = 0;

    public static final int FERMENTING_BAR_X = 101;
    public static final int FERMENTING_BAR_Y = 18;
    public static final int FERMENTING_BAR_WIDTH = 10;
    public static final int FERMENTING_BAR_HEIGHT = 28;
    public static final int FERMENTING_BAR_U = 176;
    public static final int FERMENTING_BAR_V = 47;

    public ApplePressGui(ApplePressMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = leftPos;
        int y = topPos;
        blitTexture(poseStack, TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderProgressArrows(poseStack, x, y);
    }

    private void renderProgressArrows(PoseStack poseStack, int x, int y) {
        if (menu.isCrafting(0)) {
            int height = menu.getScaledProgress(0);
            int xPosition = x + MASHING_BAR_X;
            int yPosition = y + MASHING_BAR_Y + height;
            int textureV = MASHING_BAR_V + height;
            int renderHeight = MASHING_BAR_HEIGHT - height;
            blitTexture(poseStack, TEXTURE, xPosition, yPosition, MASHING_BAR_U, textureV, MASHING_BAR_WIDTH, renderHeight);
        }
        if (menu.isCrafting(1)) {
            int height = menu.getScaledProgress(1);
            int xPosition = x + FERMENTING_BAR_X;
            int yPosition = y + FERMENTING_BAR_Y + FERMENTING_BAR_HEIGHT - height;
            blitTexture(poseStack, TEXTURE, xPosition, yPosition, FERMENTING_BAR_U, FERMENTING_BAR_V + FERMENTING_BAR_HEIGHT - height, FERMENTING_BAR_WIDTH, height);
        }
    }

    /** Port: 1.20's GuiGraphics#blit(texture, ...) as a texture bind and a 256x256 blit. */
    static void blitTexture(PoseStack poseStack, net.minecraft.resources.ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(poseStack, x, y, u, v, width, height, 256, 256);
    }
}
