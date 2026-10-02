package io.github.mortuusars.exposure.world.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class AlbumPlayerInventorySlot extends Slot {
   protected boolean isActive;

   public AlbumPlayerInventorySlot(Container container, int slot, int x, int y) {
      super(container, slot, x, y);
   }

   public boolean isActive() {
      return this.isActive;
   }

   public void setActive(boolean value) {
      this.isActive = value;
   }
}
