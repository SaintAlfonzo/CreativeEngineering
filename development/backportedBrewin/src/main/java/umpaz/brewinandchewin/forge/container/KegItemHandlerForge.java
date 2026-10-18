package umpaz.brewinandchewin.forge.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.ItemStackHandler;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

public class KegItemHandlerForge extends ItemStackHandler implements AbstractedItemHandler {
   public KegItemHandlerForge(int size) {
      super(size);
   }

   @Override
   public int getSlotCount() {
      return this.getSlots();
   }

   @Override
   public void readFromNbt(CompoundTag tag) {
      // Port: a 2.0 keg saved 9 slots; ItemStackHandler.deserializeNBT would resize to "Size", so pin it to ours.
      int size = this.stacks.size();
      this.deserializeNBT(tag);
      if (this.stacks.size() != size) {
         this.setSize(size);
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      return this.serializeNBT();
   }
}
