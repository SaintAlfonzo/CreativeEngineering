package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.AlbumSyncNoteC2SP;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import io.github.mortuusars.exposure.world.inventory.slot.AlbumPlayerInventorySlot;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.Button;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.gui.BackportContainerScreen;

public class AlbumScreen extends BackportContainerScreen<AlbumMenu> {
   public static final WidgetSprites SIGN_BUTTON_SPRITES = new WidgetSprites(
      Exposure.resource("album/sign"), Exposure.resource("album/sign_disabled"), Exposure.resource("album/sign_highlighted")
   );
   protected final Pager pager = new Pager()
      .setChangeSound(new SoundEffect(() -> SoundEvents.BOOK_PAGE_TURN))
      .onPageChanged((oldPage, newPage) -> this.clickButton(PagingDirection.fromChange(oldPage, newPage).ordinal()));
   protected final KeyBindings keyBindings = KeyBindings.of(
      Key.press(Minecrft.options().keyInventory).executes(this::onClose),
      Key.press(263).or(Key.press(65)).executes(this.pager::previousPage),
      Key.press(262).or(Key.press(68)).executes(this.pager::nextPage),
      Key.release(263).or(Key.press(65)).executes(this.pager::resetCooldown),
      Key.release(262).or(Key.press(68)).executes(this.pager::resetCooldown)
   );
   protected final List<AlbumScreen.Page> pages = new ArrayList<>();
   @Nullable
   protected Button enterSignModeButton;

   public AlbumScreen(AlbumMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
   }

   protected void init() {
      this.imageWidth = 298;
      this.imageHeight = 188;
      super.init();
      this.titleLabelY = -999;
      this.inventoryLabelX = 69;
      this.inventoryLabelY = -999;
      this.pages.clear();
      AlbumScreen.Page leftPage = this.createPage(Side.LEFT, 0);
      this.pages.add(leftPage);
      ImageButton previousPageButton = new ImageButton(
         this.leftPos + 12,
         this.topPos + 164,
         13,
         15,
         AlbumGUI.PREVIOUS_PAGE_BUTTON_SPRITES,
         button -> this.pager.changePage(PagingDirection.PREVIOUS),
         Component.translatable("gui.exposure.previous_page")
      );
      previousPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.previous_page")));
      this.addRenderableWidget(previousPageButton);
      AlbumScreen.Page rightPage = this.createPage(Side.RIGHT, 140);
      this.pages.add(rightPage);
      ImageButton nextPageButton = new ImageButton(
         this.leftPos + 273,
         this.topPos + 164,
         13,
         15,
         AlbumGUI.NEXT_PAGE_BUTTON_SPRITES,
         button -> this.pager.changePage(PagingDirection.NEXT),
         Component.translatable("gui.exposure.next_page")
      );
      nextPageButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.next_page")));
      this.addRenderableWidget(nextPageButton);
      if (((AlbumMenu)this.getMenu()).isAlbumEditable()) {
         this.enterSignModeButton = new ImageButton(
            this.leftPos - 23, this.topPos + 17, 22, 22, SIGN_BUTTON_SPRITES, b -> this.enterSignMode(), Component.translatable("gui.exposure.album.sign")
         );
         this.enterSignModeButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.sign")));
         this.addRenderableWidget(this.enterSignModeButton);
      }

      int spreadsCount = (int)Math.ceil((double)((float)((AlbumMenu)this.getMenu()).getPages().size() / 2.0F));
      this.pager.setPagesCount(spreadsCount).setPreviousPageButton(previousPageButton).setNextPageButton(nextPageButton);
   }

   protected void containerTick() {
      this.forEachPage(page -> page.noteWidget.ifLeft(TextBox::tick));
   }

