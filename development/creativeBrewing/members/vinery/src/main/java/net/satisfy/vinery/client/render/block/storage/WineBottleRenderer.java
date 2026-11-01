package net.satisfy.vinery.client.render.block.storage;

import net.minecraft.world.item.ItemStack;
import net.satisfy.foundation.storage.BottleGroupRenderer;
import net.satisfy.vinery.core.registry.ObjectRegistry;

public class WineBottleRenderer extends BottleGroupRenderer {
    @Override
    protected boolean isLying(ItemStack stack) {
        return stack.is(ObjectRegistry.KELP_CIDER.get().asItem());
    }
}
