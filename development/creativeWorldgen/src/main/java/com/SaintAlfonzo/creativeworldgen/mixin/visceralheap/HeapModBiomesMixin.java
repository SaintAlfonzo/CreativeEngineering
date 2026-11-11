package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.init.ModBiomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/** Removes BOP's TerraBlender setup. Only a stub TerraBlender is installed, and the Heap is placed by the biome tuner. */
@Mixin(value = ModBiomes.class, remap = false)
public abstract class HeapModBiomesMixin {

    /**
     * @author SaintAlfonzo
     * @reason Cancelling is not enough: the original body names TerraBlender and BOP's region classes, and the bytecode
     * verifier would load them.
     */
    @Overwrite
    public static void setupTerraBlender() {
    }
}
