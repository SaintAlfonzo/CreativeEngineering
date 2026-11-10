package com.SaintAlfonzo.creativedelight.hearth.machine.client;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.machine.HHMachines;
import com.SaintAlfonzo.creativedelight.hearth.machine.StompingRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

/** JEI page of the Stomping Basin, laid out like 1.4.1's. Loaded only through HHJeiPlugin. */
public class StompingCategory implements IRecipeCategory<StompingRecipe> {
    public static final RecipeType<StompingRecipe> TYPE = RecipeType.create(HearthAndHarvestMod.MODID, "stomping", StompingRecipe.class);
    private static final ResourceLocation TEXTURE = new ResourceLocation(HearthAndHarvestMod.MODID, "textures/gui/jei/jei_stomping_gui.png");
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public StompingCategory(IGuiHelper helper) {
        background = helper.createDrawable(TEXTURE, 33, 16, 116, 42);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HHMachines.STOMPING_BASIN_ITEM.get()));
        IDrawableStatic arrowStatic = helper.drawableBuilder(TEXTURE, 176, 15, 24, 17).build();
        arrow = helper.createAnimatedDrawable(arrowStatic, 200, mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<StompingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("hearthandharvest.jei.stomping");
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
    public void setRecipe(IRecipeLayoutBuilder builder, StompingRecipe recipe, IFocusGroup focuses) {
        List<Group> grouped = group(recipe.getIngredients());
        int[] cols = {6, 25};
        int[] rows = {4, 22};
        for (int i = 0; i < grouped.size() && i < 4; i++) {
            Group entry = grouped.get(i);
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack stack : entry.stacks) {
                ItemStack copy = stack.copy();
                copy.setCount(entry.count);
                stacks.add(copy);
            }
            builder.addSlot(RecipeIngredientRole.INPUT, cols[i % 2], rows[i / 2]).addItemStacks(stacks);
        }
        FluidStack fluid = recipe.resultFluid();
        if (!fluid.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 24).addIngredient(ForgeTypes.FLUID_STACK, fluid).setFluidRenderer(1000L, false, 26, 16);
        }
        if (!recipe.getResultItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 2).addItemStack(recipe.getResultItem());
        }
    }

    @Override
    public void draw(StompingRecipe recipe, IRecipeSlotsView slots, PoseStack stack, double mouseX, double mouseY) {
        arrow.draw(stack, 48, 20);
    }

    @Override
    public List<Component> getTooltipStrings(StompingRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        return mouseX >= 48 && mouseX <= 72 && mouseY >= 2 && mouseY <= 40 ? List.of(Component.translatable("hearthandharvest.jei.stomping.tooltip")) : List.of();
    }

    private static List<Group> group(List<Ingredient> ingredients) {
        List<Group> grouped = new ArrayList<>();
        outer:
        for (Ingredient ingredient : ingredients) {
            List<ItemStack> incoming = Arrays.asList(ingredient.getItems());
            for (Group entry : grouped) {
                if (same(entry.stacks, incoming)) {
                    entry.count++;
                    continue outer;
                }
            }
            grouped.add(new Group(new ArrayList<>(incoming)));
        }
        return grouped;
    }

    private static boolean same(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSame(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    private static final class Group {
        final List<ItemStack> stacks;
        int count = 1;

        Group(List<ItemStack> stacks) {
            this.stacks = stacks;
        }
    }
}
