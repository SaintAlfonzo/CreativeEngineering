package io.github.mortuusars.exposure.integration.jei;

import com.google.common.collect.ImmutableList;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.PlatformHelper;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.integration.jei.category.PhotographPrintingCategory;
import io.github.mortuusars.exposure.integration.jei.category.PhotographStackingCategory;
import io.github.mortuusars.exposure.integration.jei.forge.ExposureJeiPluginImpl;
import io.github.mortuusars.exposure.integration.jei.recipe.ComponentTransferringShapelessExtension;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographPrintingJeiRecipe;
import io.github.mortuusars.exposure.integration.jei.recipe.PhotographStackingJeiRecipe;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ExposureJeiPlugin implements IModPlugin {
   public static final RecipeType<PhotographPrintingJeiRecipe> PHOTOGRAPH_PRINTING_RECIPE_TYPE = RecipeType.create(
      "exposure", "photograph_printing", PhotographPrintingJeiRecipe.class
   );
   public static final RecipeType<PhotographStackingJeiRecipe> PHOTOGRAPH_STACKING_RECIPE_TYPE = RecipeType.create(
      "exposure", "photograph_stacking", PhotographStackingJeiRecipe.class
   );
   private static final ResourceLocation ID = Exposure.resource("jei_plugin");

   @NotNull
   public ResourceLocation getPluginUid() {
      return ID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      registration.addRecipeCategories(new IRecipeCategory[]{new PhotographPrintingCategory(registration.getJeiHelpers().getGuiHelper())});
      registration.addRecipeCategories(new IRecipeCategory[]{new PhotographStackingCategory(registration.getJeiHelpers().getGuiHelper())});
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(new ItemStack((ItemLike)Exposure.Items.LIGHTROOM.get()), new RecipeType[]{PHOTOGRAPH_PRINTING_RECIPE_TYPE});
      registration.addRecipeCatalyst(new ItemStack((ItemLike)Exposure.Items.STACKED_PHOTOGRAPHS.get()), new RecipeType[]{PHOTOGRAPH_STACKING_RECIPE_TYPE});
   }

   public void registerRecipes(@NotNull IRecipeRegistration registration) {
      registration.addRecipes(
         PHOTOGRAPH_PRINTING_RECIPE_TYPE,
         ImmutableList.of(new PhotographPrintingJeiRecipe(ExposureType.BLACK_AND_WHITE), new PhotographPrintingJeiRecipe(ExposureType.COLOR))
      );
      registration.addRecipes(PHOTOGRAPH_STACKING_RECIPE_TYPE, ImmutableList.of(new PhotographStackingJeiRecipe(0), new PhotographStackingJeiRecipe(1)));
      if ((Boolean)Config.Client.SHOW_JEI_INFORMATION.get()) {
         registration.addItemStackInfo(
            List.of(new ItemStack((ItemLike)Exposure.Items.PHOTOGRAPH_FRAME.get()), new ItemStack((ItemLike)Exposure.Items.CLEAR_PHOTOGRAPH_FRAME.get())),
            new Component[]{Component.translatable("exposure.jei.info.photograph_frame")}
         );
      }

      if (PlatformHelper.isModLoaded("create")) {
         addSequencedDevelopingRecipes(registration);
      }
   }

   public static void addSequencedDevelopingRecipes(@NotNull IRecipeRegistration registration) {
      ExposureJeiPluginImpl.addSequencedDevelopingRecipes(registration);
   }

   public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
      registration.getCraftingCategory().addCategoryExtension(ComponentTransferringRecipe.class, ComponentTransferringShapelessExtension::new);
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addGenericGuiContainerHandler(AlbumScreen.class, new IGuiContainerHandler<AlbumScreen>() {
         @NotNull
         public List<Rect2i> getGuiExtraAreas(@NotNull AlbumScreen containerScreen) {
            return List.of(new Rect2i(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight()));
         }
      });
      registration.addGenericGuiContainerHandler(ItemRenameScreen.class, new IGuiContainerHandler<ItemRenameScreen>() {
         @NotNull
         public List<Rect2i> getGuiExtraAreas(@NotNull ItemRenameScreen containerScreen) {
            return List.of(new Rect2i(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight()));
         }
      });
   }
}
