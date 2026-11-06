package com.SaintAlfonzo.creativecommon.compat.jei;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativecommon.compat.PotionFluidTooltip;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * A page of several recipes that make or use the same thing, one row each: machine | inputs -> outputs. The Tipped
 * Arrows and Potion Buckets pages ({@link PotionPagesJEI}) put the hand craft, the Spout, the Item Drain and the
 * Bottling Machine for one potion on one page. Slots hold items or fluids; potion fluids get their effect lines.
 */
@ParametersAreNonnullByDefault
public class RowPagesCategory implements IRecipeCategory<RowPagesCategory.Page> {

    /** Most input / output slots a row draws; a recipe with more stays on its own page. */
    public static final int MAX_INPUTS = 3;
    public static final int MAX_OUTPUTS = 2;

    private static final int ROW_H = 20;
    private static final int INPUT_X = 24;
    private static final int ARROW_X = INPUT_X + MAX_INPUTS * 18 + 4;
    private static final int OUTPUT_X = ARROW_X + JeiArrow.WIDTH + 4;

    /** One slot: items or fluids (one of the two lists is empty). */
    public record Slot(List<ItemStack> items, List<FluidStack> fluids) {
        public static Slot items(List<ItemStack> items) {
            return new Slot(items, List.of());
        }

        public static Slot fluids(List<FluidStack> fluids) {
            return new Slot(List.of(), fluids);
        }
    }

    /** One recipe. {@code machine} is where it happens (a CATALYST slot). */
    public record Row(ItemStack machine, List<Slot> inputs, List<Slot> outputs) {
        public boolean fits() {
            return !inputs.isEmpty() && inputs.size() <= MAX_INPUTS && !outputs.isEmpty() && outputs.size() <= MAX_OUTPUTS;
        }
    }

    public record Page(ResourceLocation id, List<Row> rows) {
    }

    private final RecipeType<Page> type;
    private final String titleKey;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public RowPagesCategory(IGuiHelper helper, RecipeType<Page> type, String titleKey, IDrawable icon, int maxRows) {
        this.type = type;
        this.titleKey = titleKey;
        this.icon = icon;
        background = helper.createBlankDrawable(OUTPUT_X + MAX_OUTPUTS * 18, maxRows * ROW_H);
        slot = helper.getSlotDrawable();
        arrow = JeiArrow.create(helper);
    }

    @Override
    public RecipeType<Page> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(titleKey);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(Page page) {
        return page.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        for (int r = 0; r < page.rows().size(); r++) {
            Row row = page.rows().get(r);
            int y = r * ROW_H + 2;
            builder.addSlot(RecipeIngredientRole.CATALYST, 1, y).addItemStack(row.machine());
            for (int n = 0; n < row.inputs().size(); n++) {
                slot(builder, RecipeIngredientRole.INPUT, INPUT_X + n * 18 + 1, y, row.inputs().get(n));
            }
            for (int n = 0; n < row.outputs().size(); n++) {
                slot(builder, RecipeIngredientRole.OUTPUT, OUTPUT_X + n * 18 + 1, y, row.outputs().get(n));
            }
        }
    }

    private static void slot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, Slot contents) {
        IRecipeSlotBuilder slot = builder.addSlot(role, x, y);
        if (!contents.fluids().isEmpty()) {
            slot.addIngredients(ForgeTypes.FLUID_STACK, contents.fluids())
                    .setFluidRenderer(contents.fluids().get(0).getAmount(), false, 16, 16)
                    .addTooltipCallback(PotionFluidTooltip.POTION_EFFECTS);
        } else {
            slot.addItemStacks(contents.items());
        }
    }

    @Override
    public void draw(Page page, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        for (int r = 0; r < page.rows().size(); r++) {
            Row row = page.rows().get(r);
            int y = r * ROW_H + 1;
            slot.draw(poseStack, 0, y);
            for (int n = 0; n < row.inputs().size(); n++) {
                slot.draw(poseStack, INPUT_X + n * 18, y);
            }
            arrow.draw(poseStack, ARROW_X, y);
            for (int n = 0; n < row.outputs().size(); n++) {
                slot.draw(poseStack, OUTPUT_X + n * 18, y);
            }
        }
    }
}
