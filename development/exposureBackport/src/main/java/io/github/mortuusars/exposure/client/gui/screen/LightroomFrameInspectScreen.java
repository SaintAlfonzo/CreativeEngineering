package io.github.mortuusars.exposure.client.gui.screen;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import java.util.Collections;
import io.github.mortuusars.exposure.backport.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class LightroomFrameInspectScreen extends FilmFrameInspectScreen {
   private final LightroomScreen lightroomScreen;

   public LightroomFrameInspectScreen(LightroomScreen lightroomScreen) {
      super(((LightroomMenu)lightroomScreen.getMenu()).getExposedFrames(), ((LightroomMenu)lightroomScreen.getMenu()).getSelectedFrame());
      this.lightroomScreen = lightroomScreen;
      this.pager.setChangeSound(null);
   }

   @Override
   protected void pageChanged(int oldPage, int newPage) {
      PagingDirection direction = PagingDirection.fromChange(oldPage, newPage);
      Collections.rotate(this.frames, -direction.getValue());
      if (newPage != ((LightroomMenu)this.lightroomScreen.getMenu()).getSelectedFrame()) {
         this.lightroomScreen.changeFrame(direction);
      }
   }

   @Override
   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      if (this.zoom.get() < this.zoom.getMin() + 0.1F && this.zoom.getTarget() < this.zoom.getMin() + 0.1F) {
         Minecrft.get().setScreen(this.lightroomScreen);
         Minecrft.player().playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1.0F, 0.7F);
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
