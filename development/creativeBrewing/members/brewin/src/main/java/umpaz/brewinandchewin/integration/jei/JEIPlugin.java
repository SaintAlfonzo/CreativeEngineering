package umpaz.brewinandchewin.integration.jei;

import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.integration.jei.category.CheeseRecipeCategory;
import umpaz.brewinandchewin.integration.jei.category.FermentingRecipeCategory;

/**
 * Port of the JEI plugin onto JEI 11.8 (1.19.2). The [+] button moves the ingredients and fills or empties the tank from
 * fluid containers in the inventory ({@link FermentingTransfer}, alcohol.md 10.3).
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {
   private static final ResourceLocation ID = BrewinAndChewin.asResource("jei_plugin");

   @Override
   public void registerCategories(IRecipeCategoryRegistration registry) {
      registry.addRecipeCategories(new FermentingRecipeCategory(registry.getJeiHelpers().getGuiHelper(), registry.getJeiHelpers().getPlatformFluidHelper()));
      registry.addRecipeCategories(new CheeseRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
   }

   @Override
   public void registerRecipes(IRecipeRegistration registration) {
      BnCJEIRecipes modRecipes = new BnCJEIRecipes();
      registration.addRecipes(BnCJEIRecipeTypes.FERMENTING, modRecipes.getKegRecipes());
      registration.addRecipes(BnCJEIRecipeTypes.AGING, modRecipes.getCheeseRecipes());
   }

   @Override
   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addRecipeCatalyst(new ItemStack(BnCItems.KEG), BnCJEIRecipeTypes.FERMENTING);
   }

   @Override
   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(KegScreen.class, 80, 25, 23, 17, BnCJEIRecipeTypes.FERMENTING);
      IJeiHelpers helpers = registration.getJeiHelpers();
      Rect2i bounds = new Rect2i(107, 18, 26, 30);
      registration.addGuiContainerHandler(KegScreen.class, new IGuiContainerHandler<KegScreen>() {
         @Override
         public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(KegScreen screen, double mouseX, double mouseY) {
            if (!bounds.contains((int)mouseX - screen.getGuiLeft(), (int)mouseY - screen.getGuiTop())) {
               return Optional.empty();
            }
            AbstractedFluidStack tank = screen.getMenu().kegTank.getAbstractedFluid();
            if (tank.isEmpty()) {
               return Optional.empty();
            }
            Rect2i area = new Rect2i(screen.getGuiLeft() + bounds.getX(), screen.getGuiTop() + bounds.getY(), bounds.getWidth(), bounds.getHeight());
            return clickable(helpers, helpers.getPlatformFluidHelper(), tank, area);
         }
      });
   }

   private static <T> Optional<IClickableIngredient<?>> clickable(IJeiHelpers helpers, IPlatformFluidHelper<T> fluidHelper, AbstractedFluidStack tank, Rect2i area) {
      T fluid = fluidHelper.create(tank.fluid(), tank.amount(), tank.tag());
      Optional<ITypedIngredient<T>> typed = helpers.getIngredientManager().createTypedIngredient(fluidHelper.getFluidIngredientType(), fluid);
      return typed.map(ingredient -> new IClickableIngredient<T>() {
         @Override
         public ITypedIngredient<T> getTypedIngredient() {
            return ingredient;
         }

         @Override
         public Rect2i getArea() {
            return area;
         }
      });
   }

   @Override
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(new FermentingTransfer(registration.getTransferHelper()), BnCJEIRecipeTypes.FERMENTING);
   }

   @Override
   public ResourceLocation getPluginUid() {
      return ID;
   }
}
