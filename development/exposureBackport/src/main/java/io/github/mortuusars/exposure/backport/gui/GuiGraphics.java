package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Backport of 1.20+ {@code GuiGraphics} over 1.19.2's {@link PoseStack}-based GUI code, covering the calls Exposure
 * and its addons make. Upstream GUI code keeps its {@code guiGraphics.blit(...)}/{@code drawString(...)} calls;
 * 1.19.2 entry points ({@code render(PoseStack, ...)}) wrap their PoseStack with {@code new GuiGraphics(poseStack)}.
 * <p>
 * GUI sprites ({@link #blitSprite}): 1.20.2+ keeps them in an atlas built from {@code textures/gui/sprites/}. Here
 * a sprite id {@code ns:path} is the texture {@code ns:textures/gui/sprites/path.png}, drawn stretched (the only
 * scaling Exposure's sprites use). Vanilla sprites the mods use are mapped onto their 1.19.2 sheets.
 */
public class GuiGraphics {
    private final Minecraft minecraft;
    private final PoseStack pose;
    private final MultiBufferSource.BufferSource bufferSource;
    private final Deque<int[]> scissorStack = new ArrayDeque<>();

    public GuiGraphics(PoseStack pose) {
        this.minecraft = Minecraft.getInstance();
        this.pose = pose;
        this.bufferSource = minecraft.renderBuffers().bufferSource();
    }

    public PoseStack pose() {
        return pose;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return bufferSource;
    }

    public int guiWidth() {
        return minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return minecraft.getWindow().getGuiScaledHeight();
    }

    public void flush() {
        RenderSystem.disableDepthTest();
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
    }

    public void drawManaged(Runnable drawFunction) {
        flush();
        drawFunction.run();
        flush();
    }

    public void setColor(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    // ---- scissor ----

    public void enableScissor(int minX, int minY, int maxX, int maxY) {
        scissorStack.push(new int[]{minX, minY, maxX, maxY});
        GuiComponent.enableScissor(minX, minY, maxX, maxY);
    }

    public void disableScissor() {
        scissorStack.poll();
        int[] previous = scissorStack.peek();
        if (previous != null) {
            GuiComponent.enableScissor(previous[0], previous[1], previous[2], previous[3]);
        } else {
            GuiComponent.disableScissor();
        }
    }

    // ---- fills ----

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        GuiComponent.fill(pose, minX, minY, maxX, maxY, color);
    }

    public void fill(int minX, int minY, int maxX, int maxY, int z, int color) {
        pose.pushPose();
        pose.translate(0, 0, z);
        GuiComponent.fill(pose, minX, minY, maxX, maxY, color);
        pose.popPose();
    }

    public void fill(GuiRenderTypes renderType, int minX, int minY, int maxX, int maxY, int color) {
        fill(renderType, minX, minY, maxX, maxY, 0, color);
    }

    public void fill(GuiRenderTypes renderType, int minX, int minY, int maxX, int maxY, int z, int color) {
        renderType.setup();
        fill(minX, minY, maxX, maxY, z, color);
        renderType.clear();
    }

    public void fillGradient(GuiRenderTypes renderType, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int z) {
        renderType.setup();
        fillGradient(x1, y1, x2, y2, z, colorFrom, colorTo);
        renderType.clear();
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        fillGradient(x1, y1, x2, y2, 0, colorFrom, colorTo);
    }

    public void fillGradient(int x1, int y1, int x2, int y2, int z, int colorFrom, int colorTo) {
        GradientAccess.fill(pose, x1, y1, x2, y2, colorFrom, colorTo, z);
    }

    /** Left-to-right gradient (Exposure's test screens build these from raw vertices in 1.20). */
    public void fillHorizontalGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        Matrix4f matrix = pose.last().pose();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(colorFrom).endVertex();
        builder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(colorFrom).endVertex();
        builder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(colorTo).endVertex();
        builder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(colorTo).endVertex();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public void hLine(int minX, int maxX, int y, int color) {
        if (maxX < minX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }
        fill(minX, y, maxX + 1, y + 1, color);
    }

    public void vLine(int x, int minY, int maxY, int color) {
        if (maxY < minY) {
            int i = minY;
            minY = maxY;
            maxY = i;
        }
        fill(x, minY + 1, x + 1, maxY, color);
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        fill(x, y, x + width, y + 1, color);
        fill(x, y + height - 1, x + width, y + height, color);
        fill(x, y + 1, x + 1, y + height - 1, color);
        fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    // ---- text (1.20: dropShadow defaults to true) ----

    public int drawString(Font font, @Nullable String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        }
        return dropShadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        }
        return dropShadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
        return dropShadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        return dropShadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean dropShadow) {
        return drawString(font, text.getVisualOrderText(), x, y, color, dropShadow);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        drawString(font, text, x - font.width(text) / 2, y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        FormattedCharSequence sequence = text.getVisualOrderText();
        drawString(font, sequence, x - font.width(sequence) / 2, y, color);
    }

    public void drawCenteredString(Font font, FormattedCharSequence text, int x, int y, int color) {
        drawString(font, text, x - font.width(text) / 2, y, color);
    }

    public void drawWordWrap(Font font, net.minecraft.network.chat.FormattedText text, int x, int y, int lineWidth, int color) {
        for (FormattedCharSequence line : font.split(text, lineWidth)) {
            drawString(font, line, x, y, color, false);
            y += 9;
        }
    }

    // ---- textures ----

    /** blit(texture, x, y, u, v, width, height): 256x256 texture. */
    public void blit(ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        blit(atlasLocation, x, y, 0, (float) uOffset, (float) vOffset, uWidth, vHeight, 256, 256);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int blitOffset, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        blit(atlasLocation, x, x + uWidth, y, y + vHeight, blitOffset, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        blit(atlasLocation, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
    }

    public void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight) {
        blit(atlasLocation, x, y, width, height, uOffset, vOffset, width, height, textureWidth, textureHeight);
    }

    void blit(ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
        innerBlit(atlasLocation, x1, x2, y1, y2, blitOffset,
                (uOffset + 0.0F) / (float) textureWidth, (uOffset + (float) uWidth) / (float) textureWidth,
                (vOffset + 0.0F) / (float) textureHeight, (vOffset + (float) vHeight) / (float) textureHeight);
    }

    public void blit(int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite) {
        innerBlit(sprite.atlas().location(), x, x + width, y, y + height, blitOffset, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
    }

    public void blit(int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite, float red, float green, float blue, float alpha) {
        setColor(red, green, blue, alpha);
        blit(x, y, blitOffset, width, height, sprite);
        setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void innerBlit(ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, (float) x1, (float) y1, (float) blitOffset).uv(minU, minV).endVertex();
        builder.vertex(matrix, (float) x1, (float) y2, (float) blitOffset).uv(minU, maxV).endVertex();
        builder.vertex(matrix, (float) x2, (float) y2, (float) blitOffset).uv(maxU, maxV).endVertex();
        builder.vertex(matrix, (float) x2, (float) y1, (float) blitOffset).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    // ---- GUI sprites (1.20.2+) ----

    /** Texture of a GUI sprite id: {@code ns:path} -> {@code ns:textures/gui/sprites/path.png}. */
    public static ResourceLocation spriteTexture(ResourceLocation sprite) {
        return new ResourceLocation(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
    }

    public void blitSprite(ResourceLocation sprite, int x, int y, int width, int height) {
        blitSprite(sprite, x, y, 0, width, height);
    }

    public void blitSprite(ResourceLocation sprite, int x, int y, int blitOffset, int width, int height) {
        VanillaSprite vanilla = VANILLA_SPRITES.get(sprite);
        if (vanilla != null) {
            vanilla.blit(this, x, y, blitOffset, width, height);
            return;
        }
        innerBlit(spriteTexture(sprite), x, x + width, y, y + height, blitOffset, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    /** Part of a sprite: the sprite is {@code textureWidth}x{@code textureHeight}, draws the region at (u, v). */
    public void blitSprite(ResourceLocation sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int uWidth, int vHeight) {
        blitSprite(sprite, textureWidth, textureHeight, uPosition, vPosition, x, y, 0, uWidth, vHeight);
    }

    public void blitSprite(ResourceLocation sprite, int textureWidth, int textureHeight, int uPosition, int vPosition, int x, int y, int blitOffset, int uWidth, int vHeight) {
        if (uWidth == 0 || vHeight == 0) {
            return;
        }
        innerBlit(spriteTexture(sprite), x, x + uWidth, y, y + vHeight, blitOffset,
                (float) uPosition / textureWidth, (float) (uPosition + uWidth) / textureWidth,
                (float) vPosition / textureHeight, (float) (vPosition + vHeight) / textureHeight);
    }

    /** A vanilla 1.20.2+ sprite drawn from its 1.19.2 sheet, sliced so it keeps its borders at any size. */
    private record VanillaSprite(ResourceLocation texture, int u, int v, int width, int height, int border) {
        void blit(GuiGraphics graphics, int x, int y, int blitOffset, int w, int h) {
            int b = Math.min(border, Math.min(w, h) / 2);
            if (b == 0 || (w == width && h == height)) {
                graphics.blit(texture, x, x + w, y, y + h, blitOffset, width, height, u, v, 256, 256);
                return;
            }
            int innerW = width - 2 * b;
            int innerH = height - 2 * b;
            int[] xs = {x, x + b, x + w - b, x + w};
            int[] ys = {y, y + b, y + h - b, y + h};
            int[] us = {u, u + b, u + b + innerW};
            int[] vs = {v, v + b, v + b + innerH};
            int[] uws = {b, innerW, b};
            int[] vhs = {b, innerH, b};
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (xs[col + 1] > xs[col] && ys[row + 1] > ys[row]) {
                        graphics.blit(texture, xs[col], xs[col + 1], ys[row], ys[row + 1], blitOffset,
                                uws[col], vhs[row], us[col], vs[row], 256, 256);
                    }
                }
            }
        }
    }

    private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation TOASTS = new ResourceLocation("textures/gui/toasts.png");
    private static final Map<ResourceLocation, VanillaSprite> VANILLA_SPRITES = Map.of(
            new ResourceLocation("widget/slider"), new VanillaSprite(WIDGETS, 0, 46, 200, 20, 3),
            new ResourceLocation("widget/slider_highlighted"), new VanillaSprite(WIDGETS, 0, 46, 200, 20, 3),
            new ResourceLocation("widget/slider_handle"), new VanillaSprite(WIDGETS, 0, 66, 200, 20, 3),
            new ResourceLocation("widget/slider_handle_highlighted"), new VanillaSprite(WIDGETS, 0, 86, 200, 20, 3),
            new ResourceLocation("widget/button"), new VanillaSprite(WIDGETS, 0, 66, 200, 20, 3),
            new ResourceLocation("widget/button_highlighted"), new VanillaSprite(WIDGETS, 0, 86, 200, 20, 3),
            new ResourceLocation("widget/button_disabled"), new VanillaSprite(WIDGETS, 0, 46, 200, 20, 3),
            new ResourceLocation("toast/tutorial"), new VanillaSprite(TOASTS, 0, 96, 160, 32, 4));

    // ---- items ----

    public void renderItem(ItemStack stack, int x, int y) {
        withPose(() -> itemRenderer().renderAndDecorateItem(stack, x, y));
    }

    public void renderItem(ItemStack stack, int x, int y, int seed) {
        withPose(() -> itemRenderer().renderAndDecorateItem(stack, x, y, seed));
    }

    public void renderItem(ItemStack stack, int x, int y, int seed, int guiOffset) {
        withPose(() -> itemRenderer().renderAndDecorateItem(stack, x, y, seed, guiOffset));
    }

    public void renderItem(LivingEntity entity, ItemStack stack, int x, int y, int seed) {
        withPose(() -> itemRenderer().renderAndDecorateItem(entity, stack, x, y, seed));
    }

    public void renderFakeItem(ItemStack stack, int x, int y) {
        withPose(() -> itemRenderer().renderAndDecorateFakeItem(stack, x, y));
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        withPose(() -> itemRenderer().renderGuiItemDecorations(font, stack, x, y));
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, @Nullable String text) {
        withPose(() -> itemRenderer().renderGuiItemDecorations(font, stack, x, y, text));
    }

    private ItemRenderer itemRenderer() {
        return minecraft.getItemRenderer();
    }

    /** 1.19.2's ItemRenderer draws with the global model-view stack: apply this pose there for the call. */
    private void withPose(Runnable render) {
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        render.run();
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    // ---- tooltips (drawn by the current screen, or a stand-in when there is none, e.g. on the HUD) ----

    public void renderTooltip(Font font, ItemStack stack, int mouseX, int mouseY) {
        Screen screen = tooltipScreen();
        screen.renderComponentTooltip(pose, screen.getTooltipFromItem(stack), mouseX, mouseY, font, stack);
    }

    public void renderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY) {
        tooltipScreen().renderTooltip(pose, tooltipLines, visualTooltipComponent, mouseX, mouseY, font);
    }

    public void renderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        tooltipScreen().renderTooltip(pose, tooltipLines, visualTooltipComponent, mouseX, mouseY, font, stack);
    }

    public void renderTooltip(Font font, Component message, int mouseX, int mouseY) {
        tooltipScreen().renderComponentTooltip(pose, List.of(message), mouseX, mouseY, font);
    }

    public void renderComponentTooltip(Font font, List<Component> tooltipLines, int mouseX, int mouseY) {
        tooltipScreen().renderComponentTooltip(pose, tooltipLines, mouseX, mouseY, font);
    }

    public void renderComponentTooltip(Font font, List<? extends net.minecraft.network.chat.FormattedText> tooltipLines, int mouseX, int mouseY, ItemStack stack) {
        tooltipScreen().renderComponentTooltip(pose, tooltipLines, mouseX, mouseY, font, stack);
    }

    public void renderTooltip(Font font, List<? extends FormattedCharSequence> tooltipLines, int mouseX, int mouseY) {
        tooltipScreen().renderTooltip(pose, tooltipLines, mouseX, mouseY, font);
    }

    public void renderTooltip(Font font, List<? extends FormattedCharSequence> tooltipLines, DefaultTooltipPositioner positioner, int mouseX, int mouseY) {
        renderTooltip(font, tooltipLines, mouseX, mouseY);
    }

    public void renderComponentHoverEffect(Font font, @Nullable Style style, int mouseX, int mouseY) {
        TooltipScreen.INSTANCE.prepare(minecraft).hoverEffect(pose, style, mouseX, mouseY);
    }

    private Screen tooltipScreen() {
        Screen screen = minecraft.screen;
        return screen != null ? screen : TooltipScreen.INSTANCE.prepare(minecraft);
    }

    /** A screen that is never shown, only used for its tooltip drawing (sized to the window on every use). */
    private static class TooltipScreen extends Screen {
        static final TooltipScreen INSTANCE = new TooltipScreen();

        private TooltipScreen() {
            super(Component.empty());
        }

        TooltipScreen prepare(Minecraft minecraft) {
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            if (this.minecraft != minecraft || this.width != width || this.height != height) {
                init(minecraft, width, height);
            }
            return this;
        }

        void hoverEffect(PoseStack poseStack, @Nullable Style style, int mouseX, int mouseY) {
            renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        }
    }

    /** GuiComponent's gradient fill is protected static. */
    private static class GradientAccess extends GuiComponent {
        static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int z) {
            GuiComponent.fillGradient(poseStack, x1, y1, x2, y2, colorFrom, colorTo, z);
        }
    }
}
