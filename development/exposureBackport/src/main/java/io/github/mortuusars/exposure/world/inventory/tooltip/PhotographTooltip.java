package io.github.mortuusars.exposure.world.inventory.tooltip;

import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PhotographTooltip(List<ItemAndStack<PhotographItem>> photographs) implements TooltipComponent {
}
