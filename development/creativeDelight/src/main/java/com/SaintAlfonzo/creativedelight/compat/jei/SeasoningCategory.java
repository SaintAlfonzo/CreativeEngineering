package com.SaintAlfonzo.creativedelight.compat.jei;

import com.SaintAlfonzo.creativecompat.compat.jei.JeiArrow;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
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

/**
 * The Seasoning page: machine (crafting table or mixer) | food + seasoning(s) -> seasoned food. One page can stand for
 * a whole tag (Salt's "any saltable food + salt"): the food and output slots cycle together through a focus link, so
 * "uses of cooked beef" shows the page fixed on cooked beef.
 */
@ParametersAreNonnullByDefault
public class SeasoningCategory implements IRecipeCategory<SeasoningCategory.Display> {

    public static final RecipeType<Display> TYPE = RecipeType.create(CreativeDelight.MOD_ID, "seasoning", Display.class);

    /** Most input slots a page draws; longer crafts stay on the Crafting page. */
    public static final int MAX_INPUTS = 3;

    private static final int INPUT_X = 26;
    private static final int ARROW_X = INPUT_X + MAX_INPUTS * 18 + 4;
    private static final int OUTPUT_X = ARROW_X + JeiArrow.WIDTH + 6;
    private static final int Y = 9;

    /**
     * One page. {@code machine} is where the craft happens (a CATALYST slot); {@code inputs[0]} is the food; when
     * {@code linked}, {@code inputs[0]} and {@code outputs} are the same length and cycle in step.
     */
    public record Display(ResourceLocation id, ItemStack machine, List<List<ItemStack>> inputs, List<ItemStack> outputs,
                          boolean linked) {
    }

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;

    public SeasoningCategory(IGuiHelper helper, ItemStack iconStack) {
        background = helper.createBlankDrawable(OUTPUT_X + 18, 36);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
        slot = helper.getSlotDrawable();
        arrow = JeiArrow.create(helper);
    }

    @Override
    public RecipeType<Display> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("creativeengineering.jei.seasoning");
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
    public ResourceLocation getRegistryName(Display display) {
        return display.id();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display display, IFocusGroup focuses) {
        if (!display.machine().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 1, Y + 1).addItemStack(display.machine());
        }
        IRecipeSlotBuilder food = null;
        for (int n = 0; n < display.inputs().size(); n++) {
            IRecipeSlotBuilder input = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + n * 18 + 1, Y + 1)
                    .addItemStacks(display.inputs().get(n));
            if (n == 0) {
                food = input;
            }
        }
        IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, Y + 1)
                .addItemStacks(display.outputs());
        if (display.linked() && food != null) {
            builder.createFocusLink(food, output);
        }
    }

    @Override
    public void draw(Display display, IRecipeSlotsView slots, PoseStack poseStack, double mouseX, double mouseY) {
        if (!display.machine().isEmpty()) {
            slot.draw(poseStack, 0, Y);
        }
        for (int n = 0; n < display.inputs().size(); n++) {
            slot.draw(poseStack, INPUT_X + n * 18, Y);
        }
        arrow.draw(poseStack, ARROW_X, Y);
        slot.draw(poseStack, OUTPUT_X, Y);
    }
}
