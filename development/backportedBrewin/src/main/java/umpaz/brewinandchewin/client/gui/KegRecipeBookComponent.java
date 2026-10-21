package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;

/**
 * Port of KegRecipeBookComponent onto 1.19.2. The ghost recipe also shows the base fluid in the gauge and the
 * temperature the recipe needs. Port: the filter button uses one 256x256 sheet built from upstream's four sprites
 * (textures/gui/keg_recipe_book.png), and the cycling ghost fluid steps every 1.5 s of wall time.
 */
public class KegRecipeBookComponent extends RecipeBookComponent {
   private static final ResourceLocation FILTER_TEXTURE = BrewinAndChewin.asResource("textures/gui/keg_recipe_book.png");
   private static final Component FILTER_NAME = Component.translatable("brewinandchewin.container.recipe_book.fermentable");
   private final RecipeManager recipeManager;

   public KegRecipeBookComponent(RecipeManager recipeManager) {
      this.recipeManager = recipeManager;
   }

   @Override
   protected void initFilterButtonTextures() {
      this.filterButton.initTextureValues(0, 0, 28, 18, FILTER_TEXTURE);
   }

   public void hide() {
      this.setVisible(false);
   }

   @Nullable
   public Recipe<?> getGhostRecipe() {
      return this.ghostRecipe.getRecipe();
   }

   @NotNull
   @Override
   protected Component getRecipeFilterName() {
      return FILTER_NAME;
   }

   private static AbstractedFluidStack cycledFluid(FluidIngredientWithAmount ingredient) {
      List<AbstractedFluidStack> stacks = ingredient.ingredient().displayStacks();
      AbstractedFluidStack fluidStack = stacks.get((int)(Util.getMillis() / 1500L % stacks.size()));
      return new AbstractedFluidStack(fluidStack.fluid(), ingredient.amount(), fluidStack.tag(), ingredient.unit().orElse(FluidUnit.getLoaderUnit()));
   }

