package umpaz.brewinandchewin.integration.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.block.entity.container.KegTransfer;
import umpaz.brewinandchewin.common.network.serverbound.JEITransferKegRecipeServerboundPacket;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The keg's JEI [+] button (alcohol.md 10.3): runs {@link KegTransfer} on the client to show what's wrong (temperature,
 * missing ingredients or fluid containers, a tank it can't empty, a full inventory), then asks the server to do it.
 */
public class FermentingTransfer implements IRecipeTransferHandler<KegMenu, KegFermentingPouringRecipe> {
   private final IRecipeTransferHandlerHelper helper;

   public FermentingTransfer(IRecipeTransferHandlerHelper helper) {
      this.helper = helper;
   }

   @Override
   public Class<? extends KegMenu> getContainerClass() {
      return KegMenu.class;
   }

   @Override
   public Optional<MenuType<KegMenu>> getMenuType() {
      return Optional.of(BnCMenuTypes.KEG);
   }

   @Override
   public RecipeType<KegFermentingPouringRecipe> getRecipeType() {
      return BnCJEIRecipeTypes.FERMENTING;
   }

   @Nullable
   @Override
   public IRecipeTransferError transferRecipe(KegMenu menu, KegFermentingPouringRecipe recipe, IRecipeSlotsView view, Player player, boolean maxTransfer,
      boolean doTransfer) {
      KegTransfer.Plan plan = KegTransfer.plan(menu, recipe, maxTransfer);
      switch (plan.error) {
         case TEMPERATURE:
            return this.error("brewinandchewin.jei.tooltip.error.recipe.transfer.temperature");
         case INVENTORY_FULL:
            return this.error("jei.tooltip.error.recipe.transfer.inventory.full");
         case CANT_EMPTY:
            return this.error("brewinandchewin.jei.tooltip.error.recipe.transfer.cant_empty");
         case NOT_ENOUGH_FLUID:
            return this.error("brewinandchewin.jei.tooltip.error.recipe.transfer.not_enough_fluid");
         case MISSING:
            return this.helper.createUserErrorForMissingSlots(Component.translatable("jei.tooltip.error.recipe.transfer.missing"), missingViews(view, plan, recipe));
         default:
            break;
      }
      if (doTransfer) {
         BrewinAndChewin.getHelper().sendServerbound(new JEITransferKegRecipeServerboundPacket(recipe.getId(), maxTransfer));
      }
      return null;
   }

   private IRecipeTransferError error(String key) {
      return this.helper.createUserErrorWithTooltip(Component.translatable(key));
   }

   /** The page's item input slots follow the ingredient list (FermentingRecipeCategory); the fluid slot is the input holding a fluid. */
   private static List<IRecipeSlotView> missingViews(IRecipeSlotsView view, KegTransfer.Plan plan, KegFermentingPouringRecipe recipe) {
      List<IRecipeSlotView> inputs = view.getSlotViews(RecipeIngredientRole.INPUT);
      List<IRecipeSlotView> itemViews = new ArrayList<>();
      IRecipeSlotView fluidView = null;
      for (IRecipeSlotView slot : inputs) {
         if (slot.getIngredients(VanillaTypes.ITEM_STACK).findAny().isPresent()) {
            itemViews.add(slot);
         } else if (fluidView == null && slot.getAllIngredients().findAny().isPresent()) {
            fluidView = slot;
         }
      }
      List<IRecipeSlotView> missing = new ArrayList<>();
      for (int index : plan.missingIngredients) {
         int position = countNonEmptyBefore(recipe, index);
         if (position < itemViews.size()) {
            missing.add(itemViews.get(position));
         }
      }
      if (plan.missingFluid && fluidView != null) {
         missing.add(fluidView);
      }
      return missing;
   }

   /** Every ingredient gets a slot, but an empty one holds no items, so it isn't among the item views. */
   private static int countNonEmptyBefore(KegFermentingPouringRecipe recipe, int index) {
      int count = 0;
      for (int i = 0; i < index && i < recipe.getIngredients().size(); i++) {
         if (!recipe.getIngredients().get(i).isEmpty()) {
            count++;
         }
      }
      return count;
   }
}
