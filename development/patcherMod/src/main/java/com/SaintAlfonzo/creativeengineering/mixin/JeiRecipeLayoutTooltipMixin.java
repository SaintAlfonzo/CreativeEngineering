package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * JEI 11.8.1 recipe-slot tooltips lose their item, so AppleSkin (and anything else reading
 * {@code RenderTooltipEvent.GatherComponents#getItemStack}) sees {@code ItemStack.EMPTY}.
 *
 * <p>{@code RecipeLayout.drawOverlays} builds {@code new JeiTooltip()}, copies only the text lines in with
 * {@code addAll(hoveredSlot.getTooltip())} and draws it. The ingredient list does the same but also calls
 * {@code setIngredient}, which makes {@code JeiTooltip.draw} pass the stack (and its tooltip image) to
 * Forge. The first redirect remembers the hovered slot's displayed ingredient; the second attaches it to
 * the tooltip just before drawing. The category-tooltip branch never hits the first redirect, so it draws
 * as before. Catalyst slots are handled by {@link JeiRecipesGuiTooltipMixin}.
 */
@Mixin(value = RecipeLayout.class, remap = false)
public class JeiRecipeLayoutTooltipMixin {

    @Unique
    @Nullable
    private ITypedIngredient<?> creativeengineering$hoveredIngredient;

    @Redirect(method = "drawOverlays", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/api/gui/ingredient/IRecipeSlotDrawable;getTooltip()Ljava/util/List;"))
    private List<Component> creativeengineering$rememberIngredient(IRecipeSlotDrawable slot) {
        this.creativeengineering$hoveredIngredient = slot.getDisplayedIngredient().orElse(null);
        return slot.getTooltip();
    }

    @Redirect(method = "drawOverlays", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/common/gui/JeiTooltip;draw(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    private void creativeengineering$drawWithIngredient(JeiTooltip tooltip, PoseStack poseStack, int x, int y) {
        if (this.creativeengineering$hoveredIngredient != null) {
            tooltip.setIngredient(this.creativeengineering$hoveredIngredient);
            this.creativeengineering$hoveredIngredient = null;
        }
        tooltip.draw(poseStack, x, y);
    }
}
