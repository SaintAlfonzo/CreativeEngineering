// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface WidgetRenderer {
    WidgetRenderer EMPTY = (pose, ctx, partialTick) -> {
    };

    void render(PoseStack pose, Ctx ctx, float partialTick);

    default WidgetRenderer withPadding(int padding) {
        return withPadding(padding, padding, padding, padding);
    }

    default WidgetRenderer withPadding(int vertical, int horizontal) {
        return withPadding(vertical, horizontal, vertical, horizontal);
    }

    default WidgetRenderer withPadding(int top, int right, int bottom, int left) {
        WidgetRenderer self = this;
        return (pose, ctx, pt) -> self.render(pose, ctx.with(ctx.x + left, ctx.y + top, ctx.width - left - right, ctx.height - top - bottom), pt);
    }

    default WidgetRenderer withPaddingBottom(int padding) {
        return withPadding(0, 0, padding, 0);
    }

    default WidgetRenderer withCentered(int width, int height) {
        WidgetRenderer self = this;
        return (pose, ctx, pt) -> self.render(pose, ctx.with(ctx.x + (ctx.width - width) / 2, ctx.y + (ctx.height - height) / 2, width, height), pt);
    }
}