   @Override
   public void renderTooltip(PoseStack pose, int renderX, int renderY, int mouseX, int mouseY) {
      super.renderTooltip(pose, renderX, renderY, mouseX, mouseY);
      if (this.isVisible() && this.menu instanceof KegMenu kegMenu && this.ghostRecipe.getRecipe() instanceof KegFermentingRecipe fermentingRecipe) {
         if (!KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature())) {
            this.renderTemperatureTooltip(pose, fermentingRecipe, renderX, renderY, mouseX, mouseY);
         }

         Optional<FluidIngredientWithAmount> ingredient = fermentingRecipe.getFluidIngredient();
         if (ingredient.isEmpty() || ingredient.get().ingredient().displayStacks().isEmpty()) {
            return;
         }

         AbstractedFluidStack fluidStack = cycledFluid(ingredient.get());
         if (!kegMenu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid())) {
            this.renderTankTooltip(pose, kegMenu, renderX, renderY, mouseX, mouseY, fluidStack);
         }
      }
   }

   @Override
   public void renderGhostRecipe(PoseStack pose, int leftPos, int topPos, boolean singleItem, float partialTick) {
      this.ghostRecipe.render(pose, this.minecraft, leftPos, topPos, singleItem, partialTick);
      if (!(this.menu instanceof KegMenu kegMenu) || !(this.ghostRecipe.getRecipe() instanceof KegFermentingRecipe fermentingRecipe)) {
         return;
      }
      if (!KegBlockEntity.isValidTemp(kegMenu.getKegTemperature(), fermentingRecipe.getTemperature())) {
         RenderSystem.enableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
         RenderSystem.setShaderTexture(0, KegScreen.BACKGROUND_TEXTURE);
         int temp = fermentingRecipe.getTemperature();
         int minX = leftPos + 52;
         int maxX = minX + 8;
         if (temp < 3) {
            GuiComponent.blit(pose, leftPos + KegScreen.CHILLY_BAR.x(), topPos + KegScreen.CHILLY_BAR.y(), 0, 184, 0, KegScreen.CHILLY_BAR.width(), KegScreen.CHILLY_BAR.height(), 256, 256);
            minX = leftPos + KegScreen.CHILLY_BAR.x();
         }

         if (temp == 1) {
            GuiComponent.blit(pose, leftPos + KegScreen.COLD_BAR.x(), topPos + KegScreen.COLD_BAR.y(), 0, 176, 0, KegScreen.COLD_BAR.width(), KegScreen.COLD_BAR.height(), 256, 256);
            minX = leftPos + KegScreen.COLD_BAR.x();
         }

         if (temp > 3) {
            GuiComponent.blit(pose, leftPos + KegScreen.WARM_BAR.x(), topPos + KegScreen.WARM_BAR.y(), 0, 201, 0, KegScreen.WARM_BAR.width(), KegScreen.WARM_BAR.height(), 256, 256);
            maxX = leftPos + KegScreen.WARM_BAR.x() + KegScreen.WARM_BAR.width();
         }

         if (temp == 5) {
            GuiComponent.blit(pose, leftPos + KegScreen.HOT_BAR.x(), topPos + KegScreen.HOT_BAR.y(), 0, 210, 0, KegScreen.HOT_BAR.width(), KegScreen.HOT_BAR.height(), 256, 256);
            maxX = leftPos + KegScreen.HOT_BAR.x() + KegScreen.HOT_BAR.width();
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         GuiComponent.fill(pose, minX, topPos + 55, maxX, topPos + 58, 822018048);
      }

      Optional<FluidIngredientWithAmount> fluidIngredient = fermentingRecipe.getFluidIngredient();
      if (fluidIngredient.isPresent() && !fluidIngredient.get().ingredient().displayStacks().isEmpty()) {
         AbstractedFluidStack fluidStack = cycledFluid(fluidIngredient.get());
         if (kegMenu.kegTank.isEmpty() || !kegMenu.kegTank.getAbstractedFluid().fluid().isSame(fluidStack.fluid())) {
            if (BnCConfiguration.CLIENT_CONFIG.get().renderFluidInKeg()) {
               BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, pose, leftPos + 120, topPos + 19, 0.6F);
            }

            GuiComponent.fill(pose, leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822018048);
            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(fluidStack).copy();
            int pourCount = (int)(Math.min(fluidIngredient.get().loaderAmount(), kegMenu.kegTank.getFluidCapacity())
               / FluidUnit.convert(250L, FluidUnit.MILLIBUCKET, FluidUnit.getLoaderUnit()));
            itemDisplay.setCount(pourCount);
            if (!itemDisplay.isEmpty()) {
               int itemX = leftPos + 124;
               int itemY = topPos + 23;
               this.minecraft.getItemRenderer().renderAndDecorateFakeItem(itemDisplay, itemX, itemY);
               RenderSystem.depthFunc(516);
               GuiComponent.fill(pose, itemX, itemY, itemX + 16, itemY + 16, 822083583);
               RenderSystem.depthFunc(515);
               this.minecraft.getItemRenderer().renderGuiItemDecorations(this.minecraft.font, itemDisplay, itemX, itemY);
            }
         }
      } else if (fluidIngredient.isEmpty() && !kegMenu.kegTank.isEmpty()) {
         GuiComponent.fill(pose, leftPos + 120, topPos + 19, leftPos + 120 + 16 + 8, topPos + 31 + 16, 822018048);
      }
   }

   private void renderTankTooltip(PoseStack pose, KegMenu kegMenu, int renderX, int renderY, int mouseX, int mouseY, AbstractedFluidStack stack) {
      if (!isHovering(120, 19, 24, 28, mouseX - renderX, mouseY - renderY)
         || !kegMenu.kegTank.isEmpty() && kegMenu.kegTank.getAbstractedFluid().fluid().isSame(stack.fluid())) {
         return;
      }
      BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
      Component component = BrewinAndChewin.getHelper()
         .getFluidDisplayName(stack)
         .plainCopy()
         .append(
            (config.displayUnit().shortFormat(" (%s/%s") + ")")
               .formatted(
                  FluidUnit.convert(stack.amount(), stack.unit(), config.displayUnit()),
                  FluidUnit.convert(kegMenu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), config.displayUnit())
               )
         );
      List<Component> components = new ArrayList<>(List.of(component));
      if (config.oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ADVANCED_TOOLTIPS && this.minecraft.options.advancedItemTooltips
         || config.oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ALWAYS) {
         FluidUnit opposite = FluidUnit.getOpposite(config.displayUnit());
         components.add(
            Component.literal(
                  opposite.shortFormat("%s/%s")
                     .formatted(
                        FluidUnit.convert(stack.amount(), stack.unit(), opposite),
                        FluidUnit.convert(kegMenu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), opposite)
                     )
               )
               .withStyle(ChatFormatting.GRAY)
         );
      }

      if (this.minecraft.options.advancedItemTooltips) {
         components.add(Component.literal(String.valueOf(ForgeRegistries.FLUIDS.getKey(stack.fluid()))).withStyle(ChatFormatting.DARK_GRAY));
      }

      if (this.minecraft.screen != null) {
         // Port: the drink item the tank pours into goes along, so AppleSkin shows its food values
         this.minecraft.screen.renderComponentTooltip(pose, components, mouseX, mouseY, BnCFluidItemDisplays.getFluidItemDisplay(stack));
      }
   }

   private void renderTemperatureTooltip(PoseStack pose, KegFermentingRecipe fermentingRecipe, int renderX, int renderY, int mouseX, int mouseY) {
      if (!isHovering(34, 54, 43, 5, mouseX - renderX, mouseY - renderY) || this.minecraft.screen == null) {
         return;
      }
      MutableComponent key = switch (fermentingRecipe.getTemperature()) {
         case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
         case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
         case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
         case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
         default -> BnCTextUtils.getTranslation("container.keg.normal");
      };
      this.minecraft.screen.renderComponentTooltip(pose, List.of(Component.translatable("brewinandchewin.container.keg.temperature_requirement", key)), mouseX, mouseY);
   }

   private static boolean isHovering(int x, int y, int xWidth, int yWidth, int mouseX, int mouseY) {
      return mouseX >= x && mouseX <= x + xWidth && mouseY >= y && mouseY <= y + yWidth;
   }

   @Override
   public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
      ItemStack resultStack = recipe.getResultItem().copy();
      this.ghostRecipe.setRecipe(recipe);
      if (slots.get(5).getItem().isEmpty()) {
         this.ghostRecipe.addIngredient(Ingredient.of(resultStack), slots.get(5).x, slots.get(5).y);
      }

      if (recipe instanceof KegFermentingRecipe fermentingRecipe && fermentingRecipe.getResult().left().isPresent()) {
         this.recipeManager
            .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
            .stream()
            .filter(kegPouringRecipe -> kegPouringRecipe.getRawFluid().matches(fermentingRecipe.getResult().left().get()))
            .findFirst()
            .ifPresent(pouring -> this.ghostRecipe.addIngredient(Ingredient.of(pouring.getContainer()), slots.get(4).x, slots.get(4).y));
      }

      this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
   }
}
