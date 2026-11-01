package umpaz.brewinandchewin.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.client.utility.BnCRectangle;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.KegItem;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCItemUtils;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.FluidUnit;

/** Port of KegScreen onto 1.19.2's PoseStack screens (GuiGraphics calls become blit / itemRenderer / renderComponentTooltip). */
public class KegScreen extends AbstractContainerScreen<KegMenu> implements RecipeUpdateListener {
   public static final ResourceLocation BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/keg.png");
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   private static final BnCRectangle PROGRESS_ARROW = new BnCRectangle(80, 25, 0, 18);
   public static final BnCRectangle COLD_BAR = new BnCRectangle(35, 55, 8, 4);
   public static final BnCRectangle CHILLY_BAR = new BnCRectangle(43, 55, 9, 4);
   public static final BnCRectangle WARM_BAR = new BnCRectangle(60, 55, 9, 4);
   public static final BnCRectangle HOT_BAR = new BnCRectangle(69, 55, 8, 4);
   private static final BnCRectangle LEFT_BUBBLE = new BnCRectangle(109, 44, 9, 24);
   private static final BnCRectangle RIGHT_BUBBLE = new BnCRectangle(147, 44, 9, 24);
   private final KegRecipeBookComponent recipeBookComponent = new KegRecipeBookComponent(Minecraft.getInstance().level.getRecipeManager());
   private boolean widthTooNarrow;
   private static final Map<Fluid, Component> FLUID_CONTAINER_COMPONENTS = new HashMap<>();

   public KegScreen(KegMenu screenContainer, Inventory inv, Component titleIn) {
      super(screenContainer, inv, titleIn);
      this.leftPos = 0;
      this.topPos = 0;
      this.imageWidth = 176;
      this.imageHeight = 166;
      this.titleLabelX = 28;
   }

