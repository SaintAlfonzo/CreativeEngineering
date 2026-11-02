package com.SaintAlfonzo.creativedelight.barbeques.compat.jei;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.BarbequesDelight;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.GrillingRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.SkeweringRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQItems;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Arrays;

/**
 * The Grilling and Skewering categories (1.3.0 BBQDJeiPlugin). Port fixes: the Skewering page has its own title (1.3.0
 * showed "Grilling" on both), and the time line shows the time for each side (half the recipe time; 1.3.0 showed the
 * whole time).
 */
@JeiPlugin
public class BBQJeiPlugin implements IModPlugin {
    public static final RecipeType<GrillingRecipe> GRILLING = RecipeType.create(BarbequesDelight.MODID, "grilling", GrillingRecipe.class);
    public static final RecipeType<SkeweringRecipe> SKEWERING = RecipeType.create(BarbequesDelight.MODID, "skewering", SkeweringRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return BarbequesDelight.loc("main");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new Grill(gui), new Basin(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(GRILLING, rm.getAllRecipesFor(BBQRecipes.GRILLING.get()));
        registration.addRecipes(SKEWERING, rm.getAllRecipesFor(BBQRecipes.SKEWERING.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BBQItems.GRILL.get()), GRILLING);
        registration.addRecipeCatalyst(new ItemStack(BBQItems.BASIN.get()), SKEWERING);
    }

    static ItemStack withCount(ItemStack stack, int count) {
        ItemStack ans = stack.copy();
        ans.setCount(Math.max(1, count));
        return ans;
    }

    static final class Grill implements IRecipeCategory<GrillingRecipe> {
        private final IDrawable background;
        private final IDrawable icon;

        Grill(IGuiHelper gui) {
            background = gui.createDrawable(BarbequesDelight.loc("textures/gui/grill.png"), 0, 0, 118, 58);
            icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BBQItems.GRILL.get()));
        }

        @Override
        public RecipeType<GrillingRecipe> getRecipeType() {
            return GRILLING;
        }

        @Override
        public Component getTitle() {
            return BBQLang.JEI_GRILLING.get();
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
        public void setRecipe(IRecipeLayoutBuilder builder, GrillingRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 16, 22).addIngredients(recipe.ingredient);
            String perSide = String.valueOf(recipe.barbecuingTime / 2 / 20.0F).replaceAll("\\.0$", "");
            builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 22).addItemStack(recipe.output)
                    .addTooltipCallback((view, list) -> list.add(BBQLang.JEI_TIME.get(perSide)));
        }
    }

    static final class Basin implements IRecipeCategory<SkeweringRecipe> {
        private final IDrawable background;
        private final IDrawable icon;

        Basin(IGuiHelper gui) {
            background = gui.createDrawable(BarbequesDelight.loc("textures/gui/skewering.png"), 0, 0, 118, 58);
            icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BBQItems.BASIN.get()));
        }

        @Override
        public RecipeType<SkeweringRecipe> getRecipeType() {
            return SKEWERING;
        }

        @Override
        public Component getTitle() {
            return BBQLang.JEI_SKEWERING.get();
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
        public void setRecipe(IRecipeLayoutBuilder builder, SkeweringRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 13, 13).addIngredients(recipe.tool)
                    .addTooltipCallback((view, list) -> list.add(BBQLang.JEI_MAINHAND.get()));
            builder.addSlot(RecipeIngredientRole.INPUT, 13, 32)
                    .addItemStacks(Arrays.stream(recipe.side.getItems()).map(e -> withCount(e, recipe.sideCount)).toList())
                    .addTooltipCallback((view, list) -> list.add(recipe.sideCount == 0 ? BBQLang.JEI_EMPTY.get() : BBQLang.JEI_OFFHAND.get()));
            builder.addSlot(RecipeIngredientRole.INPUT, 33, 22)
                    .addItemStacks(Arrays.stream(recipe.ingredient.getItems()).map(e -> withCount(e, recipe.ingredientCount)).toList())
                    .addTooltipCallback((view, list) -> list.add(BBQLang.JEI_BASIN.get()));
            builder.addSlot(RecipeIngredientRole.OUTPUT, 83, 22).addItemStack(recipe.output);
        }
    }
}
