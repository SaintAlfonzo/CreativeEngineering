package com.SaintAlfonzo.more_mooblooms.compat.jei;

import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * "Moobloom Breeding": parent egg + parent egg -> calf egg, with the chance (4.8). One recipe per combination
 * result; the chance is the rule's chance times the result's share of the weights.
 */
public class MoobloomBreedingCategory implements IRecipeCategory<MoobloomBreedingCategory.Recipe> {

    public record Recipe(ItemStack parentA, ItemStack parentB, ItemStack child, float chance) {}

    public static final RecipeType<Recipe> TYPE = RecipeType.create(MoreMooblooms.ID, "breeding", Recipe.class);

    private static final int WIDTH = 132, HEIGHT = 26;
    private static final int SLOT_Y = 5;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawable icon;

    public MoobloomBreedingCategory(IGuiHelper gui) {
        background = gui.createBlankDrawable(WIDTH, HEIGHT);
        slot = gui.getSlotDrawable();
        icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MoobloomSpawnEggItem.MOOBLOOM_SPAWN_EGG.get()));
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.more_mooblooms.breeding");
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
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, SLOT_Y).addItemStack(recipe.parentA()).setBackground(slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.INPUT, 31, SLOT_Y).addItemStack(recipe.parentB()).setBackground(slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, SLOT_Y).addItemStack(recipe.child()).setBackground(slot, -1, -1);
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        font.draw(pose, "+", 21, SLOT_Y + 5, 0x404040);
        font.draw(pose, "→", 59, SLOT_Y + 5, 0x404040);
        font.draw(pose, MoreMoobloomsJeiPlugin.percent(recipe.chance()) + "%", 103, SLOT_Y + 5, 0x404040);
    }
}
