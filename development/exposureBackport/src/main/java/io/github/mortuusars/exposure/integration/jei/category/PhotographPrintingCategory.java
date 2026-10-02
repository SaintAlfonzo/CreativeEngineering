package io.github.mortuusars.exposure.integration.jei.category;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.integration.jei.ExposureJeiPlugin;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.Registry;

public class PhotographPrintingCategory implements IRecipeCategory<PhotographPrintingJeiRecipe> {
   private static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/jei/photograph_printing.png");
   private final Component title = Component.translatable("jei.exposure.photograph_printing.title");
   private final IDrawable background;
   private final IDrawable icon;
   private final IDrawableStatic filmDrawable;

   public PhotographPrintingCategory(IGuiHelper guiHelper) {
      this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 170, 80);
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack((ItemLike)Exposure.Items.LIGHTROOM.get()));
      this.filmDrawable = guiHelper.createDrawable(TEXTURE, 0, 80, 170, 80);
   }

   public void setRecipe(IRecipeLayoutBuilder builder, @NotNull PhotographPrintingJeiRecipe recipe, @NotNull IFocusGroup focuses) {
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 78, 17)
            .addItemStack(
               new ItemStack(
                  recipe.getExposureType() == ExposureType.COLOR
                     ? (ItemLike)Exposure.Items.DEVELOPED_COLOR_FILM.get()
                     : (ItemLike)Exposure.Items.DEVELOPED_BLACK_AND_WHITE_FILM.get()
               )
            ))
         .setSlotName("Film");
      List<ItemStack> papers = Registry.ITEM
         .getTag(Exposure.Tags.Items.PHOTO_PAPERS)
         .map(holders -> holders.stream().map(itemHolder -> new ItemStack((ItemLike)itemHolder.value())).collect(Collectors.toList()))
         .orElse(Collections.emptyList());
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.CATALYST, 6, 55).addItemStacks(papers)).setSlotName("Paper");
      if (recipe.getExposureType() == ExposureType.COLOR) {
         List<ItemStack> cyanDyes = Registry.ITEM
            .getTag(Exposure.Tags.Items.CYAN_PRINTING_DYES)
            .map(holders -> holders.stream().map(itemHolder -> new ItemStack((ItemLike)itemHolder.value())).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.CATALYST, 40, 55).addItemStacks(cyanDyes)).setSlotName("Cyan");
         List<ItemStack> magentaDyes = Registry.ITEM
            .getTag(Exposure.Tags.Items.MAGENTA_PRINTING_DYES)
            .map(holders -> holders.stream().map(itemHolder -> new ItemStack((ItemLike)itemHolder.value())).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.CATALYST, 58, 55).addItemStacks(magentaDyes)).setSlotName("Magenta");
         List<ItemStack> yellowDyes = Registry.ITEM
            .getTag(Exposure.Tags.Items.YELLOW_PRINTING_DYES)
            .map(holders -> holders.stream().map(itemHolder -> new ItemStack((ItemLike)itemHolder.value())).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
         ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.CATALYST, 76, 55).addItemStacks(yellowDyes)).setSlotName("Yellow");
      }

      List<ItemStack> blackDyes = Registry.ITEM
         .getTag(Exposure.Tags.Items.BLACK_PRINTING_DYES)
         .map(holders -> holders.stream().map(itemHolder -> new ItemStack((ItemLike)itemHolder.value())).collect(Collectors.toList()))
         .orElse(Collections.emptyList());
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.CATALYST, 94, 55).addItemStacks(blackDyes)).setSlotName("Black");
      ItemStack resultItemStack = new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH.get());
      Exposure.DataComponents.PHOTOGRAPH_TYPE.set(resultItemStack, recipe.getExposureType());
      builder.addSlot(RecipeIngredientRole.OUTPUT, 144, 55).addItemStack(resultItemStack);
   }

   public void draw(
      PhotographPrintingJeiRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull PoseStack poseStack, double mouseX, double mouseY
   ) {
      if (recipe.getExposureType() == ExposureType.COLOR) {
         RenderSystem.setShaderColor(1.1F, 0.86F, 0.66F, 1.0F);
      }

      this.filmDrawable.draw(poseStack);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @NotNull
   public RecipeType<PhotographPrintingJeiRecipe> getRecipeType() {
      return ExposureJeiPlugin.PHOTOGRAPH_PRINTING_RECIPE_TYPE;
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
