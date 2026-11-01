package umpaz.brewinandchewin.common.item;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

public record KegTooltipComponent(AbstractedFluidStack stack) implements TooltipComponent {
}
