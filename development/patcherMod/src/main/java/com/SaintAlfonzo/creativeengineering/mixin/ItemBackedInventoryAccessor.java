package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.botania.common.item.ItemBackedInventory;

/** The item a Botania item-backed inventory (Trinket Case, flower pouch) reads and writes. */
@Mixin(value = ItemBackedInventory.class, remap = false)
public interface ItemBackedInventoryAccessor {
    @Accessor(value = "stack", remap = false)
    ItemStack creativeengineering$stack();
}
