package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.inventory.LecternAlbumMenu;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LecternAlbumScreen extends AlbumViewScreen implements MenuAccess<LecternAlbumMenu> {
   private final LecternAlbumMenu menu;
   private final ContainerListener listener = new ContainerListener() {
      public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
         LecternAlbumScreen.this.bookChanged();
      }

      public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
         if (dataSlotIndex == 0) {
            LecternAlbumScreen.this.pageChanged();
         }
      }
   };

   public LecternAlbumScreen(LecternAlbumMenu menu, Inventory playerInventory, Component title) {
      super(AlbumViewScreen.AlbumAccess.fromItem(menu.getBook()));
      this.menu = menu;
      this.pager.setChangeSound(null);
   }

   @NotNull
   public LecternAlbumMenu getMenu() {
      return this.menu;
   }

   @Override
   protected void init() {
      super.init();
      if (Minecrft.player().mayBuild()) {
         this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE, b -> this.onClose()).bounds(this.width / 2 - 100, this.topPos + 196, 98, 20).build()
         );
         this.addRenderableWidget(
            Button.builder(Component.translatable("lectern.take_book"), b -> this.sendButtonClick(3))
               .bounds(this.width / 2 + 2, this.topPos + 196, 98, 20)
               .build()
         );
      }

      this.menu.addSlotListener(this.listener);
   }

   public void removed() {
      super.removed();
      this.menu.removeSlotListener(this.listener);
   }

   @Override
   protected void onSpreadChanged(int oldSpread, int newSpread) {
      super.onSpreadChanged(oldSpread, newSpread);
      this.sendButtonClick(100 + newSpread * 2);
   }

   protected void pageChanged() {
      this.pager.changePage(this.menu.getPage() / 2);
   }

   protected void bookChanged() {
      ItemStack itemStack = this.menu.getBook();
      this.setAlbumAccess(AlbumViewScreen.AlbumAccess.fromItem(itemStack));
   }

   @Override
   protected void forcePage(int pageIndex) {
      this.sendButtonClick(100 + pageIndex);
   }

   protected void sendButtonClick(int buttonId) {
      Minecrft.gameMode().handleInventoryButtonClick(this.menu.containerId, buttonId);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int page = this.getMenu().getPage();
      if (page % 2 == 1 && this.isHovering(70, 167, 17, 7, mouseX, mouseY)) {
         this.sendButtonClick(100 + page - 1);
      } else if (page % 2 == 0 && this.isHovering(210, 167, 17, 7, mouseX, mouseY)) {
         this.sendButtonClick(100 + page + 1);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   protected void drawPageNumbers(GuiGraphics guiGraphics, int currentSpreadIndex, int mouseX, int mouseY) {
      super.drawPageNumbers(guiGraphics, currentSpreadIndex, mouseX, mouseY);
      int page = this.getMenu().getPage();
      String leftPageNumber = Integer.toString(currentSpreadIndex * 2 + 1);
      String rightPageNumber = Integer.toString(currentSpreadIndex * 2 + 2);
      if (page % 2 == 1 && this.isHovering(70, 167, 17, 7, (double)mouseX, (double)mouseY)) {
         guiGraphics.drawString(
            this.font,
            leftPageNumber,
            this.leftPos + 71 + (8 - this.font.width(leftPageNumber) / 2),
            this.topPos + 167,
            Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR),
            false
         );
      } else if (page % 2 == 0 && this.isHovering(210, 167, 17, 7, (double)mouseX, (double)mouseY)) {
         guiGraphics.drawString(
            this.font,
            rightPageNumber,
            this.leftPos + 212 + (8 - this.font.width(rightPageNumber) / 2),
            this.topPos + 167,
            Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR),
            false
         );
      }
   }

   @Override
   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      super.renderTooltip(guiGraphics, x, y);
      int page = this.getMenu().getPage();
      if (page % 2 == 1 && this.isHovering(70, 167, 17, 7, (double)x, (double)y)) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.exposure.album.lectern.set_current_page"), x, y);
      } else if (page % 2 == 0 && this.isHovering(210, 167, 17, 7, (double)x, (double)y)) {
         guiGraphics.renderTooltip(this.font, Component.translatable("gui.exposure.album.lectern.set_current_page"), x, y);
      }
   }

   protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
      mouseX -= (double)this.leftPos;
      mouseY -= (double)this.topPos;
      return mouseX >= (double)(x - 1) && mouseX < (double)(x + width + 1) && mouseY >= (double)(y - 1) && mouseY < (double)(y + height + 1);
   }
}