   @Override
   public void init() {
      super.init();
      this.widthTooNarrow = this.width < 379;
      this.titleLabelX = 38;
      this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
      this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
      if (BnCConfiguration.COMMON_CONFIG.get().recipeBook().enabled()) {
         this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, button -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)button).setPosition(this.leftPos + 5, this.height / 2 - 49);
         }));
      } else {
         this.recipeBookComponent.hide();
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
      }

      this.addWidget(this.recipeBookComponent);
      this.setInitialFocus(this.recipeBookComponent);
   }

   @Override
   protected void containerTick() {
      super.containerTick();
      this.recipeBookComponent.tick();
   }

   @Override
   public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(pose);
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(pose, partialTicks, mouseX, mouseY);
         this.recipeBookComponent.render(pose, mouseX, mouseY, partialTicks);
      } else {
         this.recipeBookComponent.render(pose, mouseX, mouseY, partialTicks);
         super.render(pose, mouseX, mouseY, partialTicks);
         this.recipeBookComponent.renderGhostRecipe(pose, this.leftPos, this.topPos, false, partialTicks);
      }

      RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
      this.blit(pose, this.leftPos + 119, this.topPos + 15, 176, 22, 27, 33);
      this.renderTankTooltip(pose, mouseX, mouseY);
      this.renderTemperatureTooltip(pose, mouseX, mouseY);
      this.renderTooltip(pose, mouseX, mouseY);
      this.recipeBookComponent.renderTooltip(pose, this.leftPos, this.topPos, mouseX, mouseY);
   }

   public static void clearFluidContainerComponents() {
      FLUID_CONTAINER_COMPONENTS.clear();
   }

   private Component servedIn(AbstractedFluidStack tank) {
      return FLUID_CONTAINER_COMPONENTS.computeIfAbsent(tank.fluid(), fluid -> {
         MutableComponent component = Component.empty().withStyle(ChatFormatting.GRAY);
         int amountAdded = 0;

         for (KegPouringRecipe recipe : Minecraft.getInstance()
            .level
            .getRecipeManager()
            .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
            .stream()
            .filter(pouringRecipe -> pouringRecipe.isStrict() ? pouringRecipe.getRawFluid().matches(tank) : pouringRecipe.getRawFluid().fluid().isSame(tank.fluid()))
            .sorted(Comparator.comparing(recipex -> recipex.getContainer().getItem().getDescription().getString()))
            .toList()) {
            if (amountAdded > 0) {
               component.append(", ");
            }

            component.append(recipe.getContainer().getItem().getDescription().plainCopy().withStyle(ChatFormatting.GRAY));
            amountAdded++;
         }

         return component;
      });
   }

   private boolean ghostAllowsTank() {
      Recipe<?> ghost = this.recipeBookComponent.getGhostRecipe();
      return ghost == null
         || !(ghost instanceof KegFermentingRecipe fermentingRecipe)
         || fermentingRecipe.getResult().left().isPresent() && fermentingRecipe.getResult().left().get().matches(this.menu.kegTank.getAbstractedFluid());
   }

   private void renderTankTooltip(PoseStack pose, int mouseX, int mouseY) {
      if (this.isHovering(120, 19, 24, 28, mouseX, mouseY) && !this.menu.kegTank.isEmpty() && this.ghostAllowsTank()) {
         BnCConfiguration.Client config = BnCConfiguration.CLIENT_CONFIG.get();
         AbstractedFluidStack tank = this.menu.kegTank.getAbstractedFluid();
         Component containerComponent = BnCTextUtils.getTranslation("container.keg.served_in", this.servedIn(tank)).withStyle(ChatFormatting.GRAY);
         Component component = BrewinAndChewin.getHelper()
            .getFluidDisplayName(tank)
            .plainCopy()
            .append(
               (config.displayUnit().shortFormat(" (%s/%s") + ")")
                  .formatted(
                     FluidUnit.convert(tank.amount(), FluidUnit.getLoaderUnit(), config.displayUnit()),
                     FluidUnit.convert(this.menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), config.displayUnit())
                  )
            );
         List<Component> components = new ArrayList<>(List.of(component, containerComponent));
         // Port (alcohol.md 8.2, 8.7): the drink's effects, and the pipe rule while it's on.
         BoozeItem.addFluidEffectLines(tank.fluid(), components);
         KegItem.addPipeRuleLine(components);
         if (config.oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ADVANCED_TOOLTIPS && this.minecraft.options.advancedItemTooltips
            || config.oppositeFluidDisplay() == BnCConfiguration.Client.DisplaySettings.ALWAYS) {
            FluidUnit opposite = FluidUnit.getOpposite(config.displayUnit());
            components.add(
               Component.literal(
                     opposite.shortFormat("%s/%s")
                        .formatted(
                           FluidUnit.convert(tank.amount(), FluidUnit.getLoaderUnit(), opposite),
                           FluidUnit.convert(this.menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), opposite)
                        )
                  )
                  .withStyle(ChatFormatting.GRAY)
            );
         }

         if (this.minecraft.options.advancedItemTooltips) {
            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(tank.fluid());
            components.add(Component.literal(String.valueOf(fluidId)).withStyle(ChatFormatting.DARK_GRAY));
            if (tank.hasTag()) {
               components.add(Component.translatable("item.nbt_tags", tank.tag().getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
         }

         // Port: the drink item the tank pours into goes along, so AppleSkin shows its food values
         this.renderComponentTooltip(pose, components, mouseX, mouseY, BnCFluidItemDisplays.getFluidItemDisplay(tank));
      }
   }

   private void renderTemperatureTooltip(PoseStack pose, int mouseX, int mouseY) {
      Recipe<?> ghost = this.recipeBookComponent.getGhostRecipe();
      if (this.isHovering(35, 54, 42, 5, mouseX, mouseY)
         && (ghost == null || !(ghost instanceof KegFermentingRecipe fermentingRecipe)
            || KegBlockEntity.isValidTemp(this.menu.getKegTemperature(), fermentingRecipe.getTemperature()))) {
         MutableComponent key = switch (this.menu.getKegTemperature()) {
            case 1 -> BnCTextUtils.getTranslation("container.keg.cold");
            case 2 -> BnCTextUtils.getTranslation("container.keg.chilly");
            case 4 -> BnCTextUtils.getTranslation("container.keg.warm");
            case 5 -> BnCTextUtils.getTranslation("container.keg.hot");
            default -> BnCTextUtils.getTranslation("container.keg.normal");
         };
         this.renderComponentTooltip(pose, List.of(key), mouseX, mouseY);
      }
   }

   @Override
   protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
      super.renderLabels(pose, mouseX, mouseY);
      this.font.draw(pose, this.playerInventoryTitle, 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
   }

   @Override
   protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.minecraft == null) {
         return;
      }
      RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
      this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      // Port (remainder_buffer.md 3.6): the leftover slot's frame, copied from the output slot's.
      this.blit(pose, this.leftPos + 149, this.topPos + 54, 123, 54, 18, 18);
      int l = this.menu.getFermentProgressionScaled();
      this.blit(pose, this.leftPos + PROGRESS_ARROW.x(), this.topPos + PROGRESS_ARROW.y(), 176, 4, l + 1, PROGRESS_ARROW.height());
      if (this.menu.isFermenting()) {
         int bubScale = (int)(this.menu.getProgression() / 80.0F * (float)LEFT_BUBBLE.height()) % (LEFT_BUBBLE.height() + 1);
         this.blit(pose, this.leftPos + LEFT_BUBBLE.x(), this.topPos + LEFT_BUBBLE.y() - bubScale, 176, 79 - bubScale, LEFT_BUBBLE.width(), bubScale + 1);
         this.blit(pose, this.leftPos + RIGHT_BUBBLE.x(), this.topPos + RIGHT_BUBBLE.y() - bubScale, 186, 79 - bubScale, RIGHT_BUBBLE.width(), bubScale + 1);
      }

      int temp = this.menu.getKegTemperature();
      if (temp == 1) {
         this.blit(pose, this.leftPos + COLD_BAR.x(), this.topPos + COLD_BAR.y(), 176, 0, COLD_BAR.width(), COLD_BAR.height());
      }

      if (temp < 3) {
         this.blit(pose, this.leftPos + CHILLY_BAR.x(), this.topPos + CHILLY_BAR.y(), 184, 0, CHILLY_BAR.width(), CHILLY_BAR.height());
      }

      if (temp > 3) {
         this.blit(pose, this.leftPos + WARM_BAR.x(), this.topPos + WARM_BAR.y(), 201, 0, WARM_BAR.width(), WARM_BAR.height());
      }

      if (temp == 5) {
         this.blit(pose, this.leftPos + HOT_BAR.x(), this.topPos + HOT_BAR.y(), 210, 0, HOT_BAR.width(), HOT_BAR.height());
      }

      AbstractedFluidStack fluidStack = this.menu.kegTank.getAbstractedFluid();
      Recipe<?> ghost = this.recipeBookComponent.getGhostRecipe();
      if (!fluidStack.isEmpty()
         && (ghost == null
            || !(ghost instanceof KegFermentingRecipe fermentingRecipe)
            || fermentingRecipe.getFluidIngredient().isEmpty() && this.menu.kegTank.isEmpty()
            || fermentingRecipe.getFluidIngredient().isPresent() && fermentingRecipe.getFluidIngredient().get().ingredient().matches(fluidStack))) {
         if (BnCConfiguration.CLIENT_CONFIG.get().renderFluidInKeg()) {
            BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, pose, this.leftPos + 120, this.topPos + 19, 1.0F);
         }

         ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(fluidStack).copy();
         Optional<KegPouringRecipe> pouringRecipe = Minecraft.getInstance()
            .level
            .getRecipeManager()
            .getAllRecipesFor(BnCRecipeTypes.KEG_POURING)
            .stream()
            .sorted(Comparator.comparing(KegPouringRecipe::isStrict))
            .filter(r -> r.isStrict() ? ItemStack.isSameItemSameTags(itemDisplay, r.getResultItem()) : BnCItemUtils.isSameItem(itemDisplay, r.getResultItem()))
            .findFirst();
         int pourCount = pouringRecipe.map(r -> (int)(Math.min(this.menu.kegTank.getFluidCapacity(), fluidStack.amount()) / r.getLoaderAmount())).orElse(1);
         itemDisplay.setCount(pourCount);
         if (!itemDisplay.isEmpty()) {
            this.itemRenderer.renderAndDecorateItem(itemDisplay, this.leftPos + 124, this.topPos + 23);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemDisplay, this.leftPos + 124, this.topPos + 23);
         }
      }
   }

   @Override
   protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
      if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, buttonId);
      }
   }

   @Override
   protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int buttonIdx) {
      boolean flag = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + this.imageWidth) || mouseY >= (double)(y + this.imageHeight);
      return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, buttonIdx);
   }

   @Override
   protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType) {
      super.slotClicked(slot, mouseX, mouseY, clickType);
      this.recipeBookComponent.slotClicked(slot);
   }

   @Override
   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   @Override
   public RecipeBookComponent getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}
