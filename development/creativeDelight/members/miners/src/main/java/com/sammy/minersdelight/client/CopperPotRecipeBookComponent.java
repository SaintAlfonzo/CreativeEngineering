package com.sammy.minersdelight.client;

import com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;

import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.CONTAINER_SLOT;
import static com.sammy.minersdelight.content.block.copper_pot.CopperPotBlockEntity.MEAL_DISPLAY_SLOT;

/** FD's cooking recipe book on the Copper Pot; the ghost shows the result and container as the pot serves them. */
public class CopperPotRecipeBookComponent extends RecipeBookComponent {
    private static final ResourceLocation BUTTONS = new ResourceLocation("farmersdelight", "textures/gui/recipe_book_buttons.png");

    @Override
    protected void initFilterButtonTextures() {
        filterButton.initTextureValues(0, 0, 28, 18, BUTTONS);
    }

    public void hide() {
        setVisible(false);
    }

    @Override
    @NotNull
    protected Component getRecipeFilterName() {
        return TextUtils.getTranslation("container.recipe_book.cookable");
    }

    @Override
    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
        ghostRecipe.setRecipe(recipe);
        if (recipe instanceof CookingPotRecipe cooking) {
            Slot meal = slots.get(MEAL_DISPLAY_SLOT);
            if (meal.getItem().isEmpty()) {
                ghostRecipe.addIngredient(Ingredient.of(CopperPotBlockEntity.servedResult(cooking)), meal.x, meal.y);
            }
            ItemStack container = CopperPotBlockEntity.servedContainer(cooking);
            if (!container.isEmpty()) {
                Slot containerSlot = slots.get(CONTAINER_SLOT);
                ghostRecipe.addIngredient(Ingredient.of(container), containerSlot.x, containerSlot.y);
            }
        }
        placeRecipe(menu.getGridWidth(), menu.getGridHeight(), menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
    }
}
