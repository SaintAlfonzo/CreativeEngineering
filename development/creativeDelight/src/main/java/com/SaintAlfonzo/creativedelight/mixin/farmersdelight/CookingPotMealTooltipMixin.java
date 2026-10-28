package com.SaintAlfonzo.creativedelight.mixin.farmersdelight;

import com.SaintAlfonzo.creativeengineering.compat.MealSlotTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;

import java.util.List;

/**
 * Farmer's Delight cooking pot: the finished-meal slot's tooltip gets its stack, see {@link MealSlotTooltip} (patcherMod, shared with the
 * Create Central Kitchen guide mixins there). Client only.
 */
@Mixin(value = CookingPotScreen.class, remap = false)
public class CookingPotMealTooltipMixin {

    @Redirect(method = "renderMealDisplayTooltip", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lvectorwing/farmersdelight/client/gui/CookingPotScreen;m_96597_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"))
    private void ce$mealTooltipWithStack(CookingPotScreen screen, PoseStack poseStack, List<Component> lines, int x, int y) {
        MealSlotTooltip.render(screen, poseStack, lines, x, y);
    }
}
