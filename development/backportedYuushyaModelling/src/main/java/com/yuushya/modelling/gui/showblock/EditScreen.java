// SPDX-License-Identifier: GPL-3.0-only
// Backport of Yuushya Modelling 2.4.1 gui/showblock/EditScreen.java (MIT, see NOTICE-upstream-MIT.txt) to Forge 1.19.2;
// deviations from 2.4.1 are listed in PORT_NOTES.md.
package com.yuushya.modelling.gui.showblock;

import com.yuushya.modelling.gui.compat.ButtonUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import com.yuushya.modelling.gui.compat.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class EditScreen extends Screen {
   private final Component editBoxLabel;
   private final Consumer<String> callback;
   private final Predicate<String> isValidText;
   private final Screen lastScreen;
   private Button selectButton;
   private EditBox editBox;

   public EditScreen(Screen lastScreen, Component title, Component editBoxLabel, Consumer<String> callback, Predicate<String> isValidText) {
      super(title);
      this.lastScreen = lastScreen;
      this.editBoxLabel = editBoxLabel;
      this.isValidText = isValidText;
      this.callback = callback;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.selectButton.active || this.getFocused() != this.editBox || keyCode != 257 && keyCode != 335) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.onSelect();
         return true;
      }
   }

   protected void init() {
      this.editBox = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, this.editBoxLabel);
      this.editBox.setMaxLength(128);
      this.editBox.setResponder(string -> this.updateSelectButtonStatus());
      this.addWidget(this.editBox);
      this.selectButton = (Button)this.addRenderableWidget(
         ButtonUtils.builder(CommonComponents.GUI_DONE, button -> this.onSelect())
            .bounds(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20)
            .build()
      );
      this.addRenderableWidget(
         ButtonUtils.builder(CommonComponents.GUI_CANCEL, button -> this.callback.accept(null))
            .bounds(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20)
            .build()
      );
      this.updateSelectButtonStatus();
   }

   public void resize(Minecraft minecraft, int width, int height) {
      String string = this.editBox.getValue();
      this.init(minecraft, width, height);
      this.editBox.setValue(string);
   }

   private void onSelect() {
      this.callback.accept(this.editBox.getValue());
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public void removed() {
   }

   private void updateSelectButtonStatus() {
      this.selectButton.active = this.isValidText.test(this.editBox.getValue());
   }

   @Override
   public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
      GuiGraphics guiGraphics = new GuiGraphics(poseStack);
      super.render(poseStack, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      guiGraphics.drawString(this.font, this.editBoxLabel, this.width / 2 - 100 + 1, 100, 10526880);
      this.editBox.render(poseStack, mouseX, mouseY, partialTick);
   }
}
