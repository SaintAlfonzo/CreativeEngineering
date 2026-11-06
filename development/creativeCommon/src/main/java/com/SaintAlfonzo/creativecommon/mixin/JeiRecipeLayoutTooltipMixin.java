package com.SaintAlfonzo.creativecommon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.recipes.RecipeLayout;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
 *
 * <p>JEI picks the first slot under the mouse, so a fluid tank drawn over a RENDER_ONLY item icon (Brewin's
 * keg: drink fluid over the tankard it pours into) always shows the fluid. When the hovered slot shows no
 * item, the stack of a RENDER_ONLY item slot under the same point is attached instead: the text stays the
 * fluid's, and AppleSkin gets the drink item.
 */
@Mixin(value = RecipeLayout.class, remap = false)
public class JeiRecipeLayoutTooltipMixin {

    @Shadow
    @Final
    private List<IRecipeSlotDrawable> recipeCategorySlots;

    @Shadow
    private ImmutableRect2i area;

    @Unique
    @Nullable
    private ITypedIngredient<?> creativecommon$hoveredIngredient;

    @Unique
    private int creativecommon$mouseX;

    @Unique
    private int creativecommon$mouseY;

    @Inject(method = "drawOverlays", remap = false, at = @At("HEAD"))
    private void creativecommon$rememberMouse(PoseStack poseStack, int mouseX, int mouseY, CallbackInfo ci) {
        this.creativecommon$mouseX = mouseX;
        this.creativecommon$mouseY = mouseY;
    }

    @Redirect(method = "drawOverlays", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/api/gui/ingredient/IRecipeSlotDrawable;getTooltip()Ljava/util/List;"))
    private List<Component> creativecommon$rememberIngredient(IRecipeSlotDrawable slot) {
        ITypedIngredient<?> ingredient = slot.getDisplayedIngredient().orElse(null);
        if (ingredient != null && ingredient.getItemStack().isEmpty()) {
            ITypedIngredient<?> under = creativecommon$renderOnlyItemUnderMouse(slot);
            if (under != null) {
                ingredient = under;
            }
        }
        this.creativecommon$hoveredIngredient = ingredient;
        return slot.getTooltip();
    }

    @Redirect(method = "drawOverlays", remap = false, require = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lmezz/jei/common/gui/JeiTooltip;draw(Lcom/mojang/blaze3d/vertex/PoseStack;II)V"))
    private void creativecommon$drawWithIngredient(JeiTooltip tooltip, PoseStack poseStack, int x, int y) {
        if (this.creativecommon$hoveredIngredient != null) {
            tooltip.setIngredient(this.creativecommon$hoveredIngredient);
            this.creativecommon$hoveredIngredient = null;
        }
        tooltip.draw(poseStack, x, y);
    }

    /** Slot rects are recipe-relative, as in {@code RecipeLayout.getSlotUnderMouse}. */
    @Unique
    @Nullable
    private ITypedIngredient<?> creativecommon$renderOnlyItemUnderMouse(IRecipeSlotDrawable hovered) {
        double recipeX = this.creativecommon$mouseX - this.area.getX();
        double recipeY = this.creativecommon$mouseY - this.area.getY();
        for (IRecipeSlotDrawable other : this.recipeCategorySlots) {
            if (other == hovered || other.getRole() != RecipeIngredientRole.RENDER_ONLY || !other.isMouseOver(recipeX, recipeY)) {
                continue;
            }
            ITypedIngredient<?> ingredient = other.getDisplayedIngredient().orElse(null);
            if (ingredient != null && ingredient.getItemStack().isPresent()) {
                return ingredient;
            }
        }
        return null;
    }
}
