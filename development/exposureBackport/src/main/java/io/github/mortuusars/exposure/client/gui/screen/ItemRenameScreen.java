package io.github.mortuusars.exposure.client.gui.screen;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.world.inventory.ItemRenameMenu;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import io.github.mortuusars.exposure.backport.gui.ImageButton;
import io.github.mortuusars.exposure.backport.gui.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import io.github.mortuusars.exposure.backport.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.mortuusars.exposure.backport.gui.BackportContainerScreen;

public class ItemRenameScreen extends BackportContainerScreen<ItemRenameMenu> {
   public static final ResourceLocation TEXTURE = Exposure.resource("textures/gui/item_rename.png");
   protected EditBox name;

   public ItemRenameScreen(ItemRenameMenu menu, Inventory playerInventory, Component title) {
      super(menu, playerInventory, title);
   }

   protected void init() {
      this.imageWidth = 206;
      this.imageHeight = 66;
      this.inventoryLabelX = -999;
      this.inventoryLabelY = -999;
      this.titleLabelX = 28;
      super.init();
      this.name = new EditBox(this.font, this.leftPos + 32, this.topPos + 21, 142, 12, Component.translatable("gui.exposure.item_rename.title"));
      this.name.setTextColor(-1);
      this.name.setTextColorUneditable(-1);
      this.name.setBordered(false);
      this.name.setMaxLength(50);
      this.name.setResponder(this::onNameChanged);
      this.name.setValue(((ItemRenameMenu)this.getMenu()).getItemName());
      this.addWidget(this.name);
      this.setInitialFocus(this.name);
      ImageButton applyButton = new ImageButton(
         this.leftPos + 133,
         this.topPos + 42,
         19,
         19,
         Widgets.CONFIRM_BUTTON_SPRITES,
         button -> this.confirm(),
         Component.translatable("gui.exposure.item_rename.apply")
      );
      applyButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.item_rename.apply")));
      this.addRenderableWidget(applyButton);
      ImageButton cancelButton = new ImageButton(
         this.leftPos + 154,
         this.topPos + 42,
         19,
         19,
         Widgets.CANCEL_BUTTON_SPRITES,
         button -> this.cancel(),
         Component.translatable("gui.exposure.item_rename.cancel")
      );
      cancelButton.setTooltip(Tooltip.create(Component.translatable("gui.exposure.item_rename.cancel")));
      this.addRenderableWidget(cancelButton);
   }

   protected void confirm() {
      ((ItemRenameMenu)this.getMenu()).clickMenuButton(Minecraft.getInstance().player, 0);
      Objects.requireNonNull(Minecraft.getInstance().gameMode).handleInventoryButtonClick(((ItemRenameMenu)this.getMenu()).containerId, 0);
      this.onClose();
   }

   protected void cancel() {
      this.onClose();
   }

   public void resize(Minecraft minecraft, int width, int height) {
      String string = this.name.getValue();
      this.init(minecraft, width, height);
      this.name.setValue(string);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.name.render(guiGraphics.pose(), mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
      super.renderTooltip(guiGraphics, x, y);
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
   }

   protected void onNameChanged(String name) {
      ItemStack itemStack = ((ItemRenameMenu)this.getMenu()).getSlot(0).getItem();
      if (!DataComponents.CUSTOM_NAME.has(itemStack) && name.equals(itemStack.getHoverName().getString())
         || name.equals(itemStack.getItem().getName(itemStack).getString())) {
         name = "";
      }

      if (((ItemRenameMenu)this.getMenu()).setItemName(name) && Minecraft.getInstance().player != null) {
         Minecraft.getInstance().player.connection.send(new ServerboundRenameItemPacket(name));
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.onClose();
      }

      if (keyCode == 257) {
         this.confirm();
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)SoundEvents.UI_BUTTON_CLICK, 1.0F));
         return true;
      } else {
         return keyCode == 258 || !this.name.keyPressed(keyCode, scanCode, modifiers) && !this.name.canConsumeInput()
            ? super.keyPressed(keyCode, scanCode, modifiers)
            : true;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 1 && this.name.isMouseOver(mouseX, mouseY)) {
         this.name.setValue("");
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }
}
