package com.SaintAlfonzo.creativeworldgen.mixin.visceralheap;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import com.SaintAlfonzo.creativeworldgen.visceralheap.HeapAllowlist;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

/** Drops every Biomes O' Plenty entry outside {@link HeapAllowlist} before its supplier can run. */
@Mixin(value = DeferredRegister.class, remap = false)
public abstract class HeapDeferredRegisterMixin<T> {

    @Shadow @Final private ResourceKey<? extends Registry<T>> registryKey;
    @Shadow @Final private String modid;
    @Shadow @Final private Map<RegistryObject<T>, Supplier<? extends T>> entries;

    @Inject(method = "addEntries", at = @At("HEAD"))
    private void creativeworldgen$keepHeapOnly(RegisterEvent event, CallbackInfo ci) {
        if (!HeapAllowlist.MOD_ID.equals(modid) || !event.getRegistryKey().equals(registryKey)) return;
        int before = entries.size();
        entries.keySet().removeIf(object -> !HeapAllowlist.keep(registryKey.location(), object.getId().getPath()));
        if (entries.size() != before) {
            CreativeWorldgen.LOGGER.info("[creativeworldgen] Visceral Heap: kept {} of {} Biomes O' Plenty entries in {}",
                    entries.size(), before, registryKey.location());
        }
    }
}
