package io.github.mortuusars.exposure.client.gui.screen.album;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.gui.screen.element.TextBlock;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.component.album.AlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.AlbumPage;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumContent;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.AbstractWidget;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;

public class AlbumViewScreen extends BackportScreen {
   protected final Pager pager = new Pager().setChangeSound(new SoundEffect(() -> SoundEvents.BOOK_PAGE_TURN)).onPageChanged(this::onSpreadChanged);
   protected final KeyBindings keyBindings = KeyBindings.of(
      Key.press(Minecrft.options().keyInventory).executes(this::onClose),
      Key.press(263).or(Key.press(65)).executes(this.pager::previousPage),
      Key.press(262).or(Key.press(68)).executes(this.pager::nextPage),
      Key.release(263).or(Key.press(65)).executes(this.pager::resetCooldown),
      Key.release(262).or(Key.press(68)).executes(this.pager::resetCooldown)
   );
   protected final List<AlbumViewScreen.Page> pages = new ArrayList<>();
   protected AlbumViewScreen.AlbumAccess albumAccess;
   protected int imageWidth;
   protected int imageHeight;
   protected int leftPos;
   protected int topPos;

   public AlbumViewScreen(AlbumViewScreen.AlbumAccess albumAccess) {
      super(Component.empty());
      this.albumAccess = albumAccess;
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void init() {
      this.imageWidth = 298;
      this.imageHeight = 188;
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      this.pages.clear();
      AlbumViewScreen.Page leftPage = this.createPage(Side.LEFT, 0);
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
      AlbumViewScreen.Page rightPage = this.createPage(Side.RIGHT, 140);
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
      int spreadsCount = (int)Math.ceil((double)((float)this.albumAccess.pages().size() / 2.0F));
      this.pager.setPagesCount(spreadsCount).setPreviousPageButton(previousPageButton).setNextPageButton(nextPageButton);
   }

   protected AlbumViewScreen.Page createPage(Side side, int xOffset) {
      int x = this.leftPos + xOffset;
      int y = this.topPos;
      PhotographSlotWidget photographWidget = new PhotographSlotWidget(
            this, x + 25, y + 21, 108, 108, () -> this.getPage(side).orElse(AlbumViewScreen.PageContent.EMPTY).photograph()
         )
         .editable(false)
         .primaryAction(widget -> this.inspectPhotograph(widget.getPhotograph()));
      this.addRenderableWidget(photographWidget);
      TextBlock noteWidget = new TextBlock(
         this.font, x + 22, y + 133, 114, 27, this.getPage(side).orElse(AlbumViewScreen.PageContent.EMPTY).note(), this::handleComponentClicked
      );
      noteWidget.fontColor = Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR);
      noteWidget.alignment = HorizontalAlignment.CENTER;
      noteWidget.drawShadow = false;
      return new AlbumViewScreen.Page(side, photographWidget, noteWidget);
   }

   public List<AlbumViewScreen.PageContent> getPages() {
      return this.albumAccess.pages();
   }

   public Optional<AlbumViewScreen.PageContent> getPage(int pageIndex) {
      return pageIndex <= this.getPages().size() - 1 ? Optional.ofNullable(this.getPages().get(pageIndex)) : Optional.empty();
   }

   public Optional<AlbumViewScreen.PageContent> getPage(Side side) {
      return this.getPage(this.getCurrentSpreadIndex() * 2 + side.getIndex());
   }

   public int getCurrentSpreadIndex() {
      return this.pager.getPage();
   }

   protected void onSpreadChanged(int oldSpread, int newSpread) {
      this.forEachPage(page -> {
         Component note = this.getPage(page.side).orElse(AlbumViewScreen.PageContent.EMPTY).note();
         page.noteWidget().setMessage(note);
      });
   }

   public void setAlbumAccess(AlbumViewScreen.AlbumAccess albumAccess) {
      this.albumAccess = albumAccess;
      this.pager.setPagesCount(albumAccess.getPageCount() / 2);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);

      for (AlbumViewScreen.Page page : this.pages) {
         AbstractWidget noteWidget = page.noteWidget();
         if (noteWidget instanceof TextBlock textBlock) {
            textBlock.render(guiGraphics, mouseX, mouseY, partialTick);
         }
      }

      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      for (AlbumViewScreen.Page page : this.pages) {
         if (page.photographWidget().isHoveredOrFocused()) {
            page.photographWidget().renderTooltip(guiGraphics, x, y);
            return;
         }
      }
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderTransparentBackground(guiGraphics);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 512);
      int currentSpreadIndex = this.getCurrentSpreadIndex();
      this.drawPageNumbers(guiGraphics, currentSpreadIndex, mouseX, mouseY);
   }

   protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
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
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         for (AlbumViewScreen.Page page : this.pages) {
            if (page.noteWidget().mouseClicked(mouseX, mouseY, button)) {
               return true;
            }
         }

         return false;
      }
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

   protected void forcePage(int pageIndex) {
      this.pager.changePage(pageIndex / 2);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
      return this.keyBindings.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
   }

   protected void inspectPhotograph(ItemStack photograph) {
      if (photograph.getItem() instanceof PhotographItem) {
         Minecrft.get().setScreen(new ChildPhotographScreen(this, List.of(new ItemAndStack<>(photograph))));
         Minecrft.get()
            .getSoundManager()
            .play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(), Minecrft.level().getRandom().nextFloat() * 0.2F + 1.3F, 0.75F));
      }
   }

   protected void forEachPage(Consumer<AlbumViewScreen.Page> pageAction) {
      for (AlbumViewScreen.Page page : this.pages) {
         pageAction.accept(page);
      }
   }

   public static record AlbumAccess(List<AlbumViewScreen.PageContent> pages) {
      public static final AlbumViewScreen.AlbumAccess EMPTY = new AlbumViewScreen.AlbumAccess(Collections.emptyList());

      public int getPageCount() {
         return this.pages.size();
      }

      public static AlbumViewScreen.AlbumAccess fromItem(ItemStack stack) {
         if ((Object)Exposure.DataComponents.ALBUM_CONTENT.get(stack) instanceof AlbumContent content) {
            return new AlbumViewScreen.AlbumAccess(content.pages().stream().map(AlbumViewScreen.PageContent::new).toList());
         } else {
            return (Object)Exposure.DataComponents.SIGNED_ALBUM_CONTENT.get(stack) instanceof SignedAlbumContent content
               ? new AlbumViewScreen.AlbumAccess(content.pages().stream().map(AlbumViewScreen.PageContent::new).toList())
               : EMPTY;
         }
      }
   }

   protected static record Page(Side side, PhotographSlotWidget photographWidget, TextBlock noteWidget) {
   }

   public static record PageContent(ItemStack photograph, Component note) {
      public static final AlbumViewScreen.PageContent EMPTY = new AlbumViewScreen.PageContent(ItemStack.EMPTY, Component.empty());

      public PageContent(AlbumPage page) {
         this(page.photograph(), Component.literal(page.note()));
      }

      public PageContent(SignedAlbumPage page) {
         this(page.photograph(), page.note());
      }
   }
}
