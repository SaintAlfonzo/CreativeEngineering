package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * The Apple Press ({@code vinery:wine_press}). Its only "recipe" is hardcoded in
 * WinePressBlockEntity: one apple -> one apple mash every 72 ticks. Layout copies Vinery's REI
 * WinePressCategory (input, filling arrow, large result slot).
 */
@ParametersAreNonnullByDefault
class WinePressCategory extends VineryCategory<WinePressCategory.Display> {
    static final RecipeType<Display> TYPE =
            new RecipeType<>(new ResourceLocation(CreativeEngineering.MODID, "vinery_wine_press"), Display.class);

    /** REI points shifted by (0, +4) so the large result frame sits at the top edge. */
    private static final int INPUT_X = 1, INPUT_Y = 5;
    private static final int ARROW_X = 24, ARROW_Y = 4;
    private static final int RESULT_X = 62, RESULT_Y = 5;

    record Display(ItemStack input, ItemStack output) {
    }

    private final IDrawableAnimated arrow;

    WinePressCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable("creativeengineering.jei.vinery.wine_press"),
                VineryIds.WINE_PRESS, 83, 26);
        this.arrow = animatedArrow(VineryIds.PRESS_TICKS);
    }

    static List<Display> collect() {
        ItemStack mash = VineryIds.stack(VineryIds.APPLE_MASH);
        return mash.isEmpty() ? List.of() : List.of(new Display(new ItemStack(Items.APPLE), mash));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, INPUT_Y).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, RESULT_Y).addItemStack(recipe.output());
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY) {
        drawSlot(pose, INPUT_X, INPUT_Y);
        drawArrow(pose, arrow, ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, RESULT_Y);
    }

    @Override
    public List<Component> getTooltipStrings(Display recipe, IRecipeSlotsView view, double mouseX, double mouseY) {
        return tooltipIf(over(mouseX, mouseY, ARROW_X, ARROW_Y, ARROW_W, ARROW_H),
                ticksAsSeconds(VineryIds.PRESS_TICKS));
    }
}
