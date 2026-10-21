package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

/**
 * Port status: only the pouring entry so far. Upstream's StackedContents subclass (it lets the recipe book also fill or
 * empty the tank with fluid containers) is not ported yet; the keg recipe book places the item ingredients only.
 */
public class KegStackedContents {
   public record PouringEntry(ItemStack stack, long fluidAmount, FluidUnit fluidUnit, boolean strict) {
   }
}
