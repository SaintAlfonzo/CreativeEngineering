package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Same fix as {@link JeiRecipeLayoutTooltipMixin} for the recipe catalyst column on the left of the JEI
 * recipe screen. In 11.8.1 the catalyst tooltip is drawn by the static lambda
 * {@code RecipesGui.lambda$render$6(PoseStack, int, int, IRecipeSlotDrawable)}:
 * {@code new JeiTooltip()}, {@code addAll(slot.getTooltip())}, {@code draw(...)}, without the ingredient.
 * The handlers are static because the lambda is; JEI draws on the render thread only.
 */
@Mixin(value = RecipesGui.class, remap = false)
public class JeiRecipesGuiTooltipMixin {

    @Unique
    @Nullable
    private static ITypedIngredient<?> creativeengineering$hoveredCatalyst;

    @Redirect(method = "lambda$render$6", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/api/gui/ingredient/IRecipeSlotDrawable;getTooltip()Ljava/util/List;"))
    private static List<Component> creativeengineering$rememberCatalyst(IRecipeSlotDrawable slot) {
        creativeengineering$hoveredCatalyst = slot.getDisplayedIngredient().orElse(null);
        return slot.getTooltip();
    }

    @Redirect(method = "lambda$render$6", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/common/gui/JeiTooltip;draw(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    private static void creativeengineering$drawWithCatalyst(JeiTooltip tooltip, PoseStack poseStack, int x, int y) {
        if (creativeengineering$hoveredCatalyst != null) {
            tooltip.setIngredient(creativeengineering$hoveredCatalyst);
            creativeengineering$hoveredCatalyst = null;
        }
        tooltip.draw(poseStack, x, y);
    }
}
