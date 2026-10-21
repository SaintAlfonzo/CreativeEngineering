package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.wares.client.gui.agreement.AgreementScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

/**
 * Wares (1.2.9) delivery agreement: {@code AgreementScreen.renderTooltip} builds the hovered requested/payment
 * item's tooltip (plus the display slot's extra line) and draws it with the stackless
 * {@code renderTooltip(pose, lines, image, x, y)}, so AppleSkin adds no food bars. Same lines, drawn with
 * Forge's overload that carries the hovered slot's stack.
 */
@Mixin(value = AgreementScreen.class, remap = false)
public class WaresAgreementTooltipMixin {

    @Redirect(method = "m_7025_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lio/github/mortuusars/wares/client/gui/agreement/AgreementScreen;m_169388_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void creativeengineering$tooltipWithStack(AgreementScreen screen, PoseStack poseStack, List<Component> lines,
                                                      Optional<TooltipComponent> image, int x, int y) {
        Slot slot = screen.getSlotUnderMouse();
        screen.renderTooltip(poseStack, lines, image, x, y, slot != null ? slot.getItem() : ItemStack.EMPTY);
    }
}
