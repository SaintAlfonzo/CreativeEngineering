// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium), earth.terrarium.olympus.client.components.base.BaseWidget,
// on 1.19.2's AbstractWidget (no 1.19.3 Tooltip: the screen draws the hovered widget's tooltip, see TooltipSource).
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class KitWidget extends AbstractWidget implements TooltipSource {
    @Nullable
    private Component tooltip;

    protected KitWidget(int width, int height) {
        super(0, 0, width, height, CommonComponents.EMPTY);
    }

    public KitWidget withTooltip(@Nullable Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public KitWidget withSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public KitWidget withSize(int size) {
        return withSize(size, size);
    }

    public KitWidget withPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        isHovered = Gfx.inScissor(mouseX, mouseY) && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        renderWidget(pose, mouseX, mouseY, partialTick);
    }

    protected abstract void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick);

    @Nullable
    @Override
    public Component tooltipAt(double mouseX, double mouseY) {
        return visible && isHovered && tooltip != null ? tooltip : null;
    }

    /** Olympus widgets click silently. */
    @Override
    public void playDownSound(SoundManager manager) {
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }
}
