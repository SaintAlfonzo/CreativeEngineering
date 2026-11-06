package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.compat.MealSlotTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.farmersrespite.client.gui.KettleScreen;

import java.util.List;

/**
 * Farmer's Respite kettle: the finished-meal slot's tooltip gets its stack, see {@link MealSlotTooltip} (patcherMod, shared with the
 * Create Central Kitchen guide mixins there). Client only.
 */
@Mixin(value = KettleScreen.class, remap = false)
public class KettleMealTooltipMixin {

    @Redirect(method = "renderMealDisplayTooltip", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lumpaz/farmersrespite/client/gui/KettleScreen;m_96597_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"))
    private void ce$mealTooltipWithStack(KettleScreen screen, PoseStack poseStack, List<Component> lines, int x, int y) {
        MealSlotTooltip.render(screen, poseStack, lines, x, y);
    }
}
