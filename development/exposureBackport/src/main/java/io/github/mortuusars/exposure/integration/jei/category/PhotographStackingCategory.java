package io.github.mortuusars.exposure.integration.jei.category;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.integration.jei.ExposureJeiPlugin;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import java.util.List;
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
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class PhotographStackingCategory implements IRecipeCategory<PhotographStackingJeiRecipe> {
   private static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/jei/photograph_stacking.png");
   private final Component title = Component.translatable("jei.exposure.photograph_stacking.title");
   private final IDrawable background;
   private final IDrawable icon;
   private final IDrawableStatic stackCursor;
   private final IDrawableStatic removeCursor;

   public PhotographStackingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 109, 38);
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack((ItemLike)Exposure.Items.STACKED_PHOTOGRAPHS.get()));
      this.stackCursor = guiHelper.createDrawable(TEXTURE, 109, 0, 20, 20);
      this.removeCursor = guiHelper.createDrawable(TEXTURE, 109, 38, 20, 20);
   }

   /** JEI 11 tooltip hook (JEI 19: getTooltip(ITooltipBuilder, ...)). */
   @NotNull
   public List<Component> getTooltipStrings(PhotographStackingJeiRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      List<Component> tooltip = new java.util.ArrayList<>();
      if (recipe.getType() == 0 && mouseX >= 10.0 && mouseX < 38.0 && mouseY >= 4.0 && mouseY < 28.0) {
         tooltip.add(Component.translatable("jei.exposure.photograph_stacking.stacking.tooltip"));
      }

      if (recipe.getType() == 1 && mouseX >= 10.0 && mouseX < 37.0 && mouseY >= 13.0 && mouseY < 35.0) {
         tooltip.add(Component.translatable("jei.exposure.photograph_stacking.removing.tooltip"));
      }
      return tooltip;
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull PhotographStackingJeiRecipe recipe, @NotNull IFocusGroup focuses) {
      if (recipe.getType() == 0) {
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
               .addItemStacks(
                  List.of(new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get()), new ItemStack((ItemLike)Exposure.Items.STACKED_PHOTOGRAPHS.get()))
               ))
            .setOverlay(this.stackCursor, 7, -6)
            .setSlotName("Input");
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 11)
               .addItemStack(new ItemStack((ItemLike)Exposure.Items.STACKED_PHOTOGRAPHS.get())))
            .setSlotName("Result");
      }

      if (recipe.getType() == 1) {
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 11, 11)
               .addItemStack(new ItemStack((ItemLike)Exposure.Items.STACKED_PHOTOGRAPHS.get())))
            .setOverlay(this.removeCursor, 11, 4)
            .setSlotName("Input");
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 11).addItemStack(new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get())))
            .setSlotName("Result");
      }
   }

   @NotNull
   public RecipeType<PhotographStackingJeiRecipe> getRecipeType() {
      return ExposureJeiPlugin.PHOTOGRAPH_STACKING_RECIPE_TYPE;
   }

   @NotNull
   public Component getTitle() {
      return this.title;
   }

   @NotNull
   public IDrawable getBackground() {
      return this.background;
   }

   @NotNull
   public IDrawable getIcon() {
      return this.icon;
   }
}
