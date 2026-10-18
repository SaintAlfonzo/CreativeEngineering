package umpaz.brewinandchewin.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCItemUtils;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.integration.jei.BnCJEIRecipeTypes;
import umpaz.brewinandchewin.integration.jei.KegFermentingPouringRecipe;
import vectorwing.farmersdelight.common.utility.ClientRenderUtils;

/** The Fermenting page: base fluid, ingredients, temperature, result fluid, and the container that pours it. */
public class FermentingRecipeCategory implements IRecipeCategory<KegFermentingPouringRecipe> {
   public static final ResourceLocation UID = BrewinAndChewin.asResource("fermenting");
   protected final IPlatformFluidHelper<?> fluidHelper;
   protected final IDrawableAnimated arrow;
   private final Component title;
   private final IDrawable background;
   private final IDrawable icon;
   private final IDrawable cold;
   private final IDrawable chilly;
   private final IDrawable warm;
   private final IDrawable hot;
   protected final IDrawable timeIcon;
   protected final IDrawable expIcon;
   protected final IDrawable kegOverlay;
   protected final IDrawableAnimated leftBubble;
   protected final IDrawableAnimated rightBubble;

   public FermentingRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<?> fluidHelper) {
      this.fluidHelper = fluidHelper;
      this.title = BnCTextUtils.getTranslation("jei.fermenting");
      ResourceLocation backgroundImage = BrewinAndChewin.asResource("textures/gui/jei/keg.png");
      this.background = guiHelper.createDrawable(backgroundImage, 12, 13, 136, 56);
      this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BnCItems.KEG));
      this.arrow = guiHelper.drawableBuilder(backgroundImage, 171, 4, 23, 16).buildAnimated(200, StartDirection.LEFT, false);
      this.cold = guiHelper.createDrawable(backgroundImage, 170, 0, 8, 3);
      this.chilly = guiHelper.createDrawable(backgroundImage, 178, 0, 9, 3);
      this.warm = guiHelper.createDrawable(backgroundImage, 195, 0, 9, 3);
      this.hot = guiHelper.createDrawable(backgroundImage, 204, 0, 8, 3);
      this.expIcon = guiHelper.createDrawable(backgroundImage, 170, 32, 9, 9);
      this.timeIcon = guiHelper.createDrawable(backgroundImage, 170, 21, 8, 11);
      this.kegOverlay = guiHelper.createDrawable(backgroundImage, 170, 45, 26, 30);
      this.leftBubble = guiHelper.drawableBuilder(backgroundImage, 170, 75, 9, 24).buildAnimated(50, StartDirection.BOTTOM, false);
      this.rightBubble = guiHelper.drawableBuilder(backgroundImage, 180, 75, 9, 24).buildAnimated(50, StartDirection.BOTTOM, false);
   }

   @Override
   public RecipeType<KegFermentingPouringRecipe> getRecipeType() {
      return BnCJEIRecipeTypes.FERMENTING;
   }

   @Override
   public Component getTitle() {
      return this.title;
   }

   @Override
   public IDrawable getBackground() {
      return this.background;
   }

   @Override
   public IDrawable getIcon() {
      return this.icon;
   }

   private <T> void addFluids(IRecipeSlotBuilder slot, IPlatformFluidHelper<T> helper, List<AbstractedFluidStack> stacks, long amount) {
      List<T> fluids = new ArrayList<>();
      for (AbstractedFluidStack stack : stacks) {
         fluids.add(helper.create(stack.fluid(), amount, stack.tag()));
      }
      slot.addIngredients(helper.getFluidIngredientType(), fluids);
   }

   private <T> void addInvisibleFluids(IRecipeLayoutBuilder builder, RecipeIngredientRole role, IPlatformFluidHelper<T> helper, List<AbstractedFluidStack> stacks,
      long amount) {
      List<T> fluids = new ArrayList<>();
      for (AbstractedFluidStack stack : stacks) {
         fluids.add(helper.create(stack.fluid(), amount, stack.tag()));
      }
      builder.addInvisibleIngredients(role).addIngredients(helper.getFluidIngredientType(), fluids);
   }

   /** The item a fluid pours into, counted for the given amount (upstream's itemDisplay/pourCount pair). */
   private static ItemStack pouredDisplay(AbstractedFluidStack stack, long amount) {
      ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(stack).copy();
      Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance()
         .level
         .getRecipeManager()
         .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
         .stream()
         .sorted(Comparator.comparing(KegPouringRecipe::isStrict))
         .filter(r -> r.isStrict() ? ItemStack.isSameItemSameTags(itemDisplay, r.getResultItem()) : BnCItemUtils.isSameItem(itemDisplay, r.getResultItem()))
         .findFirst();
      long capacity = BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity();
      itemDisplay.setCount(pouringRecipe.map(r -> (int)(Math.min(capacity, amount) / r.getLoaderAmount())).orElse(1));
      return itemDisplay;
   }

   @Override
   public void setRecipe(IRecipeLayoutBuilder builder, KegFermentingPouringRecipe recipe, IFocusGroup focusGroup) {
      boolean renderFluid = BnCConfiguration.CLIENT_CONFIG.get().renderFluidInKeg();
      long capacity = BnCConfiguration.COMMON_CONFIG.get().keg().localizedCapacity();
      if (recipe.getFluidIngredient().isPresent()) {
         FluidIngredientWithAmount ingredient = recipe.getFluidIngredient().get();
         List<AbstractedFluidStack> stacks = ingredient.ingredient().displayStacks();
         if (renderFluid) {
            IRecipeSlotBuilder recipeSlot = builder.addSlot(RecipeIngredientRole.INPUT, 0, 2)
               .setFluidRenderer(capacity, false, 26, 30)
               .setOverlay(this.kegOverlay, 0, 0);
            this.addFluids(recipeSlot, this.fluidHelper, stacks, ingredient.loaderAmount());
         } else {
            this.addInvisibleFluids(builder, RecipeIngredientRole.INPUT, this.fluidHelper, stacks, ingredient.loaderAmount());
         }

         IRecipeSlotBuilder itemSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 5, 5);
         for (AbstractedFluidStack stack : stacks) {
            ItemStack itemDisplay = pouredDisplay(stack, ingredient.loaderAmount());
            if (!itemDisplay.isEmpty()) {
               itemSlot.addItemStack(itemDisplay);
            }
         }
      }

      NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
      int borderSlotSize = 18;

      for (int row = 0; row < 2; row++) {
         for (int column = 0; column < 2; column++) {
            int inputIndex = row * 2 + column;
            if (inputIndex < recipeIngredients.size()) {
               builder.addSlot(RecipeIngredientRole.INPUT, column * borderSlotSize + 29, row * borderSlotSize + 1)
                  .addIngredients(recipeIngredients.get(inputIndex));
            }
         }
      }

      if (recipe.getResult().left().isPresent()) {
         AbstractedFluidStack result = recipe.getResult().left().get();
         long resultAmount = recipe.getUnit().convertToLoader(result.amount());
         if (renderFluid) {
            IRecipeSlotBuilder resultSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 2)
               .setFluidRenderer(capacity, false, 26, 30)
               .setOverlay(this.kegOverlay, 0, 0);
            this.addFluids(resultSlot, this.fluidHelper, List.of(result), resultAmount);
         } else {
            this.addInvisibleFluids(builder, RecipeIngredientRole.OUTPUT, this.fluidHelper, List.of(result), resultAmount);
         }

         // Port fix: upstream counts the servings from the base fluid's amount, which crashes for a fluid result with no
         // base fluid; the result's own amount is what gets poured.
         ItemStack itemDisplay = pouredDisplay(result, resultAmount);
         if (!itemDisplay.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 105, 5).addItemStack(itemDisplay);
         }
      }

      if (recipe.getCatalyst() != null) {
         builder.addSlot(RecipeIngredientRole.CATALYST, 73, 39).addItemStack(recipe.getCatalyst());
      }

      if (recipe.getOutput() != null) {
         builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 39).addItemStack(recipe.getOutput());
      }

      builder.moveRecipeTransferButton(132, 43);
   }

   @Override
   public void draw(KegFermentingPouringRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack pose, double mouseX, double mouseY) {
      this.arrow.draw(pose, 67, 10);
      this.leftBubble.draw(pose, 90, 3);
      this.rightBubble.draw(pose, 127, 3);
      if (recipe.getTemperature() <= 2) {
         this.chilly.draw(pose, 33, 39);
      }

      if (recipe.getTemperature() <= 1) {
         this.cold.draw(pose, 25, 39);
      }

      if (recipe.getTemperature() >= 4) {
         this.warm.draw(pose, 50, 39);
      }

      if (recipe.getTemperature() >= 5) {
         this.hot.draw(pose, 59, 39);
      }

      this.timeIcon.draw(pose, 70, 2);
      if (recipe.getExperience() > 0.0F) {
         this.expIcon.draw(pose, 69, 21);
      }
   }

   @Override
   public List<Component> getTooltipStrings(KegFermentingPouringRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
      List<Component> tooltip = new ArrayList<>();
      if (ClientRenderUtils.isCursorInsideBounds(67, 2, 22, 28, mouseX, mouseY)) {
         int cookTime = recipe.getFermentTime();
         if (cookTime > 0) {
            if (cookTime >= 1200) {
               tooltip.add(Component.translatable("gui.jei.category.smelting.time.minutes", cookTime / 1200));
            } else {
               tooltip.add(Component.translatable("gui.jei.category.smelting.time.seconds", cookTime / 20));
            }
         }

         float experience = recipe.getExperience();
         if (experience > 0.0F) {
            tooltip.add(Component.translatable("gui.jei.category.smelting.experience", experience));
         }
      } else if (ClientRenderUtils.isCursorInsideBounds(24, 38, 44, 5, mouseX, mouseY)) {
         MutableComponent key = switch (recipe.getTemperature()) {
            case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
            case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
            case 3 -> BnCTextUtils.getTranslation("container.keg.normal");
            case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
            case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
            default -> null;
         };
         if (key != null) {
            tooltip.add(key);
         }
      } else if (ClientRenderUtils.isCursorInsideBounds(92, 39, 10, 16, mouseX, mouseY) && recipe.getCatalyst() != null) {
         tooltip.add(Component.literal(String.valueOf(recipe.getPouringAmount())).append(I18n.get("generic.unit.millibuckets")));
      }
      return tooltip;
   }
}
