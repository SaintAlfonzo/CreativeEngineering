package umpaz.brewinandchewin.common.utility;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeUtils;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;

public class BnCRecipeUtils {
   public static ItemStack getPouredItemFromFluid(AbstractedFluidStack fluid) {
      if (fluid.isEmpty()) {
         return ItemStack.EMPTY;
      } else if (BrewinAndChewin.isClient) {
         return BnCClientRecipeUtils.getPouredItemFromFluid(fluid);
      } else {
         MinecraftServer server = BrewinAndChewin.getHelper().getServer();
         if (server == null) {
            return ItemStack.EMPTY;
         } else {
            Optional<KegPouringRecipe> recipe = server.getRecipeManager()
               .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
               .stream()
               .sorted(Comparator.comparing(KegPouringRecipe::isStrict))
               .filter(kegPouringRecipe -> kegPouringRecipe.getRawFluid().matches(fluid))
               .findFirst();
            return recipe.map(KegPouringRecipe::getOutput).orElse(ItemStack.EMPTY);
         }
      }
   }
}
