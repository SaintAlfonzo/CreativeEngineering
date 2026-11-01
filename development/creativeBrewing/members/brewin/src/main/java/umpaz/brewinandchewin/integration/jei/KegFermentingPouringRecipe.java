package umpaz.brewinandchewin.integration.jei;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;

/** One JEI page: a fermenting recipe plus, for a fluid result, the pouring recipe that bottles it. */
public class KegFermentingPouringRecipe extends KegFermentingRecipe {
   @Nullable
   private final ItemStack catalyst;
   @Nullable
   private ItemStack output;
   private long pouringAmount;
   private final int catalystAmount;

   KegFermentingPouringRecipe(KegFermentingRecipe fermentingRecipe, @Nullable KegPouringRecipe pouringRecipe) {
      super(
         fermentingRecipe.getId(),
         fermentingRecipe.getIngredients(),
         fermentingRecipe.getRecipeBookCategory(),
         fermentingRecipe.getFluidIngredient(),
         fermentingRecipe.getRawUnit(),
         fermentingRecipe.getResult(),
         fermentingRecipe.getExperience(),
         fermentingRecipe.getFermentTime(),
         fermentingRecipe.getTemperature()
      );
      if (fermentingRecipe.getResult().right().isPresent()) {
         this.output = fermentingRecipe.getResult().right().get().copy();
      } else if (pouringRecipe != null) {
         this.output = pouringRecipe.getOutput();
      }

      if (pouringRecipe != null) {
         this.catalyst = pouringRecipe.getContainer();
         this.catalystAmount = pouringRecipe.getResultItem().getCount();
         this.pouringAmount = pouringRecipe.getUnit().convertToLoader(pouringRecipe.getRawFluid().amount());
      } else {
         this.catalyst = null;
         this.catalystAmount = 0;
      }
   }

   @Nullable
   public ItemStack getCatalyst() {
      return this.catalyst;
   }

   public int getCatalystAmount() {
      return this.catalystAmount;
   }

   public long getPouringAmount() {
      return this.pouringAmount;
   }

   @Nullable
   public ItemStack getOutput() {
      return this.output;
   }
}
