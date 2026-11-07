// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.components.renderers
// (WidgetRenderers, TextWidgetRenderer, IconWidgetRenderer, TextWithIconWidgetRenderer, SolidColorWidgetRenderer).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class Renderers {
    /** Olympus UIIcons.CHEVRON_DOWN / CHEVRON_UP (Olympus art, served from the jar nested in the upstream jar). */
    public static final ResourceLocation CHEVRON_DOWN = new ResourceLocation("olympus", "icons/chevron_down");
    public static final ResourceLocation CHEVRON_UP = new ResourceLocation("olympus", "icons/chevron_up");
    public static final ResourceLocation EYE_DROPPER = new ResourceLocation("olympus", "icons/eye_dropper");

    private Renderers() {
    }

    public interface Colorable extends WidgetRenderer {
        Colorable withColor(int color);
    }

    public static TextRenderer text(Component text) {
        return new TextRenderer(text);
    }

    public static IconRenderer icon(ResourceLocation icon) {
        return new IconRenderer(icon);
    }

    public static TextWithIconRenderer textWithIcon(Component text, ResourceLocation icon) {
        return new TextWithIconRenderer(text(text), icon(icon));
    }

    public static TextWithIconRenderer textWithChevron(Component text, boolean open) {
        return textWithIcon(text, open ? CHEVRON_DOWN : CHEVRON_UP).withTextLeftIconRight();
    }

    public static TextWithIconRenderer ellipsisWithChevron(boolean open) {
        return textWithChevron(CommonComponents.ELLIPSIS, open);
    }

    public static SolidRenderer solid() {
        return new SolidRenderer();
    }

    public static WidgetRenderer sprite(WidgetSprites sprites) {
        return (pose, ctx, pt) -> Sprites.blit(pose, sprites.get(ctx.widget.isActive(), ctx.widget.isHoveredOrFocused()), ctx.x, ctx.y, ctx.width, ctx.height);
    }

    public static WidgetRenderer sprite(ResourceLocation sprite) {
        return (pose, ctx, pt) -> Sprites.blit(pose, sprite, ctx.x, ctx.y, ctx.width, ctx.height);
    }

    /** Olympus WidgetRenderers.withColors: the colour follows active / hovered state. */
    public static WidgetRenderer withColors(Colorable renderer, int disabled, int normal, int hover) {
        return (pose, ctx, pt) -> {
            if (ctx.widget.active) {
                renderer.withColor(ctx.widget.isHoveredOrFocused() ? hover : normal);
            } else {
                renderer.withColor(disabled);
            }
            renderer.render(pose, ctx, pt);
        };
    }

    public static final class TextRenderer implements Colorable {
        private final Component text;
        private Font font = Minecraft.getInstance().font;
        private int color = Gfx.DARK_GRAY;
        private boolean shadow;
        private float alignX = 0.5f;

        TextRenderer(Component text) {
            this.text = text;
        }

        @Override
        public void render(PoseStack pose, Ctx ctx, float pt) {
            int textWidth = font.width(text);
            int centerY = ctx.y + ctx.height / 2 - 9 / 2;
            if (textWidth > ctx.width) {
                // Olympus scrolls an overlong text back and forth, clipped to the area.
                double seconds = Util.getMillis() / 1000.0;
                int overhang = textWidth - ctx.width;
                double e = Math.max(overhang * 0.5, 3.0);
                double f = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * seconds / e)) / 2.0 + 0.5;
                double g = Mth.lerp(f, 0.0, overhang);
                Gfx.enableScissor(ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height);
                Gfx.drawString(pose, font, text, ctx.x - (int) g, centerY, Gfx.ensureAlpha(color), shadow);
                Gfx.disableScissor();
            } else {
                int centerX = ctx.x + Math.round(ctx.width * alignX);
                Gfx.drawString(pose, font, text, centerX - Math.round(textWidth * alignX), centerY, Gfx.ensureAlpha(color), shadow);
            }
        }

        @Override
        public TextRenderer withColor(int color) {
            this.color = color;
            return this;
        }

        public TextRenderer withShadow() {
            this.shadow = true;
            return this;
        }

        public TextRenderer withAlignment(float alignX) {
            this.alignX = alignX;
            return this;
        }

        public TextRenderer withLeftAlignment() {
            return withAlignment(0f);
        }
    }

    public static final class IconRenderer implements Colorable {
        private final ResourceLocation icon;
        private int color = Gfx.WHITE;
        private boolean shadow;

        IconRenderer(ResourceLocation icon) {
            this.icon = icon;
        }

        @Override
        public void render(PoseStack pose, Ctx ctx, float pt) {
            float red = ((color >> 16) & 0xFF) / 255f;
            float green = ((color >> 8) & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;
            float alpha = ((color >>> 24) & 0xFF) / 255f;
            if (alpha == 0f) alpha = 1f;
            if (shadow) {
                RenderSystem.setShaderColor(red / 3f, green / 3f, blue / 3f, alpha);
                Sprites.blit(pose, icon, ctx.x + 1, ctx.y + 1, ctx.width, ctx.height);
            }
            RenderSystem.setShaderColor(red, green, blue, alpha);
            Sprites.blit(pose, icon, ctx.x, ctx.y, ctx.width, ctx.height);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        @Override
        public IconRenderer withColor(int color) {
            this.color = color;
            return this;
        }

        public IconRenderer withShadow() {
            this.shadow = true;
            return this;
        }
    }

    public static final class TextWithIconRenderer implements Colorable {
        private final TextRenderer textRenderer;
        private final IconRenderer iconRenderer;
        private int gap = 4;
        private int iconAlign = 1;
        private int iconSize = 10;

        TextWithIconRenderer(TextRenderer text, IconRenderer icon) {
            this.textRenderer = text;
            text.withAlignment(0f);
            this.iconRenderer = icon;
        }

        @Override
        public void render(PoseStack pose, Ctx ctx, float pt) {
            int iconSpace = iconSize + gap;
            textRenderer.withPadding(0, iconAlign * iconSpace, 0, (1 - iconAlign) * iconSpace).render(pose, ctx, pt);
            int textSpace = ctx.width - iconSpace;
            iconRenderer.withCentered(iconSize, iconSize)
                    .withPadding(0, (1 - iconAlign) * textSpace, 0, iconAlign * textSpace)
                    .render(pose, ctx, pt);
        }

        @Override
        public TextWithIconRenderer withColor(int color) {
            textRenderer.withColor(color);
            iconRenderer.withColor(color);
            return this;
        }

        public TextWithIconRenderer withIconSize(int size) {
            this.iconSize = size;
            return this;
        }

        public TextWithIconRenderer withTextLeftIconRight() {
            textRenderer.withAlignment(0f);
            iconAlign = 1;
            return this;
        }

        public TextWithIconRenderer withTextLeftIconLeft() {
            textRenderer.withAlignment(0f);
            iconAlign = 0;
            return this;
        }
    }

    public static final class SolidRenderer implements Colorable {
        private int color;
        private boolean ignoreAlpha;

        @Override
        public void render(PoseStack pose, Ctx ctx, float pt) {
            int c = ignoreAlpha ? color | 0xFF000000 : color;
            Gfx.fill(pose, ctx.x, ctx.y, ctx.x + ctx.width, ctx.y + ctx.height, c);
        }

        @Override
        public SolidRenderer withColor(int color) {
            this.color = color;
            return this;
        }

        public SolidRenderer withoutAlpha() {
            this.ignoreAlpha = true;
            return this;
        }
    }
}