   protected AlbumScreen.Page createPage(Side side, int xOffset) {
      int x = this.leftPos + xOffset;
      int y = this.topPos;
      Rect2i page = new Rect2i(x, y, 149, 188);
      Rect2i photo = new Rect2i(x + 25, y + 21, 108, 108);
      Rect2i note = new Rect2i(x + 22, y + 133, 114, 27);
      PhotographSlotWidget photographWidget = new PhotographSlotWidget(
         this, photo.getX(), photo.getY(), photo.getWidth(), photo.getHeight(), () -> ((AlbumMenu)this.getMenu()).getPhotograph(side)
      ) {
         @Override
         public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return !AlbumScreen.this.isInAddingMode() && super.mouseClicked(mouseX, mouseY, button);
         }

         public boolean isHovered() {
            return !AlbumScreen.this.isInAddingMode() && super.isHovered();
         }
      };
      photographWidget.editable(((AlbumMenu)this.getMenu()).isAlbumEditable()).primaryAction(widget -> {
         if (!widget.inspectPhotograph() && widget.getPhotograph().isEmpty() && widget.isEditable()) {
            this.clickButton(side == Side.LEFT ? 2 : 3);
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }
      }).secondaryAction(widget -> {
         if (widget.isEditable() && !widget.getPhotograph().isEmpty()) {
            this.clickButton(side == Side.LEFT ? 2 : 3);
            Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.7F, 1.1F));
         }
      });
      this.addRenderableWidget(photographWidget);
      Either<TextBox, TextBlock> noteWidget;
      if (((AlbumMenu)this.getMenu()).isAlbumEditable()) {
         TextBox textBox = new TextBox(
               this.font,
               note.getX(),
               note.getY(),
               note.getWidth(),
               note.getHeight(),
               () -> ((AlbumMenu)this.getMenu()).getPage(side).map(AlbumPage::note).orElse(""),
               text -> this.onNoteChanged(side, text)
            )
            .setFontColor(Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR))
            .setSelectionColor(Config.getColor(Config.Client.ALBUM_SELECTION_COLOR), Config.getColor(Config.Client.ALBUM_SELECTION_UNFOCUSED_COLOR));
         textBox.horizontalAlignment = HorizontalAlignment.CENTER;
         this.addRenderableWidget(textBox);
         noteWidget = Either.left(textBox);
      } else {
         TextBlock textBlock = new TextBlock(
            this.font, note.getX(), note.getY(), note.getWidth(), note.getHeight(), this.getNoteComponent(side), this::handleComponentClicked
         );
         textBlock.fontColor = Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR);
         textBlock.alignment = HorizontalAlignment.CENTER;
         textBlock.drawShadow = false;
         noteWidget = Either.right(textBlock);
      }

      return new AlbumScreen.Page(side, page, photo, note, photographWidget, noteWidget);
   }

   protected void onNoteChanged(Side side, String noteText) {
      int pageIndex = ((AlbumMenu)this.getMenu()).getCurrentSpreadIndex() * 2 + side.getIndex();
      ((AlbumMenu)this.getMenu()).updatePage(pageIndex, page -> page.setNote(noteText));
      Packets.sendToServer(new AlbumSyncNoteC2SP(pageIndex, noteText));
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.updateWidgetVisibility();
      this.inventoryLabelY = this.isInAddingMode() ? ((AlbumPlayerInventorySlot)((AlbumMenu)this.getMenu()).getPlayerInventorySlots().get(0)).y - 12 : -999;
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);

      for (AlbumScreen.Page page : this.pages) {
         if (page.getNoteWidget() instanceof TextBlock textBlock) {
            textBlock.render(guiGraphics, mouseX, mouseY, partialTick);
         }
      }

      if (this.isInAddingMode()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();

         for (Slot slot : ((AlbumMenu)this.getMenu()).slots) {
            if (!slot.getItem().isEmpty() && !(slot.getItem().getItem() instanceof PhotographItem)) {
               guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 350, 176.0F, 188.0F, 18, 18, 512, 512);
            }
         }

         RenderSystem.disableBlend();
      }

      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   private void updateWidgetVisibility() {
      this.forEachPage(pagex -> pagex.getNoteWidget().visible = !this.isInAddingMode());

      for (AlbumScreen.Page page : this.pages) {
         page.photographWidget.visible = !((AlbumMenu)this.getMenu()).getPhotograph(page.side).isEmpty()
            || !this.isInAddingMode() && ((AlbumMenu)this.getMenu()).isAlbumEditable();
      }

      if (this.enterSignModeButton != null) {
         this.enterSignModeButton.visible = ((AlbumMenu)this.getMenu()).canSignAlbum();
      }
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderTransparentBackground(guiGraphics);
      this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 15.0F);
      super.renderLabels(guiGraphics, mouseX, mouseY);
      guiGraphics.pose().popPose();
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      if (!this.isInAddingMode()
         || this.hoveredSlot == null
         || this.hoveredSlot.getItem().isEmpty()
         || this.hoveredSlot.getItem().getItem() instanceof PhotographItem) {
         if (!this.isInAddingMode()) {
            for (AlbumScreen.Page page : this.pages) {
               if (page.photographWidget.isHoveredOrFocused()) {
                  page.photographWidget.renderTooltip(guiGraphics, x, y);
                  return;
               }

               if (((AlbumMenu)this.getMenu()).isAlbumEditable() && page.isMouseOver(page.noteArea, (double)x, (double)y)) {
                  List<Component> tooltip = new ArrayList<>();
                  tooltip.add(Component.translatable("gui.exposure.album.note"));
                  if (!page.getNoteWidget().isFocused()) {
                     tooltip.add(Component.translatable("gui.exposure.album.left_click_to_edit"));
                  }

                  boolean hasText = page.noteWidget.left().map(box -> !box.getText().isEmpty()).orElse(false);
                  if (hasText) {
                     tooltip.add(Component.translatable("gui.exposure.album.right_click_to_clear"));
                  }

                  guiGraphics.renderTooltip(this.font, tooltip, Optional.empty(), x, y);
                  return;
               }
            }
         }

         super.renderTooltip(guiGraphics, x, y);
      }
   }

   @NotNull
   public List<Component> getTooltipFromItem(ItemStack stack) {
      List<Component> tooltipLines = super.getTooltipFromItem(stack);
      if (this.isInAddingMode() && this.hoveredSlot != null && this.hoveredSlot.getItem() == stack && stack.getItem() instanceof PhotographItem) {
         tooltipLines.add(Component.empty());
         tooltipLines.add(Component.translatable("gui.exposure.album.left_click_to_add"));
      }

      return tooltipLines;
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 512);
      if (this.enterSignModeButton != null && this.enterSignModeButton.visible) {
         guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos - 27, this.topPos + 14, 447.0F, 0.0F, 27, 28, 512, 512);
      }

      int currentSpreadIndex = ((AlbumMenu)this.getMenu()).getCurrentSpreadIndex();
      this.drawPageNumbers(guiGraphics, currentSpreadIndex);
      if (this.isInAddingMode()) {
         AlbumPlayerInventorySlot firstSlot = (AlbumPlayerInventorySlot)((AlbumMenu)this.getMenu()).getPlayerInventorySlots().get(0);
         int x = firstSlot.x - 8;
         int y = firstSlot.y - 18;
         guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos + x, this.topPos + y, 10, 0.0F, 188.0F, 176, 100, 512, 512);
         Side pageBeingAddedTo = ((AlbumMenu)this.getMenu()).getSideBeingAddedTo();

         for (AlbumScreen.Page page : this.pages) {
            if (page.side == pageBeingAddedTo) {
               guiGraphics.blitSprite(
                  PhotographSlotWidget.EMPTY_SPRITES.enabledFocused(),
                  page.photoArea.getX(),
                  page.photoArea.getY(),
                  page.photoArea.getWidth(),
                  page.photoArea.getHeight()
               );
            }
         }
      }
   }

   protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex) {
      Font font = Minecrft.get().font;
      String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
      String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);
      guiGraphics.drawString(
         font,
         leftPageNumber,
         this.leftPos + 71 + (8 - font.width(leftPageNumber) / 2),
         this.topPos + 167,
         Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR),
         false
      );
      guiGraphics.drawString(
         font,
         rightPageNumber,
         this.leftPos + 212 + (8 - font.width(rightPageNumber) / 2),
         this.topPos + 167,
         Config.getColor(Config.Client.ALBUM_FONT_SECONDARY_COLOR),
         false
      );
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isInAddingMode()) {
         if (this.isHoveringOverInventory(mouseX, mouseY)
            || this.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, button) && !((AlbumMenu)this.getMenu()).getCarried().isEmpty()) {
            return super.mouseClicked(mouseX, mouseY, button);
         } else {
            this.clickButton(-1);
            return true;
         }
      } else {
         for (AlbumScreen.Page page : this.pages) {
            if (((AlbumMenu)this.getMenu()).isAlbumEditable() && button == 1 && page.isMouseOver(page.noteArea, mouseX, mouseY)) {
               page.noteWidget.ifLeft(box -> box.setText(""));
               return true;
            }
         }

         boolean handled = super.mouseClicked(mouseX, mouseY, button);

         for (AlbumScreen.Page pagex : this.pages) {
            if (pagex.getNoteWidget() instanceof TextBlock textBlock && textBlock.mouseClicked(mouseX, mouseY, button)) {
               handled = true;
               break;
            }
         }

         for (AlbumScreen.Page pagexx : this.pages) {
            if (pagexx.getNoteWidget().isFocused() && !pagexx.isMouseOver(pagexx.noteArea, mouseX, mouseY)) {
               this.setFocused(null);
               return true;
            }
         }

         if (!(this.getFocused() instanceof TextBox)) {
            this.setFocused(null);
         }

         return handled;
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (this.isQuickCrafting && !((AlbumMenu)this.getMenu()).getCarried().isEmpty() && ((AlbumMenu)this.getMenu()).getCarried().getCount() == 1) {
         this.isQuickCrafting = false;
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean handleComponentClicked(@Nullable Style style) {
      if (style == null) {
         return false;
      } else {
         ClickEvent clickEvent = style.getClickEvent();
         if (clickEvent == null) {
            return false;
         } else if (clickEvent.getAction() == Action.CHANGE_PAGE) {
            String pageIndexStr = clickEvent.getValue();
            int pageIndex = Integer.parseInt(pageIndexStr) - 1;
            this.forcePage(pageIndex);
            return true;
         } else {
            boolean handled = super.handleComponentClicked(style);
            if (handled && clickEvent.getAction() == Action.RUN_COMMAND) {
               this.onClose();
            }

            return handled;
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      return this.isInAddingMode()
         ? super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
         : this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
   }

   protected void clickButton(int buttonId) {
      ((AlbumMenu)this.getMenu()).clickMenuButton(Minecrft.player(), buttonId);
      Minecrft.gameMode().handleInventoryButtonClick(((AlbumMenu)this.getMenu()).containerId, buttonId);
      if (buttonId == -1) {
         this.setFocused(null);
      }

      if (buttonId == AlbumMenu.PREVIOUS_PAGE_BUTTON || buttonId == AlbumMenu.NEXT_PAGE_BUTTON) {
         for (AlbumScreen.Page page : this.pages) {
            page.noteWidget.ifLeft(TextBox::setCursorToEnd).ifRight(textBlock -> textBlock.setMessage(this.getNoteComponent(page.side)));
         }
      }
   }

   protected boolean isHoveringOverInventory(double mouseX, double mouseY) {
      if (!this.isInAddingMode()) {
         return false;
      } else {
         AlbumPlayerInventorySlot firstSlot = (AlbumPlayerInventorySlot)((AlbumMenu)this.getMenu()).getPlayerInventorySlots().get(0);
         int x = firstSlot.x - 8;
         int y = firstSlot.y - 18;
         return this.isHovering(x, y, 176, 100, mouseX, mouseY);
      }
   }

   protected boolean isHoveringOverSignElement(double mouseX, double mouseY) {
      return this.enterSignModeButton != null
         && this.enterSignModeButton.visible
         && this.isHovering(this.leftPos - 27, this.topPos + 14, 27, 28, mouseX, mouseY);
   }

   protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
      return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)
         && !this.isHoveringOverInventory(mouseX, mouseY)
         && !this.isHoveringOverSignElement(mouseX, mouseY);
   }

   protected boolean forcePage(int pageIndex) {
      try {
         int newSpreadIndex = pageIndex / 2;
         if (newSpreadIndex != ((AlbumMenu)this.getMenu()).getCurrentSpreadIndex()
            && newSpreadIndex >= 0
            && newSpreadIndex <= ((AlbumMenu)this.getMenu()).getPages().size() / 2) {
            PagingDirection pagingDirection = newSpreadIndex < ((AlbumMenu)this.getMenu()).getCurrentSpreadIndex()
               ? PagingDirection.PREVIOUS
               : PagingDirection.NEXT;

            for (int pageChanges = 0;
               (newSpreadIndex != ((AlbumMenu)this.getMenu()).getCurrentSpreadIndex() || !this.pager.canChangePage(pagingDirection)) && pageChanges <= 16;
               pageChanges++
            ) {
               this.pager.changePage(pagingDirection);
            }

            return true;
         } else {
            return false;
         }
      } catch (Exception var5) {
         Exposure.LOGGER.error("Cannot force page: {}", var5.toString());
         return false;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 258) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         for (AlbumScreen.Page page : this.pages) {
            AbstractWidget widget = (AbstractWidget)page.noteWidget.map(box -> box, block -> block);
            if (widget.isFocused()) {
               if (keyCode == 256) {
                  this.setFocused(null);
                  return true;
               }

               return widget.keyPressed(keyCode, scanCode, modifiers);
            }
         }

         if (!this.isInAddingMode() || !Minecrft.options().keyInventory.matches(keyCode, scanCode) && keyCode != 256) {
            return this.keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
         } else {
            this.clickButton(-1);
            return true;
         }
      }
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      for (AlbumScreen.Page page : this.pages) {
         if (((AbstractWidget)page.noteWidget.map(box -> box, block -> block)).isFocused()) {
            return super.keyReleased(keyCode, scanCode, modifiers);
         }
      }

      return this.keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
   }

   @NotNull
   protected Component getNoteComponent(Side side) {
      return (Component)((AlbumMenu)this.getMenu()).getPage(side).map(page -> Component.literal(page.note())).orElse(Component.empty());
   }

   protected void enterSignMode() {
      if (this.isInAddingMode()) {
         this.clickButton(-1);
      }

      Minecrft.get().setScreen(new AlbumSigningScreen(this));
   }

   protected boolean isInAddingMode() {
      return ((AlbumMenu)this.getMenu()).isInAddingPhotographMode();
   }

   protected void forEachPage(Consumer<AlbumScreen.Page> pageAction) {
      for (AlbumScreen.Page page : this.pages) {
         pageAction.accept(page);
      }
   }

   protected class Page {
      public final Side side;
      public final Rect2i pageArea;
      public final Rect2i photoArea;
      public final Rect2i noteArea;
      public final PhotographSlotWidget photographWidget;
      public final Either<TextBox, TextBlock> noteWidget;

      private Page(Side side, Rect2i pageArea, Rect2i photoArea, Rect2i noteArea, PhotographSlotWidget photographWidget, Either<TextBox, TextBlock> noteWidget) {
         this.side = side;
         this.pageArea = pageArea;
         this.photoArea = photoArea;
         this.noteArea = noteArea;
         this.photographWidget = photographWidget;
         this.noteWidget = noteWidget;
      }

      public boolean isMouseOver(Rect2i area, double mouseX, double mouseY) {
         return AlbumScreen.this.isHovering(
            area.getX() - AlbumScreen.this.leftPos, area.getY() - AlbumScreen.this.topPos, area.getWidth(), area.getHeight(), mouseX, mouseY
         );
      }

      public AbstractWidget getNoteWidget() {
         return (AbstractWidget)this.noteWidget.map(box -> box, block -> block);
      }
   }
}
