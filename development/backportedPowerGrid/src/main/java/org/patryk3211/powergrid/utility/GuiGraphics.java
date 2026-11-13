// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). A small stand-in for 1.20's GuiGraphics on 1.19.2 (PoseStack + GuiComponent), so ported screens keep
// upstream's drawing calls. Only the methods the ported code uses are here (the circuit editor's set since P9b). It
// extends GuiComponent for the protected line and gradient helpers.
package org.patryk3211.powergrid.utility;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiGraphics extends GuiComponent {
    private final PoseStack pose;

    public GuiGraphics(PoseStack pose) {
        this.pose = pose;
    }

    public PoseStack pose() {
        return pose;
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        blit(texture, x, y, 0, u, v, width, height, 256, 256);
    }

    /** 1.20 {@code blit(texture, x, y, u, v, width, height, textureWidth, textureHeight)}. */
    public void blit(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        blit(texture, x, y, 0, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        GuiComponent.blit(pose, x, y, z, u, v, width, height, textureHeight, textureWidth);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    /** 1.20 {@code renderOutline}: a one pixel frame (P9a, circuit component footprints). */
    public void renderOutline(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        GuiComponent.drawCenteredString(pose, font, text, x, y, color);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        GuiComponent.drawCenteredString(pose, font, text, x, y, color);
    }

    /** Draws an item at the current pose (1.19.2 item rendering reads the model-view stack, so the pose is applied there). */
    public void renderItem(ItemStack stack, int x, int y) {
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y);
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /** 1.20 {@code hLine}: a one pixel horizontal line from x1 to x2 inclusive (P9b). */
    public void hLine(int x1, int x2, int y, int color) {
        super.hLine(pose, x1, x2, y, color);
    }

    /** 1.20 {@code vLine}: a one pixel vertical line between y1 and y2 exclusive (P9b). */
    public void vLine(int x, int y1, int y2, int color) {
        super.vLine(pose, x, y1, y2, color);
    }

    /** 1.20 {@code fillGradient} (P9b, the editor's saving overlay). */
    public void fillGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        GuiComponent.fillGradient(pose, x1, y1, x2, y2, colorFrom, colorTo, 0);
    }

    /**
     * 1.20 {@code blitRepeating}: fills width x height with the source region tiled, each tile cut to near-equal
     * slices as 1.20's {@code Divisor} does (P9b, the component properties panel).
     */
    public void blitRepeating(ResourceLocation texture, int x, int y, int width, int height, int u, int v, int sourceWidth, int sourceHeight) {
        int[] cols = slices(width, sourceWidth);
        int[] rows = slices(height, sourceHeight);
        int px = x;
        for(int cw : cols) {
            int uOff = (sourceWidth - cw) / 2;
            int py = y;
            for(int rh : rows) {
                int vOff = (sourceHeight - rh) / 2;
                blit(texture, px, py, u + uOff, v + vOff, cw, rh);
                py += rh;
            }
            px += cw;
        }
    }

    private static int[] slices(int target, int total) {
        if(target <= 0 || total <= 0)
            return new int[0];
        int count = (target + total - 1) / total;
        int quotient = target / count, mod = target % count, remainder = 0;
        int[] result = new int[count];
        for(int i = 0; i < count; ++i) {
            int part = quotient;
            remainder += mod;
            if(remainder >= count) {
                remainder -= count;
                ++part;
            }
            result[i] = part;
        }
        return result;
    }

    /** 1.20 {@code renderTooltip(font, lines, x, y)} for pre-split lines (P9b). */
    public void renderTooltip(Font font, List<? extends FormattedCharSequence> lines, int mouseX, int mouseY) {
        var screen = Minecraft.getInstance().screen;
        if(screen != null)
            screen.renderTooltip(pose, lines, mouseX, mouseY);
    }

    /** 1.20 {@code renderTooltip(font, text, x, y)} (P9b). */
    public void renderTooltip(Font font, Component text, int mouseX, int mouseY) {
        var screen = Minecraft.getInstance().screen;
        if(screen != null)
            screen.renderTooltip(pose, text, mouseX, mouseY);
    }

    public void renderComponentTooltip(Font font, List<Component> tooltip, int mouseX, int mouseY) {
        var screen = Minecraft.getInstance().screen;
        if(screen != null)
            screen.renderComponentTooltip(pose, tooltip, mouseX, mouseY, font);
    }
}
