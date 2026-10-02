package io.github.mortuusars.exposure.client.gui.screen.camera;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.screen.ItemListScreen;
import io.github.mortuusars.exposure.client.gui.screen.element.ToggleImageButton;
import io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast;
import io.github.mortuusars.exposure.client.gui.toast.ToastIcon;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.Filter;
import io.github.mortuusars.exposure.data.Filters;
import io.github.mortuusars.exposure.data.Lenses;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.util.supporter.Supporters;
import io.github.mortuusars.exposure.world.inventory.AbstractCameraAttachmentsMenu;
import io.github.mortuusars.exposure.world.inventory.CameraInHandAttachmentsMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.Registry;
import io.github.mortuusars.exposure.backport.gui.BackportContainerScreen;

public class CameraAttachmentsScreen extends BackportContainerScreen<AbstractCameraAttachmentsMenu> {
   public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/camera_attachments.png");
   public static final WidgetSprites SKIN_REGULAR_BUTTON_SPRITES = Widgets.threeStateSprites(Exposure.resource("camera_attachments/regular"));
   public static final WidgetSprites SKIN_GOLD_BUTTON_SPRITES = Widgets.threeStateSprites(Exposure.resource("camera_attachments/gold"));
   protected final Player player;
   protected Map<Integer, Rect2i> slotPlaceholders = Map.of(
      0, new Rect2i(238, 0, 18, 18), 1, new Rect2i(238, 18, 18, 18), 2, new Rect2i(238, 36, 18, 18), 3, new Rect2i(238, 54, 18, 18)
   );
   protected final CameraAttachmentsScreen.HoveredElement flash = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(96, 11, 28, 27)), () -> ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(1).hasItem()
   );
   protected final CameraAttachmentsScreen.HoveredElement filterOnLens = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(114, 57, 13, 6), new Rect2i(110, 63, 17, 24)), () -> ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(2).hasItem()
   );
   protected final CameraAttachmentsScreen.HoveredElement lens = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(93, 48, 33, 34)), () -> ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(2).hasItem()
   );
   protected final CameraAttachmentsScreen.HoveredElement filter = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(110, 55, 13, 6), new Rect2i(106, 61, 17, 24)), () -> !((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(2).hasItem()
   );
   protected final CameraAttachmentsScreen.HoveredElement lensBuiltIn = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(93, 48, 29, 32)), () -> !((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(2).hasItem()
   );
   protected final CameraAttachmentsScreen.HoveredElement selfTimer = new CameraAttachmentsScreen.HoveredElement(List.of(new Rect2i(92, 77, 6, 7)), () -> true);
   protected final CameraAttachmentsScreen.HoveredElement viewfinder = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(65, 25, 30, 12), new Rect2i(72, 31, 39, 11), new Rect2i(80, 42, 24, 5)), () -> true
   );
   protected final CameraAttachmentsScreen.HoveredElement film = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(48, 33, 15, 38), new Rect2i(52, 24, 16, 11)), () -> true
   );
   protected final CameraAttachmentsScreen.HoveredElement shutterSpeedKnob = new CameraAttachmentsScreen.HoveredElement(
      List.of(new Rect2i(68, 49, 21, 26)), () -> true
   );
   protected long openedAt = System.currentTimeMillis();
   protected boolean hasHoveredOverPart;

   public CameraAttachmentsScreen(AbstractCameraAttachmentsMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
      this.player = playerInventory.player;
      this.showTutorialToasts();
   }

   protected void showTutorialToasts() {
      if ((Boolean)Config.Client.ATTACHMENTS_SHOW_INFO_TOAST.get()) {
         Minecrft.get()
            .getToasts()
            .addToast(
               new BetterTutorialToast(
                  ToastIcon.HOVER,
                  Component.translatable("gui.exposure.camera_attachments.mouse_over_toast.title"),
                  Component.translatable("gui.exposure.camera_attachments.mouse_over_toast.message"),
                  () -> {
                     if (Minecrft.get().screen != this) {
                        Config.Client.ATTACHMENTS_SHOW_INFO_TOAST.set(true);
                        Config.Client.SPEC.save();
                        return true;
                     } else {
                        return this.hasHoveredOverPart;
                     }
                  }
               )
            );
         Config.Client.ATTACHMENTS_SHOW_INFO_TOAST.set(false);
         Config.Client.SPEC.save();
      }

      if ((Boolean)Config.Client.ATTACHMENTS_SHOW_WIKI_TOAST.get()) {
         Minecrft.get()
            .getToasts()
            .addToast(
               new BetterTutorialToast(
                  ToastIcon.F1,
                  Component.translatable("gui.exposure.camera_attachments.wiki_toast.title"),
                  Component.translatable("gui.exposure.camera_attachments.wiki_toast.message"),
                  7000,
                  () -> {
                     if (Minecrft.get().screen != this && !(Minecrft.get().screen instanceof ConfirmLinkScreen)) {
                        Config.Client.ATTACHMENTS_SHOW_WIKI_TOAST.set(true);
                        Config.Client.SPEC.save();
                        return true;
                     } else {
                        return false;
                     }
                  }
               )
            );
         Config.Client.ATTACHMENTS_SHOW_WIKI_TOAST.set(false);
         Config.Client.SPEC.save();
      }
   }

   protected void init() {
      this.imageHeight = 185;
      this.inventoryLabelY = this.imageHeight - 94;
      super.init();
      if (Supporters.hasAccessToGoldenSkin(Minecrft.player().getUUID())) {
         ToggleImageButton button = new ToggleImageButton(
            this.leftPos + 8, this.topPos + 18, 7, 7, SKIN_GOLD_BUTTON_SPRITES, SKIN_REGULAR_BUTTON_SPRITES, this::changeSkin
         );
         button.setState(
            ((AbstractCameraAttachmentsMenu)this.getMenu()).getCamera().map((i, s) -> (Boolean)Exposure.DataComponents.CAMERA_GOLD.getOrDefault(s, false))
         );
         button.setTooltip(Tooltip.create(Component.translatable("gui.exposure.camera_attachments.change_skin")));
         this.addRenderableWidget(button);
      }
   }

   protected void changeSkin(boolean gold) {
      int buttonId = gold ? 101 : 100;
      Minecrft.gameMode().handleInventoryButtonClick(((AbstractCameraAttachmentsMenu)this.getMenu()).containerId, buttonId);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);

      for (Slot slot : ((AbstractCameraAttachmentsMenu)this.getMenu()).slots) {
         if (!slot.mayPickup(this.player)) {
            guiGraphics.renderItem(slot.getItem(), this.leftPos + slot.x, this.topPos + slot.y);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(TEXTURE, this.leftPos + slot.x - 2, this.topPos + slot.y - 2, 350, 236.0F, 92.0F, 20, 20, 256, 256);
            RenderSystem.disableBlend();
         }
      }

      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.renderSlotPlaceholders(guiGraphics, mouseX, mouseY, partialTick);
      this.renderAttachments(guiGraphics, mouseX, mouseY, partialTick);

      for (Slot slot : ((AbstractCameraAttachmentsMenu)this.getMenu()).slots) {
         if (!slot.mayPickup(this.player)) {
            guiGraphics.blit(TEXTURE, this.leftPos + slot.x - 2, this.topPos + slot.y - 2, 236, 72, 20, 20);
         }
      }

      RenderSystem.disableBlend();
   }

   protected void renderAttachments(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(1).hasItem()) {
         int vOffset = this.isMouseOver(this.flash, mouseX, mouseY) ? 28 : 0;
         guiGraphics.blit(TEXTURE, this.leftPos + 96, this.topPos + 11, 176, vOffset, 28, 28);
      }

      boolean hasLens = ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(2).hasItem();
      if (hasLens) {
         int vOffset = this.isMouseOver(this.lens, mouseX, mouseY) && !this.isMouseOver(this.filterOnLens, mouseX, mouseY) ? 37 : 0;
         guiGraphics.blit(TEXTURE, this.leftPos + 93, this.topPos + 47, 176, 56 + vOffset, 35, 37);
      } else if (this.isMouseOver(this.lensBuiltIn, mouseX, mouseY) && !this.isMouseOver(this.filter, mouseX, mouseY)) {
         guiGraphics.blit(TEXTURE, this.leftPos + 93, this.topPos + 47, 176, 130, 31, 35);
      }

      Slot filterSlot = ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(3);
      int filterX = hasLens ? 102 : 98;
      int filterY = hasLens ? 54 : 52;
      if (filterSlot.hasItem()) {
         Filters.of(Minecrft.registryAccess(), filterSlot.getItem())
            .ifPresent(filter -> this.renderFilter(guiGraphics, mouseX, mouseY, filter, filterX, filterY));
      } else if (this.isMouseOver(this.filterOnLens, mouseX, mouseY)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         guiGraphics.blit(TEXTURE, this.leftPos + 110, this.topPos + 58, 176, 165, 15, 23);
      } else if (this.isMouseOver(this.filter, mouseX, mouseY)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         guiGraphics.blit(TEXTURE, this.leftPos + 106, this.topPos + 56, 176, 165, 15, 23);
      } else if (this.isMouseOver(this.viewfinder, mouseX, mouseY) && !this.isMouseOver(this.flash, mouseX, mouseY)) {
         guiGraphics.blit(TEXTURE, this.leftPos + 65, this.topPos + 24, 42, 185, 49, 26);
      } else if (this.isMouseOver(this.film, mouseX, mouseY)) {
         guiGraphics.blit(TEXTURE, this.leftPos + 47, this.topPos + 20, 0, 185, 42, 52);
      } else if (this.isMouseOver(this.shutterSpeedKnob, mouseX, mouseY)) {
         guiGraphics.blit(TEXTURE, this.leftPos + 68, this.topPos + 49, 148, 185, 21, 26);
      } else if (this.isMouseOver(this.selfTimer, mouseX, mouseY)) {
         guiGraphics.blit(TEXTURE, this.leftPos + 93, this.topPos + 78, 169, 185, 4, 5);
      }
   }

   protected void renderFilter(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, Filter filter, int filterX, int filterY) {
      Color tint = filter.attachmentTintColor();
      float r = tint.getRF();
      float g = tint.getGF();
      float b = tint.getBF();
      if (this.isMouseOver(this.filterOnLens, mouseX, mouseY) || this.isMouseOver(this.filter, mouseX, mouseY)) {
         r *= 1.35F;
         g *= 1.35F;
         b *= 1.35F;
      }

      RenderSystem.setShaderColor(r, g, b, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      ResourceLocation filterTexture = filter.attachmentTexture();
      guiGraphics.blit(filterTexture, this.leftPos + filterX, this.topPos + filterY, 0.0F, 0.0F, 32, 32, 32, 32);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   protected boolean isMouseOver(CameraAttachmentsScreen.HoveredElement element, int mouseX, int mouseY) {
      if (!element.isEnabled.get()) {
         return false;
      } else {
         mouseX -= this.leftPos;
         mouseY -= this.topPos;

         for (Rect2i area : element.hoverArea) {
            if (mouseX >= area.getX() && mouseX < area.getX() + area.getWidth() && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight()) {
               return true;
            }
         }

         return false;
      }
   }

   protected void renderSlotPlaceholders(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      for (int slotIndex : this.slotPlaceholders.keySet()) {
         Slot slot = ((AbstractCameraAttachmentsMenu)this.getMenu()).getSlot(slotIndex);
         if (!slot.hasItem()) {
            Rect2i placeholder = this.slotPlaceholders.get(slotIndex);
            guiGraphics.blit(
               TEXTURE,
               this.leftPos + slot.x - 1,
               this.topPos + slot.y - 1,
               placeholder.getX(),
               placeholder.getY(),
               placeholder.getWidth(),
               placeholder.getHeight()
            );
         }
      }
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      boolean hoveredOverPart = true;
      if (this.isMouseOver(this.flash, x, y)) {
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("flash.tooltip")), x, y);
      } else if (this.isMouseOver(this.viewfinder, x, y)) {
         Component controlsKey = this.translateKey(KeyboardHandler.getCameraControlsKey(), ChatFormatting.GRAY);
         Component middleClick = Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get()
            ? this.translate("viewfinder.tooltip.or_middle_click")
            : Component.empty();
         Component selfieKey = this.translateKey(Minecrft.options().keyTogglePerspective, ChatFormatting.GRAY);
         Component sprintKey = this.translateKey(Minecrft.options().keySprint, ChatFormatting.GRAY);
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("viewfinder.tooltip", controlsKey, middleClick, selfieKey, sprintKey)), x, y);
      } else if (this.isMouseOver(this.shutterSpeedKnob, x, y)) {
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("shutter_speed.tooltip")), x, y);
      } else if (this.isMouseOver(this.filter, x, y) || this.isMouseOver(this.filterOnLens, x, y)) {
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("filter.tooltip")), x, y);
      } else if (this.isMouseOver(this.lens, x, y) || this.isMouseOver(this.lensBuiltIn, x, y)) {
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("lens.tooltip")), x, y);
      } else if (this.isMouseOver(this.film, x, y)) {
         guiGraphics.renderTooltip(this.font, this.getTooltipLines(this.translate("film.tooltip")), x, y);
      } else if (this.isMouseOver(this.selfTimer, x, y)) {
         MutableComponent tooltip = this.translate("self_timer.tooltip");
         if ((Boolean)Config.Server.TIMER_ATTRACTS_MOB_ATTENTION.get()) {
            tooltip.append(this.translate("self_timer_attention.tooltip"));
         }

         guiGraphics.renderTooltip(this.font, this.getTooltipLines(tooltip), x, y);
      } else {
         hoveredOverPart = false;
         super.renderTooltip(guiGraphics, x, y);
      }

      if (!this.hasHoveredOverPart && hoveredOverPart && System.currentTimeMillis() - this.openedAt > 3000L) {
         this.hasHoveredOverPart = true;
      }
   }

   @NotNull
   public List<Component> getTooltipFromItem(ItemStack stack) {
      List<Component> tooltip = super.getTooltipFromItem(stack);
      Lenses.getFocalRange(Minecrft.registryAccess(), stack)
         .ifPresent(
            focalRange -> tooltip.add(
                  Component.translatable("gui.exposure.camera_controls.focal_range", new Object[]{focalRange.getSerializedName()})
                     .withStyle(ChatFormatting.GOLD)
               )
         );
      Filters.of(Minecrft.registryAccess(), stack)
         .filter(f -> Minecrft.options().advancedItemTooltips)
         .ifPresent(filter -> tooltip.add(Component.literal(filter.shader().toString()).withStyle(ChatFormatting.GRAY)));
      return tooltip;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 290) {
         String url = "https://moddedmc.wiki/project/exposure";
         Minecrft.get().setScreen(new ConfirmLinkScreen(confirmed -> {
            if (confirmed) {
               Util.getPlatform().openUri(url);
            }

            Minecrft.get().setScreen(this);
         }, "https://moddedmc.wiki/project/exposure", true));
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = (int)mouseX;
      int y = (int)mouseY;
      if (this.isMouseOver(this.filter, x, y) || this.isMouseOver(this.filterOnLens, x, y)) {
         List<ItemStack> itemStacks = new ArrayList<>();

         for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(Exposure.Tags.Items.FILTERS)) {
            itemStacks.add(new ItemStack(holder));
         }

         ItemListScreen screen = new ItemListScreen(this, Component.translatable("gui.exposure.filters"), itemStacks);
         Minecraft.getInstance().setScreen(screen);
         return true;
      } else if (this.isMouseOver(this.lens, x, y) || this.isMouseOver(this.lensBuiltIn, x, y)) {
         List<ItemStack> itemStacks = new ArrayList<>();

         for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(Exposure.Tags.Items.LENSES)) {
            itemStacks.add(new ItemStack(holder));
         }

         ItemListScreen screen = new ItemListScreen(this, Component.translatable("gui.exposure.lenses"), itemStacks);
         Minecrft.get().setScreen(screen);
         return true;
      } else if (!this.isMouseOver(this.film, x, y)) {
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         List<ItemStack> itemStacks = new ArrayList<>();

         for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(Exposure.Tags.Items.FILM_ROLLS)) {
            itemStacks.add(new ItemStack(holder));
         }

         ItemListScreen screen = new ItemListScreen(this, Component.translatable("gui.exposure.film_rolls"), itemStacks);
         Minecrft.get().setScreen(screen);
         return true;
      }
   }

   public void onClose() {
      super.onClose();
      if (this.getMenu() instanceof CameraInHandAttachmentsMenu attachmentsMenu && attachmentsMenu.isOpenedFromGui()) {
         Minecrft.get().setScreen(new InventoryScreen(this.player));
      }
   }

   protected MutableComponent translate(String key) {
      return Component.translatable("gui.exposure.camera_attachments." + key);
   }

   protected MutableComponent translate(String key, Object... args) {
      return Component.translatable("gui.exposure.camera_attachments." + key, args);
   }

   protected MutableComponent translate(String key, ChatFormatting formatting) {
      return Component.translatable("gui.exposure.camera_attachments." + key).withStyle(formatting);
   }

   protected MutableComponent translateKey(KeyMapping mapping, ChatFormatting formatting) {
      return Component.literal(mapping.getTranslatedKeyMessage().getString()).withStyle(formatting);
   }

   protected List<FormattedCharSequence> getTooltipLines(Component component, int width) {
      return this.font.split(component, width);
   }

   protected List<FormattedCharSequence> getTooltipLines(Component component) {
      return this.font.split(component, this.getMaxTooltipWidth());
   }

   protected int getMaxTooltipWidth() {
      return 250;
   }

   public static record HoveredElement(List<Rect2i> hoverArea, Supplier<Boolean> isEnabled) {
   }
}
