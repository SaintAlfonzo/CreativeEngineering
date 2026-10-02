package io.github.mortuusars.exposure.client.gui.screen.album;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.screen.PhotographScreen;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

public class ChildPhotographScreen extends PhotographScreen {
   private final Screen parentScreen;

   public ChildPhotographScreen(Screen parentScreen, List<ItemAndStack<PhotographItem>> photographs) {
      super(photographs);
      this.parentScreen = parentScreen;
   }

   @Override
   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      if (this.zoom.get() < this.zoom.getMin() + 0.1F && this.zoom.getTarget() < this.zoom.getMin() + 0.1F) {
         Minecrft.get().setScreen(this.parentScreen);
         Minecrft.player().playSound(Exposure.SoundEvents.PHOTOGRAPH_PLACE.get(), 0.7F, 1.1F);
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode != 256 && !Minecrft.options().keyInventory.matches(keyCode, scanCode)) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         this.onClose();
         return true;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (button == 1) {
         this.zoom.setTarget(0.0);
         return true;
      } else {
         return false;
      }
   }

   public void onClose() {
      this.zoom.setTarget(0.0);
   }
}
