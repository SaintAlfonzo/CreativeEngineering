package io.github.mortuusars.exposure.world.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AlbumPhotographSlot extends Slot {
   protected boolean isActive;

   public AlbumPhotographSlot(Container container, int slot, int x, int y) {
      super(container, slot, x, y);
   }

   public boolean mayPlace(ItemStack stack) {
      return false;
   }

   public boolean mayPickup(Player player) {
      return false;
   }

   public boolean isActive() {
      return this.isActive;
   }

   public void setActive(boolean value) {
      this.isActive = value;
   }
}
