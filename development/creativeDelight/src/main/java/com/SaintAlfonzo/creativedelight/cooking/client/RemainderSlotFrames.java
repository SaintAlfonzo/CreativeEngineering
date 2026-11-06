package com.SaintAlfonzo.creativedelight.cooking.client;

import com.SaintAlfonzo.creativedelight.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativedelight.cooking.RemainderSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the {@link RemainderSlot} frames on a station screen, cut from an existing slot frame in the station's own
 * GUI texture (no new texture). {@code frameU/frameV} is the top-left of an 18x18 slot frame in {@code texture}.
 */
public final class RemainderSlotFrames {

    private RemainderSlotFrames() {}

    public static void draw(PoseStack poseStack, ResourceLocation texture, int leftPos, int topPos, int frameU, int frameV) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        for (int i = 0; i < RemainderBuffer.SLOTS; ++i) {
            GuiComponent.blit(poseStack, leftPos + RemainderSlot.X - 1, topPos + RemainderSlot.y(i) - 1, 0,
                    frameU, frameV, 18, 18, 256, 256);
        }
    }
}
