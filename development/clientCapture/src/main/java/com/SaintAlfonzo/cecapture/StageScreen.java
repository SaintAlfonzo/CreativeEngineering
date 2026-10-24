package com.SaintAlfonzo.cecapture;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * The {@code tooltip} step's stage: a flat {@link #BACKGROUND} screen with the item drawn at the left third
 * and its tooltip rendered through the normal {@code Screen.renderTooltip(ItemStack)} path, so Legendary
 * Tooltips, AppleSkin, KubeJS and our own tooltip lines all run. The flat background is what crop.py's
 * {@code auto} crop trims. Doesn't pause the integrated server.
 */
public class StageScreen extends Screen {

    /** 0xAARRGGBB; crop.py's auto crop trims this colour (#202020). */
    public static final int BACKGROUND = 0xFF202020;

    private final ItemStack stack;

    public StageScreen(ItemStack stack) {
        super(Component.literal("cecapture stage"));
        this.stack = stack;
    }

    /** Where the virtual mouse sits: the tooltip opens 12 px right of it, with room for wide tooltips. */
    int anchorX() {
        return Math.max(24, width / 6);
    }

    int anchorY() {
        return Math.max(24, height / 3);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        fill(pose, 0, 0, width, height, BACKGROUND);
        int x = anchorX(), y = anchorY();
        itemRenderer.renderAndDecorateItem(stack, x - 8, y - 8);
        itemRenderer.renderGuiItemDecorations(font, stack, x - 8, y - 8);
        renderTooltip(pose, stack, x, y);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
