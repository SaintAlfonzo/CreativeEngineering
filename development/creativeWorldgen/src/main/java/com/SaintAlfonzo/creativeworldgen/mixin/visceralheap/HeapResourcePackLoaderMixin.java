package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import com.SaintAlfonzo.creativeworldgen.visceralheap.HeapAllowlist;
import com.SaintAlfonzo.creativeworldgen.visceralheap.HeapPackResources;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Serves Biomes O' Plenty's mod pack through {@link HeapPackResources}, which hides everything but the Heap's files. */
@Mixin(value = ResourcePackLoader.class, remap = false)
public abstract class HeapResourcePackLoaderMixin {

    @Inject(method = "createPackForMod", at = @At("RETURN"), cancellable = true)
    private static void creativeworldgen$heapPack(IModFileInfo info, CallbackInfoReturnable<PathPackResources> cir) {
        if (info.getMods().stream().anyMatch(mod -> HeapAllowlist.MOD_ID.equals(mod.getModId()))) {
            cir.setReturnValue(new HeapPackResources(info));
        }
    }
}
