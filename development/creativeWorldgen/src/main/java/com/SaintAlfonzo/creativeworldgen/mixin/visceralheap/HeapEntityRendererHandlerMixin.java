package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import biomesoplenty.client.handler.EntityRendererHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Renderers for BOP's boats and signs, all filtered out. */
@Mixin(value = EntityRendererHandler.class, remap = false)
public abstract class HeapEntityRendererHandlerMixin {

    @Inject(method = "onRegisterRenderers", at = @At("HEAD"), cancellable = true)
    private static void creativeworldgen$skip(EntityRenderersEvent.RegisterRenderers event, CallbackInfo ci) {
        ci.cancel();
    }
}
