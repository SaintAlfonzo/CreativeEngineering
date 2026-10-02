package io.github.mortuusars.exposure.backport.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.NotNull;

/** Backport of 1.19.3+ {@code Renderable} (1.19.2: {@link Widget}), drawing with {@link GuiGraphics}. */
public interface Renderable extends Widget {
    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);

    @Override
    default void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        render(new GuiGraphics(poseStack), mouseX, mouseY, partialTick);
    }
}
