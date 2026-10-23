package net.ribs.vintagedelight.compat;

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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.recipe.FermentingRecipe;

// Upstream's JEI category on the JEI 11 API. The title reads the block's lang name (Pickling Jar).
public class FermentingCategory implements IRecipeCategory<FermentingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation("vintagedelight", "fermenting");
    public static final ResourceLocation TEXTURE = new ResourceLocation("vintagedelight", "textures/gui/fermenting_jar_gui.png");
    public static final RecipeType<FermentingRecipe> FERMENTING_TYPE = new RecipeType<>(UID, FermentingRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public FermentingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 10, 10, 154, 65);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.FERMENTING_JAR.get()));
    }

    @Override
    public RecipeType<FermentingRecipe> getRecipeType() {
        return FERMENTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.vintagedelight.fermenting_jar");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FermentingRecipe recipe, IFocusGroup focuses) {
        int inputBaseX = 6;
        int firstRowY = 7;
        int secondRowY = firstRowY + 18;
        int inputSpacing = 18;
        int outputOffsetX = 90;
        int secondaryOutputX = outputOffsetX + inputSpacing;
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            int row = i / 3;
            int col = i % 3;
            int x = inputBaseX + col * inputSpacing + 10;
            int y = row == 0 ? firstRowY + 10 : secondRowY + 10;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(recipe.getIngredients().get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, inputBaseX + outputOffsetX + 10, secondRowY + 10).addItemStack(recipe.getResultItem());
        if (!recipe.getSecondaryResultItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, inputBaseX + secondaryOutputX + 10, secondRowY + 10).addItemStack(recipe.getSecondaryResultItem());
        }
        if (recipe.hasContainer()) {
            // Port: every item the container ingredient accepts (upstream showed only the first)
            builder.addSlot(RecipeIngredientRole.INPUT, inputBaseX + 3 * inputSpacing + 2 + 10, firstRowY + 10).addIngredients(recipe.getContainerIngredient());
        }
    }

    @Override
    public void draw(FermentingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
        int processingTime = recipe.getProcessingTime();
        if (processingTime > 0) {
            String text = String.format("%dmin", processingTime / 1200);
            Minecraft.getInstance().font.draw(poseStack, text, 110, 20, java.awt.Color.gray.getRGB());
        }
    }
}
