package com.sammy.minersdelight.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sammy.minersdelight.MinersDelightMod;
import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import com.sammy.minersdelight.registry.MDItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Arrays;
import java.util.List;

/** FD cooking recipes with up to four ingredients, as the Copper Pot cooks and serves them. */
public class CopperPotCategory implements IRecipeCategory<CookingPotRecipe> {
    public static final RecipeType<CookingPotRecipe> TYPE = RecipeType.create(MinersDelightMod.MODID, "cooking", CookingPotRecipe.class);
    private static final ResourceLocation POT_GUI = MinersDelightMod.path("textures/gui/copper_pot.png");
    private static final ResourceLocation FD_GUI = new ResourceLocation("farmersdelight", "textures/gui/cooking_pot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable heat;
    private final IDrawable time;
    private final IDrawable experience;
    private final IDrawableAnimated arrow;

    public CopperPotCategory(IGuiHelper helper) {
        background = helper.createDrawable(POT_GUI, 29, 16, 117, 57);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(MDItems.COPPER_POT.get()));
        heat = helper.createDrawable(POT_GUI, 176, 0, 17, 15);
        time = helper.createDrawable(FD_GUI, 176, 32, 8, 11);
        experience = helper.createDrawable(FD_GUI, 176, 43, 9, 9);
        arrow = helper.drawableBuilder(POT_GUI, 176, 15, 29, 17).buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<CookingPotRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return MDItems.COPPER_POT.get().getDescription();
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
    public void setRecipe(IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < Math.min(ingredients.size(), CopperPotBlockEntity.INPUT_SLOTS); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 11 + (i % 2) * 18, 2 + (i / 2) * 18)
                    .addItemStacks(Arrays.asList(ingredients.get(i).getItems()));
        }
        ItemStack result = CopperPotBlockEntity.servedResult(recipe);
        ItemStack container = CopperPotBlockEntity.servedContainer(recipe);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 12).addItemStack(result);
        if (!container.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 54, 40).addItemStack(container);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 40).addItemStack(result);
    }

    @Override
    public void draw(CookingPotRecipe recipe, IRecipeSlotsView slots, PoseStack pose, double mouseX, double mouseY) {
        arrow.draw(pose, 48, 11);
        heat.draw(pose, 19, 40);
        time.draw(pose, 58, 4);
        if (recipe.getExperience() > 0.0F) {
            experience.draw(pose, 57, 23);
        }
    }
}
