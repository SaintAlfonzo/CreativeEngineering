package io.github.mortuusars.exposure.client.gui;

import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumViewScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.crafting.recipe.FilmDevelopingRecipe;
import io.github.mortuusars.exposure.world.item.crafting.recipe.PhotographCopyingRecipe;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientGUI {
   public static void openPhotographScreen(List<ItemAndStack<PhotographItem>> photographs) {
      Minecrft.get().setScreen(new PhotographScreen(photographs));
   }

   public static void openPhotographsScreenFromItem(int item) {
      Minecrft.get().setScreen(new PhotographScreen(PhotographScreen.PhotographProvider.fromPhotographItem(item)));
   }

   public static void openAlbumViewScreen(ItemStack albumStack) {
      Minecrft.get().setScreen(new AlbumViewScreen(AlbumViewScreen.AlbumAccess.fromItem(albumStack)));
   }

   public static void addFilmRollDevelopingTooltip(
      ItemStack filmStack, @Nullable Level tooltipContext, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced
   ) {
      addRecipeTooltip(filmStack, tooltipContext, tooltipComponents, isAdvanced, r -> {
         if (r instanceof FilmDevelopingRecipe filmDevelopingRecipe && filmDevelopingRecipe.getSourceIngredient().test(filmStack)) {
            return true;
         }

         return false;
      }, "item.exposure.film_roll.tooltip.details.develop");
   }

   public static void addPhotographCopyingTooltip(
      ItemStack photographStack, @Nullable Level tooltipContext, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced
   ) {
      addRecipeTooltip(photographStack, tooltipContext, tooltipComponents, isAdvanced, r -> {
         if (r instanceof PhotographCopyingRecipe photographCopyingRecipe && photographCopyingRecipe.getSourceIngredient().test(photographStack)) {
            return true;
         }

         return false;
      }, "item.exposure.photograph.tooltip.details.copy");
   }

   private static void addRecipeTooltip(
      ItemStack stack,
      @Nullable Level tooltipContext,
      @NotNull List<Component> tooltipComponents,
      @NotNull TooltipFlag isAdvanced,
      Predicate<CraftingRecipe> recipeFilter,
      String detailsKey
   ) {
      if (Minecraft.getInstance().level != null) {
         tooltipComponents.add(Component.translatable("tooltip.exposure.hold_for_details"));
         if (Screen.hasShiftDown()) {
            Optional<NonNullList<Ingredient>> recipeIngredients = Minecraft.getInstance()
               .level
               .getRecipeManager()
               .getAllRecipesFor(RecipeType.CRAFTING)
               .stream()
               .filter(recipeFilter)
               .findFirst()
               .map(Recipe::getIngredients);
            if (!recipeIngredients.isEmpty() && !recipeIngredients.get().isEmpty()) {
               NonNullList<Ingredient> ingredients = recipeIngredients.get();
               tooltipComponents.add(Component.empty());
               Style orange = Style.EMPTY.withColor(13079883);
               Style yellow = Style.EMPTY.withColor(15653496);
               tooltipComponents.add(Component.translatable(detailsKey).withStyle(orange));

               for (int i = 0; i < ingredients.size(); i++) {
                  ItemStack[] stacks = ((Ingredient)ingredients.get(i)).getItems();
                  if (stacks.length == 0) {
                     tooltipComponents.add(Component.literal("  ").append(Component.literal("?").withStyle(yellow)));
                  } else if (stacks.length == 1) {
                     tooltipComponents.add(Component.literal("  ").append(stacks[0].getHoverName().copy().withStyle(yellow)));
                  } else {
                     int val = (int)Math.ceil(
                        (double)((float)(Minecraft.getInstance().level.getGameTime() + (long)(10 * i)) % (20.0F * (float)stacks.length) / 20.0F)
                     );
                     int index = Mth.clamp(val - 1, 0, stacks.length - 1);
                     tooltipComponents.add(Component.literal("  ").append(stacks[index].getHoverName().copy().withStyle(yellow)));
                  }
               }
            }
         }
      }
   }
}
