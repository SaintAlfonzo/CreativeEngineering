package com.SaintAlfonzo.creativeengineering.compat.vinery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Grape stomping in the Grapevine Pot ({@code vinery:grapevine_pot}); hardcoded in
 * GrapevinePotBlock, and Vinery's REI plugin never showed it. A full pot is 6 grapes; landing on
 * it 3 times finishes the stomp, then each empty wine bottle takes 3 grapes' worth of juice, so a
 * full pot gives 2 bottles. The juice follows the first grape put in. Same REI style as the
 * Apple Press page, with one line of instructions under it.
 */
@ParametersAreNonnullByDefault
class GrapeStompingCategory extends VineryCategory<GrapeStompingCategory.Display> {
    static final RecipeType<Display> TYPE =
            new RecipeType<>(new ResourceLocation(CreativeEngineering.MODID, "vinery_grape_stomping"), Display.class);

    private static final int WIDTH = 120;
    /** Row is 101 wide (grape, bottle, arrow, large result); centred in the page. */
    private static final int X0 = (WIDTH - 101) / 2;
    private static final int GRAPE_X = X0 + 1, BOTTLE_X = X0 + 19, ROW_Y = 5;
    private static final int ARROW_X = X0 + 42, ARROW_Y = 4;
    private static final int RESULT_X = X0 + 80;
    private static final int TEXT_Y = 31;

    record Display(ItemStack grape, ItemStack juice) {
    }

    private final IDrawableAnimated arrow;

    GrapeStompingCategory(IGuiHelper gui) {
        super(gui, TYPE, Component.translatable("creativeengineering.jei.vinery.grape_stomping"),
                VineryIds.GRAPEVINE_POT, WIDTH, 40);
        // No timer in the block; animate at the Apple Press's pace so the page doesn't look frozen.
        this.arrow = animatedArrow(VineryIds.PRESS_TICKS);
    }

    static List<Display> collect() {
        List<Display> out = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ResourceLocation> e : VineryIds.GRAPE_JUICES.entrySet()) {
            ItemStack grape = VineryIds.stack(e.getKey());
            ItemStack juice = VineryIds.stack(e.getValue());
            if (!grape.isEmpty() && !juice.isEmpty()) {
                out.add(new Display(grape, juice));
            }
        }
        return out;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        int bottles = VineryIds.POT_GRAPES / VineryIds.GRAPES_PER_BOTTLE;
        builder.addSlot(RecipeIngredientRole.INPUT, GRAPE_X, ROW_Y)
                .addItemStack(withCount(recipe.grape(), VineryIds.POT_GRAPES))
                .addTooltipCallback((slot, tooltip) -> tooltip.add(
                        Component.translatable("creativeengineering.jei.vinery.grape_stomping.first_grape")
                                .withStyle(ChatFormatting.GRAY)));
        ItemStack bottle = VineryIds.stack(VineryIds.WINE_BOTTLE, bottles);
        if (!bottle.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, BOTTLE_X, ROW_Y)
                    .addItemStack(bottle)
                    .addTooltipCallback((slot, tooltip) -> tooltip.add(
                            Component.translatable("creativeengineering.jei.vinery.grape_stomping.per_bottle",
                                    VineryIds.GRAPES_PER_BOTTLE).withStyle(ChatFormatting.GRAY)));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, ROW_Y)
                .addItemStack(withCount(recipe.juice(), bottles));
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView view, PoseStack pose, double mouseX, double mouseY) {
        drawSlot(pose, GRAPE_X, ROW_Y);
        drawSlot(pose, BOTTLE_X, ROW_Y);
        drawArrow(pose, arrow, ARROW_X, ARROW_Y);
        drawBigSlot(pose, RESULT_X, ROW_Y);
        drawCentered(pose, Component.translatable("creativeengineering.jei.vinery.grape_stomping.jumps",
                VineryIds.POT_JUMPS), WIDTH / 2, TEXT_Y);
    }
}
