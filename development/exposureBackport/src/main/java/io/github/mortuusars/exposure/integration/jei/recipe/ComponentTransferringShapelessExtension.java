package io.github.mortuusars.exposure.integration.jei.recipe;

import io.github.mortuusars.exposure.world.item.crafting.recipe.ComponentTransferringRecipe;
import java.util.List;
import java.util.stream.Collectors;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** JEI 11 (1.19.2): one extension instance per recipe (JEI 19 passes a RecipeHolder to a shared one). */
public class ComponentTransferringShapelessExtension implements ICraftingCategoryExtension {
   private final ComponentTransferringRecipe recipe;

   public ComponentTransferringShapelessExtension(ComponentTransferringRecipe recipe) {
      this.recipe = recipe;
   }

   public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ICraftingGridHelper craftingGridHelper, @NotNull IFocusGroup focuses) {
      List<List<ItemStack>> inputs = this.recipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).collect(Collectors.toList());
      inputs.add(0, List.of(this.recipe.getSourceIngredient().getItems()));
      ItemStack resultItem = this.recipe.getResult();
      int width = this.getWidth();
      int height = this.getHeight();
      craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
      craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
   }

   @Override
   public @Nullable ResourceLocation getRegistryName() {
      return this.recipe.getId();
   }
}
