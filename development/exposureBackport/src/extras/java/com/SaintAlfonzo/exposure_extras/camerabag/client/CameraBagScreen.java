package com.SaintAlfonzo.exposure_extras.camerabag.client;

import com.SaintAlfonzo.exposure_extras.camerabag.CameraBagMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Drawn like Spice of Life's lunch box screen, with its textures: a blank 176x166 panel with the
 * player inventory, plus one 18x18 slot frame per bag slot wherever the menu put it.
 */
public class CameraBagScreen extends AbstractContainerScreen<CameraBagMenu> {

    private static final ResourceLocation PANEL = new ResourceLocation("solapplepie", "textures/gui/inventory.png");
    private static final ResourceLocation SLOT = new ResourceLocation("solapplepie", "textures/gui/slot.png");

    public CameraBagScreen(CameraBagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PANEL);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        RenderSystem.setShaderTexture(0, SLOT);
        for (int index = 0; index < menu.bagSize(); index++) {
            Slot slot = menu.slots.get(index);
            blit(pose, leftPos + slot.x - 1, topPos + slot.y - 1, 0.0F, 0.0F, 18, 18, 18, 18);
        }
    }
}
