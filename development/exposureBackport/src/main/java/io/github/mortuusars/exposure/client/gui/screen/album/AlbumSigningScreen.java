package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.HorizontalAlignment;
import io.github.mortuusars.exposure.client.gui.screen.element.textbox.TextBox;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.inventory.AlbumMenu;
import net.minecraft.ChatFormatting;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import io.github.mortuusars.exposure.backport.gui.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.mortuusars.exposure.backport.gui.BackportScreen;

public class AlbumSigningScreen extends BackportScreen {
   public static final WidgetSprites CANCEL_BUTTON_SPRITE = new WidgetSprites(
      Exposure.resource("album/cancel"), Exposure.resource("album/cancel_disabled"), Exposure.resource("album/cancel_highlighted")
   );
   public static final int SELECTION_COLOR = -7829249;
   public static final int SELECTION_UNFOCUSED_COLOR = -4473857;
   protected final AlbumScreen parentScreen;
   protected int imageWidth;
   protected int imageHeight;
   protected int leftPos;
   protected int topPos;
   protected TextBox titleTextBox;
   protected ImageButton signButton;
   protected ImageButton cancelSigningButton;
   protected String titleText = "";

   public AlbumSigningScreen(AlbumScreen parent) {
      super(Component.empty());
      this.parentScreen = parent;
   }

   protected void init() {
      this.imageWidth = 149;
      this.imageHeight = 188;
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
      this.titleTextBox = new TextBox(this.font, this.leftPos + 21, this.topPos + 73, 108, 9, () -> this.titleText, text -> this.titleText = text)
         .setFontColor(Config.getColor(Config.Client.ALBUM_FONT_MAIN_COLOR))
         .setSelectionColor(-7829249, -4473857);
      this.titleTextBox.textValidator = text -> text != null && this.font.wordWrapHeight(text, 108) <= 9 && !text.contains("\n");
      this.titleTextBox.horizontalAlignment = HorizontalAlignment.CENTER;
      this.addRenderableWidget(this.titleTextBox);
      this.signButton = new ImageButton(
         this.leftPos + 46,
         this.topPos + 110,
         22,
         22,
         AlbumScreen.SIGN_BUTTON_SPRITES,
         b -> this.signAlbum(),
         Component.translatable("gui.exposure.album.sign")
      );
      MutableComponent component = Component.translatable("gui.exposure.album.sign")
         .append("\n")
         .append(Component.translatable("gui.exposure.album.sign.warning").withStyle(ChatFormatting.GRAY));
      this.signButton.setTooltip(Tooltip.create(component));
      this.addRenderableWidget(this.signButton);
      this.cancelSigningButton = new ImageButton(
         this.leftPos + 83,
         this.topPos + 111,
         22,
         22,
         CANCEL_BUTTON_SPRITE,
         b -> this.cancelSigning(),
         Component.translatable("gui.exposure.album.cancel_signing")
      );
      this.cancelSigningButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.album.cancel_signing")));
      this.addRenderableWidget(this.cancelSigningButton);
      this.setInitialFocus(this.titleTextBox);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      this.titleTextBox.tick();
   }

   private void updateButtons() {
      this.signButton.active = this.canSign();
   }

   protected boolean canSign() {
      return !this.titleText.isEmpty();
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.updateButtons();
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderLabels(guiGraphics);
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderTransparentBackground(guiGraphics);
      guiGraphics.blit(AlbumGUI.TEXTURE, this.leftPos, this.topPos, 0, 298.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 512);
   }

   private void renderLabels(GuiGraphics guiGraphics) {
      MutableComponent component = Component.translatable("gui.exposure.album.enter_title");
      guiGraphics.drawString(this.font, component, this.leftPos + 74 - this.font.width(component) / 2, this.topPos + 50, 16116688, false);
      component = Component.translatable("gui.exposure.album.by_author", new Object[]{Minecrft.player().getScoreboardName()});
      guiGraphics.drawString(this.font, component, this.leftPos + 74 - this.font.width(component) / 2, this.topPos + 84, 13087894, false);
   }

   protected void signAlbum() {
      if (this.canSign()) {
         ((AlbumMenu)this.parentScreen.getMenu()).setTitle(this.titleText);
         ((AlbumMenu)this.parentScreen.getMenu()).signAlbum(Minecrft.player());
         this.onClose();
      }
   }

   protected void cancelSigning() {
      Minecrft.get().setScreen(this.parentScreen);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 258) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else if (keyCode == 256) {
         this.cancelSigning();
         return true;
      } else {
         return this.titleTextBox.isFocused() ? this.titleTextBox.keyPressed(keyCode, scanCode, modifiers) : super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
