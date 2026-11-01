package umpaz.brewinandchewin.forge.container;

import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

public class SidedKegWrapperForge extends SidedKegWrapper implements IItemHandlerModifiable {
   public SidedKegWrapperForge(AbstractedItemHandler itemHandler, @Nullable Direction side) {
      super(itemHandler, side);
   }

   @Override
   public int getSlots() {
      return this.getSlotCount();
   }
}
