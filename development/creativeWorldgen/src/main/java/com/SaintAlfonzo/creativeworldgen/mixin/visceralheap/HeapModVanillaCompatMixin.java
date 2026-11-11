package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.init.ModVanillaCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 255 eager lookups of removed blocks, none of them for a Heap block. */
@Mixin(value = ModVanillaCompat.class, remap = false)
public abstract class HeapModVanillaCompatMixin {

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private static void creativeworldgen$skip(CallbackInfo ci) {
        ci.cancel();
    }
}
