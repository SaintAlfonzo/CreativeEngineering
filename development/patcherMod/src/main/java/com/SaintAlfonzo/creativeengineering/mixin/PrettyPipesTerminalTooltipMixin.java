package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.prettypipes.misc.ItemTerminalWidget;
import de.ellpeck.prettypipes.terminal.containers.ItemTerminalGui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

/**
 * Pretty Pipes (1.13.6) item terminal: {@code ItemTerminalWidget.renderToolTip} takes the item's tooltip,
 * puts the stored count on the first line and draws it with the stackless
 * {@code renderTooltip(pose, lines, Optional.empty(), x, y)}, so AppleSkin adds no food bars to terminal
 * items. This draws the same lines with Forge's overload that carries the widget's stack.
 */
@Mixin(value = ItemTerminalWidget.class, remap = false)
public abstract class PrettyPipesTerminalTooltipMixin {

    @Shadow
    public ItemStack stack;

    @Redirect(method = "m_7428_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lde/ellpeck/prettypipes/terminal/containers/ItemTerminalGui;m_169388_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void creativeengineering$tooltipWithStack(ItemTerminalGui screen, PoseStack poseStack, List<Component> lines,
                                                      Optional<TooltipComponent> image, int x, int y) {
        screen.renderTooltip(poseStack, lines, image, x, y, this.stack);
    }
}
