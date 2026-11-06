package com.SaintAlfonzo.creativedelight.mixin.create_central_kitchen;

import com.SaintAlfonzo.creativedelight.compat.MealSlotTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import plus.dragons.createcentralkitchen.content.logistics.item.guide.cooking.CookingGuideScreen;

import java.util.List;

/** Create Central Kitchen cooking guide: the finished-meal slot's tooltip gets its stack, see {@link MealSlotTooltip}. */
@Mixin(value = CookingGuideScreen.class, remap = false)
public class CckCookingGuideMealTooltipMixin {

    @Redirect(method = "m_7025_", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lplus/dragons/createcentralkitchen/content/logistics/item/guide/cooking/CookingGuideScreen;m_96597_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"))
    private void creativedelight$mealTooltipWithStack(CookingGuideScreen screen, PoseStack poseStack, List<Component> lines, int x, int y) {
        MealSlotTooltip.render(screen, poseStack, lines, x, y);
    }
}
