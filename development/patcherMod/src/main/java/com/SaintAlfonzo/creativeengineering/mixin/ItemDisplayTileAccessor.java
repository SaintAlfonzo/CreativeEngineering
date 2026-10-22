package com.SaintAlfonzo.creativeengineering.mixin;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Moonlight's private {@code ItemDisplayTile.stacks}, so {@code SupplementariesJarTileMixin} can resize a jar's
 * inventory and reorder it on load and save without going through {@code setItem} (which fires setChanged and a
 * block update mid-load). Moonlight is unobfuscated: {@code remap = false}.
 */
@Mixin(value = ItemDisplayTile.class, remap = false)
public interface ItemDisplayTileAccessor {
    @Accessor("stacks")
    NonNullList<ItemStack> creativeengineering$getStacks();

    @Accessor("stacks")
    void creativeengineering$setStacks(NonNullList<ItemStack> stacks);
}
