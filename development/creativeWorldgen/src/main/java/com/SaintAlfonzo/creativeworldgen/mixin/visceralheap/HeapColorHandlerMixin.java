package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.client.handler.ColorHandler;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 22 eager lookups of removed blocks. No Heap block is tinted. */
@Mixin(value = ColorHandler.class, remap = false)
public abstract class HeapColorHandlerMixin {

    @Inject(method = "registerItemColors", at = @At("HEAD"), cancellable = true)
    private static void creativeworldgen$skipItems(RegisterColorHandlersEvent.Item event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "registerBlockColors", at = @At("HEAD"), cancellable = true)
    private static void creativeworldgen$skipBlocks(RegisterColorHandlersEvent.Block event, CallbackInfo ci) {
        ci.cancel();
    }
}
