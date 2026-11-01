package umpaz.brewinandchewin.forge.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
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
      // Port: ItemStackHandler.deserializeNBT resizes to the saved "Size" and setSize empties every slot, so read into a
      // scratch handler and copy across: an 8-slot save (before the leftover slot) keeps its items in 9 slots.
      ItemStackHandler saved = new ItemStackHandler();
      saved.deserializeNBT(tag);
      for (int i = 0; i < this.stacks.size(); i++) {
         this.stacks.set(i, i < saved.getSlots() ? saved.getStackInSlot(i) : ItemStack.EMPTY);
      }
      this.onLoad();
   }

   @Override
   public CompoundTag writeToNbt() {
      return this.serializeNBT();
   }
}
