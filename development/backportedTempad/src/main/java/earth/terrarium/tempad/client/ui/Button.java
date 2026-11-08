// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.buttons.Button.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Button extends KitWidget {
    /** Olympus UIConstants.BUTTON, the default texture. */
    public static final WidgetSprites OLYMPUS_BUTTON = new WidgetSprites(olympus("buttons/normal"), olympus("buttons/disabled"), olympus("buttons/hovered"));
    public static final WidgetSprites OLYMPUS_DANGER = new WidgetSprites(olympus("buttons/danger/normal"), olympus("buttons/disabled"), olympus("buttons/danger/hovered"));

    @Nullable
    private WidgetSprites sprites = OLYMPUS_BUTTON;
    private WidgetRenderer renderer = WidgetRenderer.EMPTY;
    private Runnable callback = () -> {
    };

    public Button() {
        super(0, 0);
    }

    public static ResourceLocation olympus(String path) {
        return new ResourceLocation("olympus", path);
    }

    public Button withTexture(@Nullable WidgetSprites sprites) {
        this.sprites = sprites;
        return this;
    }

    public Button withRenderer(WidgetRenderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Button withCallback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        if (sprites != null) {
            Sprites.blit(pose, sprites.get(active, isHoveredOrFocused()), x, y, width, height);
        }
        renderer.render(pose, new Ctx(this, mouseX, mouseY), partialTick);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        callback.run();
    }
}
