package com.SaintAlfonzo.creativecompat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.patchouli.client.book.gui.GuiBook;

import java.util.List;

/**
 * Patchouli (1.19.2-77) books: {@code GuiBook.drawTooltip} turns the hovered item into tooltip lines (plus the
 * "shift for recipe" line) and draws them with the stackless {@code renderComponentTooltip(pose, lines, x, y)},
 * so AppleSkin adds no food bars to items shown in books. The first call (the item branch) gets the stack;
 * the second, plain text tooltips, is left alone.
 */
@Mixin(value = GuiBook.class, remap = false)
public abstract class PatchouliBookTooltipMixin {

    @Shadow
    private ItemStack tooltipStack;

    @Redirect(method = "drawTooltip", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false, ordinal = 0,
                    target = "Lvazkii/patchouli/client/book/gui/GuiBook;m_96597_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"))
    private void creativecommon$itemTooltipWithStack(GuiBook book, PoseStack poseStack, List<Component> lines, int x, int y) {
        book.renderComponentTooltip(poseStack, lines, x, y, this.tooltipStack);
    }
}
